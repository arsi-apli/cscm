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
package sk.arsi.nb.help.module.client;

import java.io.ByteArrayInputStream;
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
import java.net.Socket;
import java.nio.ByteBuffer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbPreferences;
import static sk.arsi.nb.help.module.client.CommunitydocPanel.getSysUserName;
import sk.arsi.nb.help.server.local.LocalTransferManager;
import sk.arsi.nb.help.transfer.AccountTestResult;
import sk.arsi.nb.help.transfer.AddRank;
import sk.arsi.nb.help.transfer.CreateHelpRecord;
import sk.arsi.nb.help.transfer.FindByClass;
import sk.arsi.nb.help.transfer.FindByKey;
import sk.arsi.nb.help.transfer.FindFullTextCode;
import sk.arsi.nb.help.transfer.FindFullTextDescription;
import sk.arsi.nb.help.transfer.GetMimeTypes;
import sk.arsi.nb.help.transfer.RegeneratePassword;
import sk.arsi.nb.help.transfer.RegisterUser;
import sk.arsi.nb.help.transfer.Status;
import sk.arsi.nb.help.transfer.TestAccount;
import sk.arsi.nb.help.transfer.VersionProvider;

/**
 *
 * @author arsi
 */
public class NbDocClient {

    public static final String SERVER = "SERVER";
    public static final String PORT = "PORT";
    public static final String EMAIL = "EMAIL";
    public static final String PASSWORD_HASH = "PASSWORD_HASH";
    public static final String TEAM_SERVER = "TEAM_SERVER";
    public static final String TEAM_PORT = "TEAM_PORT";
    public static final String TEAM_EMAIL = "TEAM_EMAIL";
    public static final String TEAM_PASSWORD_HASH = "TEAM_PASSWORD_HASH";
    public static final String MAX_DESCRIPTION_RESULTS = "MAX_DESCRIPTION_RESULTS";
    public static final String MAX_CODE_RESULTS = "MAX_CODE_RESULTS";
    private static boolean transferOK = true;

    private static Socket connect(ServerType serverType) throws IOException, Exception {
        if (!transferOK) {
            throw new IOException("Unsupported server version");
        }
        Socket clientSocket = null;
        switch (serverType) {
            case MASTER:
                clientSocket = new Socket(NbPreferences.forModule(CommunitydocPanel.class).get(NbDocClient.SERVER, "server.arsi.sk"), NbPreferences.forModule(CommunitydocPanel.class).getInt(NbDocClient.PORT, 3232));

                break;
            case TEAM:
                String server = NbPreferences.forModule(CommunitydocPanel.class).get(NbDocClient.TEAM_SERVER, "");
                if ("".equals(server)) {
                    return null;
                }
                clientSocket = new Socket(server, NbPreferences.forModule(CommunitydocPanel.class).getInt(NbDocClient.PORT, 3232));

                break;
            case LOCAL:
                break;
            default:
                throw new AssertionError(serverType.name());

        }
        if (clientSocket != null) {
            clientSocket.setSoTimeout(10000);
        }
        return clientSocket;
    }

    private static Socket connect(String server, int port) throws IOException, Exception {
        if (!transferOK) {
            throw new IOException("Unsupported server version");
        }
        Socket clientSocket = null;
        clientSocket = new Socket(server, port);
        clientSocket.setSoTimeout(10000);
        return clientSocket;
    }

