/*
 * (C) Copyright 2017 Arsi (http://www.arsi.sk/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package sk.arsi.nb.help.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import static java.io.ObjectStreamConstants.STREAM_VERSION;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.IndexWriter;
import sk.arsi.nb.help.server.config.ConfigManager;
import sk.arsi.nb.help.server.lucene.LuceneManager;
import sk.arsi.nb.help.transfer.AddRank;
import sk.arsi.nb.help.transfer.CreateHelpRecord;
import sk.arsi.nb.help.transfer.FindByClass;
import sk.arsi.nb.help.transfer.FindByKey;
import sk.arsi.nb.help.transfer.FindFullTextCode;
import sk.arsi.nb.help.transfer.FindFullTextDescription;
import sk.arsi.nb.help.transfer.GetDescriptions;
import sk.arsi.nb.help.transfer.GetMimeTypes;
import sk.arsi.nb.help.transfer.RegeneratePassword;
import sk.arsi.nb.help.transfer.RegisterUser;
import sk.arsi.nb.help.transfer.TestAccount;

/**
 *
 * @author arsi
 */
public class Main {

    private static final int DEFAULT_EVENT_LOOP_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    public static final ExecutorService pool = Executors.newFixedThreadPool(DEFAULT_EVENT_LOOP_THREADS);
    public static final ExecutorService sqlPool = Executors.newFixedThreadPool(DEFAULT_EVENT_LOOP_THREADS);
    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;
    private static boolean linux64;

    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
    public static IndexWriter writer;

    static class CompactObjectInputStreamA extends ObjectInputStream {

        CompactObjectInputStreamA(InputStream in) throws IOException {
            super(in);

        }

        @Override
        protected void readStreamHeader() throws IOException {
            int version = readByte() & 0xFF;
            if (version != STREAM_VERSION) {
                throw new StreamCorruptedException(
                        "Unsupported version: " + version);
            }
        }

        @Override
        protected ObjectStreamClass readClassDescriptor()
                throws IOException, ClassNotFoundException {
            int type = read();
            if (type < 0) {
                throw new EOFException();
            }
            switch (type) {
                case CompactObjectOutputStreamA.TYPE_FAT_DESCRIPTOR:
                    return super.readClassDescriptor();
                case CompactObjectOutputStreamA.TYPE_THIN_DESCRIPTOR:
                    String className = readUTF();
                    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                    return ObjectStreamClass.lookupAny(clazz);
                default:
                    throw new StreamCorruptedException(
                            "Unexpected class descriptor type: " + type);
            }
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            Class<?> clazz;
            try {
                clazz = Thread.currentThread().getContextClassLoader().loadClass(desc.getName());
            } catch (ClassNotFoundException ignored) {
                clazz = super.resolveClass(desc);
            }

            return clazz;
        }

    }

    static class CompactObjectOutputStreamA extends ObjectOutputStream {

        static final int TYPE_FAT_DESCRIPTOR = 0;
        static final int TYPE_THIN_DESCRIPTOR = 1;

        CompactObjectOutputStreamA(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            writeByte(STREAM_VERSION);
        }

