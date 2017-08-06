/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.nb.help.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import static sk.arsi.nb.help.server.Main.sqlPool;
import sk.arsi.nb.help.server.config.ConfigManager;
import sk.arsi.nb.help.server.db.Classeslist;
import sk.arsi.nb.help.server.db.Helps;
import sk.arsi.nb.help.server.db.Keyslist;
import sk.arsi.nb.help.server.db.Mimetype;
import sk.arsi.nb.help.server.db.Users;
import sk.arsi.nb.help.server.lucene.LuceneManager;
import sk.arsi.nb.help.server.mail.MailTools;
import sk.arsi.nb.help.transfer.AccountTestResult;
import sk.arsi.nb.help.transfer.AddRank;
import sk.arsi.nb.help.transfer.CreateHelpRecord;
import sk.arsi.nb.help.transfer.DescriptionRecord;
import sk.arsi.nb.help.transfer.FindByClass;
import sk.arsi.nb.help.transfer.FindByKey;
import sk.arsi.nb.help.transfer.FindFullTextCode;
import sk.arsi.nb.help.transfer.FindFullTextDescription;
import sk.arsi.nb.help.transfer.GetDescriptions;
import sk.arsi.nb.help.transfer.GetMimeTypes;
import sk.arsi.nb.help.transfer.HelpRecord;
import sk.arsi.nb.help.transfer.MimeRecord;
import sk.arsi.nb.help.transfer.RegeneratePassword;
import sk.arsi.nb.help.transfer.RegisterUser;
import sk.arsi.nb.help.transfer.Status;
import sk.arsi.nb.help.transfer.TestAccount;
import sk.arsi.nb.help.transfer.VersionProvider;

/**
 *
 * @author arsi
 */
public class TransferManager {

    public static void checkClientVersion(Object msg, ChannelHandlerContext ctx) throws Exception {
        if (!VersionProvider.INSTANCE.getVersion().equals(msg)) {
            // client transfer lib version not equal server transfer version, drop connection
            ctx.channel().writeAndFlush(VersionProvider.UNSUPPORTED_TRANSFER_VERSION);
            ReferenceCountUtil.release(msg);
            ctx.channel().close();
        }
    }