    private static Object sendAndReceive(Socket clientSocket, Serializable toSend, ServerType serverType) throws IOException, Exception {
        switch (serverType) {
            case MASTER:
            case TEAM:
                OutputStream outputStream = clientSocket.getOutputStream();
                ObjectEncoderA encoder = new ObjectEncoderA();
                outputStream.write(encoder.encode(VersionProvider.INSTANCE.getVersion())); //send transfer version to server
                outputStream.flush();
                outputStream.write(encoder.encode(toSend));
                outputStream.flush();
                InputStream inputStream = clientSocket.getInputStream();
                byte[] length = new byte[4];
                inputStream.read(length);
                int messageSize = ByteBuffer.allocate(4).put(length).getInt(0);
                byte msgData[] = new byte[messageSize];
                int currentPos = 0;
                while (currentPos < messageSize) {
                    currentPos += inputStream.read(msgData, currentPos, messageSize - currentPos);
                }
                ObjectInputStream is = new CompactObjectInputStreamA(new ByteArrayInputStream(msgData));
                Object response = is.readObject();
                if (response instanceof String) {
                    if (VersionProvider.UNSUPPORTED_TRANSFER_VERSION.equals(response) && transferOK) {
                        transferOK = false;
                        NotifyDescriptor nd = new NotifyDescriptor.Message("Incompatible version of the server, please update the Community help plugin..", NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    }
                }
                return response;
            default:
                if (toSend instanceof CreateHelpRecord) {
                    return LocalTransferManager.createHelpRecord((CreateHelpRecord) toSend);
                } else if (toSend instanceof FindByKey) {
                    return LocalTransferManager.findByKey((FindByKey) toSend);
                } else if (toSend instanceof FindByClass) {
                    return LocalTransferManager.findByClass((FindByClass) toSend);
                } else if (toSend instanceof FindFullTextCode) {
                    return LocalTransferManager.findFullTextCode((FindFullTextCode) toSend);
                } else if (toSend instanceof FindFullTextDescription) {
                    return LocalTransferManager.findFullTextDescription((FindFullTextDescription) toSend);
                } else if (toSend instanceof GetMimeTypes) {
                    return LocalTransferManager.getMimeTypes((GetMimeTypes) toSend);
                }

        }
        return null;

    }

    public static Object getByKey(String key, ServerType serverType, String mimeType) {
        try {
            Socket clientSocket = connect(serverType);
            FindByKey findByKey = new FindByKey(key, mimeType);
            return sendAndReceive(clientSocket, findByKey, serverType);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Object getByClass(String name, ServerType serverType, String mimeType) {
        try {
            Socket clientSocket = connect(serverType);
            FindByClass findByClass = new FindByClass(name, mimeType);
            return sendAndReceive(clientSocket, findByClass, serverType);
        } catch (Exception ex) {
        }
        return null;
    }

    public static Object getAllMimeTypes(ServerType serverType) {
        try {
            Socket clientSocket = connect(serverType);
            return sendAndReceive(clientSocket, new GetMimeTypes(), serverType);
        } catch (Exception ex) {
        }
        return null;
    }

    public static Object getByFullTextDescription(String name, ServerType serverType, String mimeType) {
        return getByFullTextDescription(name, serverType, mimeType, NbPreferences.forModule(CommunitydocPanel.class).getInt(NbDocClient.MAX_DESCRIPTION_RESULTS, 25));
    }

    public static Object getByFullTextDescription(String name, ServerType serverType, String mimeType, int count) {
        try {
            Socket clientSocket = connect(serverType);
            FindFullTextDescription findByClass = new FindFullTextDescription(name, mimeType, count);
            return sendAndReceive(clientSocket, findByClass, serverType);
        } catch (Exception ex) {
        }
        return null;
    }

    public static Object getByFullTextCode(String name, ServerType serverType, String mimeType) {
        return getByFullTextCode(name, serverType, mimeType, NbPreferences.forModule(CommunitydocPanel.class).getInt(NbDocClient.MAX_CODE_RESULTS, 25));
    }

    public static Object getByFullTextCode(String name, ServerType serverType, String mimeType, int count) {
        try {
            Socket clientSocket = connect(serverType);
            FindFullTextCode findByClass = new FindFullTextCode(name, mimeType, count);
            return sendAndReceive(clientSocket, findByClass, serverType);
        } catch (Exception ex) {
        }
        return null;
    }

    public static void backup(String sql) {
        LocalTransferManager.backup(sql);
    }

    public static Status addRank(long id, int rank, ServerType serverType) {
        try {
            Socket clientSocket = connect(serverType);
            AddRank addRank = null;
            switch (serverType) {
                case MASTER:
                    addRank = new AddRank(id, rank, NbPreferences.forModule(CommunitydocPanel.class).get(NbDocClient.EMAIL, getSysUserName()));
                    break;
                case TEAM:
                    addRank = new AddRank(id, rank, NbPreferences.forModule(CommunitydocPanel.class).get(NbDocClient.TEAM_EMAIL, getSysUserName()));
                    break;
                case LOCAL:
                    break;
                default:
                    throw new AssertionError(serverType.name());
            }
            return (Status) sendAndReceive(clientSocket, addRank, serverType);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Status registerUser(String firstName, String lastName, String email, String server, int port) {
        try {
            Socket clientSocket = connect(server, port);
            RegisterUser registerUser = new RegisterUser();
            return (Status) sendAndReceive(clientSocket, registerUser, ServerType.MASTER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Status registerUser(RegisterUser registerUser, String server, int port) {
        try {
            Socket clientSocket = connect(server, port);
            return (Status) sendAndReceive(clientSocket, registerUser, ServerType.MASTER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Status regeneratePassword(RegeneratePassword regeneratePassword, String server, int port) {
        try {
            Socket clientSocket = connect(server, port);
            return (Status) sendAndReceive(clientSocket, regeneratePassword, ServerType.MASTER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static AccountTestResult testAccount(TestAccount testAccount, String server, int port) {
        try {
            Socket clientSocket = connect(server, port);
            return (AccountTestResult) sendAndReceive(clientSocket, testAccount, ServerType.MASTER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Status addHelp(CreateHelpRecord record, ServerType serverType) {
        try {
            Socket clientSocket = connect(serverType);
            return (Status) sendAndReceive(clientSocket, record, serverType);
        } catch (Exception ex) {
        }
        return null;
    }

    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

    static void restore(String absolutePath) {
        LocalTransferManager.restore(absolutePath);
    }

    private static class ObjectEncoderA {

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

    private static class CompactObjectInputStreamA extends ObjectInputStream {

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

    private static class CompactObjectOutputStreamA extends ObjectOutputStream {

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
}