        @Override
        protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
            Class<?> clazz = desc.forClass();
            if (clazz.isPrimitive() || clazz.isArray() || clazz.isInterface()
                    || desc.getSerialVersionUID() == 0) {
                write(TYPE_FAT_DESCRIPTOR);
                super.writeClassDescriptor(desc);
            } else {
                write(TYPE_THIN_DESCRIPTOR);
                writeUTF(desc.getName());
            }
        }
    }

    public static class ObjectEncoderA {

        protected byte[] encode(Serializable msg) throws Exception {

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            bout.write(LENGTH_PLACEHOLDER);
            ObjectOutputStream oout = new CompactObjectOutputStreamA(bout);
            oout.writeObject(msg);
            oout.flush();
            oout.close();
            byte[] bytes = ByteBuffer.allocate(4).putInt(bout.toByteArray().length - 4).array();
            byte[] toByteArray = bout.toByteArray();
            System.arraycopy(bytes, 0, toByteArray, 0, 4);
            return toByteArray;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Command line: path to config.ini");
            System.exit(0);
        }
        try {
            ConfigManager.loadConfig(args[0]);
        } catch (IOException ex) {
            System.out.println("Unable to parse " + args[0]);
            System.exit(0);
        }
        try {
            //lucene
            LuceneManager.openLucene();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        DatabaseManager.startDatabase();
        DatabaseManager.createMimeIfNotExist("text/plain");
        DatabaseManager.createMimeIfNotExist("text/rtf");
        DatabaseManager.createMimeIfNotExist("text/x-java");
        DatabaseManager.createMimeIfNotExist("text/css");
        DatabaseManager.createMimeIfNotExist("text/x-asm");
        DatabaseManager.createMimeIfNotExist("text/x-groovy");
        DatabaseManager.createMimeIfNotExist("text/x-gsp");
        DatabaseManager.createMimeIfNotExist("text/jade");
        DatabaseManager.createMimeIfNotExist("text/javascript");
        DatabaseManager.createMimeIfNotExist("text/x-latte");
        DatabaseManager.createMimeIfNotExist("text/less");
        DatabaseManager.createMimeIfNotExist("text/x-manifest");
        DatabaseManager.createMimeIfNotExist("text/x-neon");
        DatabaseManager.createMimeIfNotExist("text/x-oql");
        DatabaseManager.createMimeIfNotExist("text/x-php");
        DatabaseManager.createMimeIfNotExist("text/x-sql");
        DatabaseManager.createMimeIfNotExist("text/scss");
        DatabaseManager.createMimeIfNotExist("text/x-tpl");
        DatabaseManager.createMimeIfNotExist("text/x-twig");
        DatabaseManager.createMimeIfNotExist("text/x-yaml");
        DatabaseManager.createMimeIfNotExist("text/html");

        System.setProperty("io.netty.recycler.maxCapacity.default", "0");
        System.setProperty("io.netty.allocator.pageSize", "4096");
        System.setProperty("io.netty.allocator.maxOrder", "9");
        System.setProperty("io.netty.allocator.numHeapArenas", "2");
        System.setProperty("io.netty.allocator.cacheTrimInterval", "500");
        System.setProperty("io.netty.buffer.bytebuf.checkAccessible", "true");

        if (Epoll.isAvailable()) {
            bossGroup = new EpollEventLoopGroup(1, new DefaultThreadFactory("netty-accept"));
            workerGroup = new EpollEventLoopGroup(DEFAULT_EVENT_LOOP_THREADS, new DefaultThreadFactory("netty-server"));
            linux64 = true;
        } else {
            bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("netty-accept"));
            workerGroup = new NioEventLoopGroup(DEFAULT_EVENT_LOOP_THREADS, new DefaultThreadFactory("netty-server"));
            linux64 = false;
        }
        Class<? extends ServerChannel> channelClass = NioServerSocketChannel.class;
        if (linux64) {
            channelClass = EpollServerSocketChannel.class;
        }
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.childOption(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT);
            b.group(bossGroup, workerGroup)
                    .channel(channelClass)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.config().setOption(ChannelOption.SO_REUSEADDR, true);
                            ch.config().setOption(ChannelOption.SO_TIMEOUT, 5000);
                            ch.config().setOption(ChannelOption.TCP_NODELAY, false);
                            ch.pipeline().addLast(new ObjectEncoder(), new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEchoServerHandler());

                        }
                    });
            try {
                // Bind and start to accept incoming connections.
                b.bind(((int) ConfigManager.SERVER.get(ConfigManager.SERVER_PORT, Integer.class))).sync().channel().closeFuture().sync();
            } catch (InterruptedException ex) {
                //             Exceptions.printStackTrace(ex);
            }
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private static class ObjectEchoServerHandler extends ChannelInboundHandlerAdapter {

        public ObjectEchoServerHandler() {
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // after connect send the client a version of his transfer lib
            if (msg instanceof String) {
                TransferManager.checkClientVersion(msg, ctx);
            } else if (msg instanceof FindByKey) {
                TransferManager.findByKey((FindByKey) msg, ctx);
            } else if (msg instanceof FindByClass) {
                TransferManager.findByClass((FindByClass) msg, ctx);
            } else if (msg instanceof CreateHelpRecord) {
                TransferManager.createHelpRecord((CreateHelpRecord) msg, ctx);
            } else if (msg instanceof AddRank) {
                TransferManager.addRank((AddRank) msg, ctx);
            } else if (msg instanceof RegisterUser) {
                TransferManager.registerUser((RegisterUser) msg, ctx);
            } else if (msg instanceof RegeneratePassword) {
                TransferManager.regeneratePassword((RegeneratePassword) msg, ctx);
            } else if (msg instanceof TestAccount) {
                TransferManager.testAccount((TestAccount) msg, ctx);
            } else if (msg instanceof FindFullTextCode) {
                TransferManager.findFullTextCode((FindFullTextCode) msg, ctx);
            } else if (msg instanceof FindFullTextDescription) {
                TransferManager.findFullTextDescription((FindFullTextDescription) msg, ctx);
            } else if (msg instanceof GetMimeTypes) {
                TransferManager.getMimeTypes((GetMimeTypes) msg, ctx);
            } else if (msg instanceof GetMimeTypes) {
                TransferManager.getDescriptions((GetDescriptions) msg, ctx);
            } else {
                ReferenceCountUtil.release(msg);
                ctx.channel().close();
            }

        }

    }

}