    public static void addRank(AddRank msg, ChannelHandlerContext ctx) throws Exception {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Helps help = DatabaseManager.findHelpById(msg.getId());
                if (help != null && !DatabaseManager.rankExist(msg.getId(), msg.getUser())) {
                    try {
                        DatabaseManager.addRankToDb(msg, help);
                        ctx.channel().writeAndFlush(new Status(true));

                    } catch (Exception e) {
                        ctx.channel().writeAndFlush(new Status(false));
                        e.printStackTrace();
                    }
                    ReferenceCountUtil.release(msg);
                    ctx.channel().close();
                } else {
                    ctx.channel().writeAndFlush(new Status(false));
                    ReferenceCountUtil.release(msg);
                    ctx.channel().close();
                }
            }
        };
        sqlPool.execute(runnable);

    }

    public static void findByKey(FindByKey msg, ChannelHandlerContext ctx) throws Exception {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                List<HelpRecord> records = new ArrayList<>();
                try {
                    List<Keyslist> resultList = DatabaseManager.findKeys(msg.getKey(), msg.getMimeType());
                    List<Helps> list = new ArrayList<>();
                    for (Keyslist key : resultList) {
                        list.addAll(key.getHelpsList());
                    }
                    orderByRank(list, records);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ctx.channel().writeAndFlush(records.toArray(new HelpRecord[records.size()]));
                ReferenceCountUtil.release(msg);
                ctx.channel().close();
            }
        };
        Main.pool.execute(runnable);
    }

    public static void findByClass(FindByClass msg, ChannelHandlerContext ctx) throws Exception {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                List<HelpRecord> records = new ArrayList<>();
                try {
                    List<Classeslist> resultList = DatabaseManager.findClasses(msg.getClassName(), msg.getMimeType());
                    List<Helps> list = new ArrayList<>();
                    for (Classeslist key : resultList) {
                        list.addAll(key.getHelpsList());
                    }
                    orderByRank(list, records);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ctx.channel().writeAndFlush(records.toArray(new HelpRecord[records.size()]));
                ReferenceCountUtil.release(msg);
                ctx.channel().close();
            }
        };
        Main.pool.execute(runnable);
    }

    public static void createHelpRecord(CreateHelpRecord msg, ChannelHandlerContext ctx) throws Exception {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (DatabaseManager.helpExist(msg.getCode())) {
                    ctx.channel().writeAndFlush(new Status(false));
                } else {
                    try {
                        Boolean disableAuth = (Boolean) ConfigManager.SERVER.get(ConfigManager.SERVER_TURN_OFF_AUTHENTICATION, Boolean.class);
                        Users user = DatabaseManager.findUser(msg.getEmail());
                        if (!disableAuth) {
                            if (user == null || !user.getPassword().equals(msg.getPasswordHash())) {
                                ctx.channel().writeAndFlush(new Status(false));
                                ReferenceCountUtil.release(msg);
                                ctx.channel().close();
                                return;
                            }
                        }
                        Mimetype mime = DatabaseManager.findOrCreateMimeType(msg.getMimeType());
                        List<Keyslist> keys = DatabaseManager.findOrCereateKeys(msg.getKeys());
                        List<Classeslist> classes = DatabaseManager.findOrCereateClasses(msg.getClasses());
                        DatabaseManager.addHelp(keys, classes, msg.getCode(), msg.getDescription(), mime, user);

                        ctx.channel().writeAndFlush(new Status(true));
                    } catch (Exception e) {
                        e.printStackTrace();
                        ctx.channel().writeAndFlush(new Status(false));
                    }
                }
                ReferenceCountUtil.release(msg);
                ctx.channel().close();
            }
        };
        sqlPool.execute(runnable);

    }

    public static void registerUser(final RegisterUser msg, final ChannelHandlerContext ctx) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Users user = DatabaseManager.findUser(msg.getEmail());
                    if (user == null && msg.getEmail() != null) {
                        String password = randomKey(8);
                        String hash = createHash(password);
                        DatabaseManager.addUser(msg.getEmail(), msg.getFirstName(), msg.getLastName(), hash);
                        ctx.channel().writeAndFlush(new Status(true));
                        try {
                            MailTools.sendEmail(msg.getEmail(), "Community help registration", "Email: " + msg.getEmail() + "\nPassword: " + password);
                        } catch (MessagingException messagingException) {
                        }

                    } else {
                        ctx.channel().writeAndFlush(new Status(false));

                    }
                } catch (Exception e) {
                    ctx.channel().writeAndFlush(new Status(false));
                }
                ReferenceCountUtil.release(msg);
                ctx.channel().close();
            }
        };
        sqlPool.execute(runnable);
    }

    public static void regeneratePassword(final RegeneratePassword msg, final ChannelHandlerContext ctx) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Users user = DatabaseManager.findUser(msg.getMail());
                if (user != null) {
                    try {
                        ctx.channel().writeAndFlush(new Status(true));
                        MailTools.sendEmail(user.getEmail(), "Community help password refresh", "Email: " + user.getEmail() + "\nPassword: " + user.getPassword());
                    } catch (Exception ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        ctx.channel().writeAndFlush(new Status(false));
                    }

                } else {
                    ctx.channel().writeAndFlush(new Status(false));
                }
                ReferenceCountUtil.release(msg);
                ctx.channel().close();
            }
        };
        sqlPool.execute(runnable);

    }

    public static void testAccount(TestAccount msg, ChannelHandlerContext ctx) {
        Runnable runnable = new Runnable() {
            public void run() {
                Users user = DatabaseManager.findUser(msg.getEmail());
                if (user != null) {
                    if (user.getPassword().equals(msg.getPasswordHash())) {
                        ctx.channel().writeAndFlush(new AccountTestResult(msg.getEmail(), msg.getPasswordHash(), true));
                        ReferenceCountUtil.release(msg);
                        ctx.channel().close();
                    } else {
                        ctx.channel().writeAndFlush(new AccountTestResult(msg.getEmail(), msg.getPasswordHash(), false));
                        ReferenceCountUtil.release(msg);
                        ctx.channel().close();
                    }

                } else {
                    ctx.channel().writeAndFlush(new AccountTestResult(msg.getEmail(), msg.getPasswordHash(), false));
                    ReferenceCountUtil.release(msg);
                    ctx.channel().close();
                }
            }
        };
        sqlPool.execute(runnable);
    }

    static final Random random = new SecureRandom();

    public static String randomKey(int length) {
        return String.format("%" + length + "s", new BigInteger(length * 5/*base 32,2^5*/, random)
                .toString(32)).replace('\u0020', '0');
    }

    public static String createHash(String password) {
        return password;
    }

    public static void findFullTextCode(FindFullTextCode msg, ChannelHandlerContext ctx) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                List<HelpRecord> records = new ArrayList<>();
                try {
                    Integer[] findHelp = LuceneManager.findHelp(msg.getText(), msg.getMaxRecords());
                    List<Helps> helps = DatabaseManager.findHelpsById(findHelp, msg.getMimeType());
                    orderByRank(helps, records);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ctx.channel().writeAndFlush(records.toArray(new HelpRecord[records.size()]));
                ReferenceCountUtil.release(msg);
                ctx.channel().close();
            }

        };
        Main.pool.execute(runnable);
    }

    public static void findFullTextDescription(FindFullTextDescription msg, ChannelHandlerContext ctx) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                List<HelpRecord> records = new ArrayList<>();
                try {
                    Integer[] findHelp = LuceneManager.findDescription(msg.getText(), msg.getMaxRecords());
                    List<Helps> helps = DatabaseManager.findHelpsById(findHelp, msg.getMimeType());
                    orderByRank(helps, records);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ctx.channel().writeAndFlush(records.toArray(new HelpRecord[records.size()]));
                ReferenceCountUtil.release(msg);
                ctx.channel().close();
            }
        };
        Main.pool.execute(runnable);
    }

    private static void orderByRank(List<Helps> helps, List<HelpRecord> records) {
        for (Helps help : helps) {
            int rank = DatabaseManager.computeRankForHelp(help.getIdhelps());
            List<Classeslist> classeslistList = help.getClasseslistList();
            List<String> cls = new ArrayList<>();
            for (Classeslist l : classeslistList) {
                cls.add(l.getClassname());
            }
            List<Keyslist> keyslistList = help.getKeyslistList();
            List<String> keys = new ArrayList<>();
            for (Keyslist kls : keyslistList) {
                keys.add(kls.getKeyname());
            }
            records.add(new HelpRecord(help.getIdhelps(), help.getCreateddate(), help.getUser().getFirst() + " " + help.getUser().getLast(), help.getHelp(), rank, keys.toArray(new String[keys.size()]), cls.toArray(new String[cls.size()]), help.getDescription()));
        }
        Collections.sort(records, new Comparator<HelpRecord>() {
            @Override
            public int compare(HelpRecord o1, HelpRecord o2) {
                return Integer.compare(o2.getRank(), o1.getRank());
            }
        });
    }

    static void getMimeTypes(GetMimeTypes msg, ChannelHandlerContext ctx) {
        Mimetype[] allMimeTypes = DatabaseManager.allMimeTypes();
        List<MimeRecord> records = new ArrayList<>();
        for (Mimetype mime : allMimeTypes) {
            records.add(new MimeRecord(mime.getMimetype(), mime.getDescription()));
        }
        ctx.channel().writeAndFlush(records.toArray(new MimeRecord[records.size()]));
        ReferenceCountUtil.release(msg);
        ctx.channel().close();

    }

    public static void getDescriptions(GetDescriptions msg, ChannelHandlerContext ctx) {
        List<Helps> helps = DatabaseManager.findHelpsByMimeType(msg.getMimeType());
        List<DescriptionRecord> records = new ArrayList<>();
        for (Helps help : helps) {
            records.add(new DescriptionRecord(help.getIdhelps(), help.getDescription()));
        }
        ctx.channel().writeAndFlush(records.toArray(new DescriptionRecord[records.size()]));
        ReferenceCountUtil.release(msg);
        ctx.channel().close();
    }
}
