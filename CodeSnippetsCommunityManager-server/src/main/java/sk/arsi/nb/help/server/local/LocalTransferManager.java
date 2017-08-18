/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.nb.help.server.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import sk.arsi.nb.help.server.DatabaseManager;
import sk.arsi.nb.help.server.db.Classeslist;
import sk.arsi.nb.help.server.db.Helps;
import sk.arsi.nb.help.server.db.Keyslist;
import sk.arsi.nb.help.server.db.Mimetype;
import sk.arsi.nb.help.server.db.Users;
import sk.arsi.nb.help.server.lucene.LuceneManager;
import sk.arsi.nb.help.transfer.CreateHelpRecord;
import sk.arsi.nb.help.transfer.DeleteSnippet;
import sk.arsi.nb.help.transfer.DescriptionRecord;
import sk.arsi.nb.help.transfer.EditHelpRecord;
import sk.arsi.nb.help.transfer.FindByClass;
import sk.arsi.nb.help.transfer.FindByKey;
import sk.arsi.nb.help.transfer.FindFullTextCode;
import sk.arsi.nb.help.transfer.FindFullTextDescription;
import sk.arsi.nb.help.transfer.GetDescriptions;
import sk.arsi.nb.help.transfer.GetMimeTypes;
import sk.arsi.nb.help.transfer.GetSingleHelpRecord;
import sk.arsi.nb.help.transfer.HelpRecord;
import sk.arsi.nb.help.transfer.MimeRecord;
import sk.arsi.nb.help.transfer.Status;

/**
 *
 * @author arsi
 */
public class LocalTransferManager {

    public static void Start(File homeDirectory) {
        try {
            //lucene
            LuceneManager.openLucene(homeDirectory);
        } catch (IOException ex) {
        }
        DatabaseManager.startDatabase(homeDirectory.getAbsolutePath() + File.separator + "codesnippets");
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
    }

    public static void backup(String sqlPath) {
        DatabaseManager.backup(sqlPath);
    }

    public static HelpRecord[] findByKey(FindByKey msg) throws Exception {
        List<HelpRecord> records = new ArrayList<>();
        try {
            List<Keyslist> resultList = DatabaseManager.findKeys(msg.getKey(), msg.getMimeType());
            List<Helps> list = new ArrayList<>();
            for (Keyslist key : resultList) {
                list.addAll(key.getHelpsList());
            }
            createHelps(list, records);
        } catch (Exception e) {
        }
        return records.toArray(new HelpRecord[records.size()]);
    }

    public static HelpRecord[] findByClass(FindByClass msg) throws Exception {
        List<HelpRecord> records = new ArrayList<>();
        try {
            List<Classeslist> resultList = DatabaseManager.findClasses(msg.getClassName(), msg.getMimeType());
            List<Helps> list = new ArrayList<>();
            for (Classeslist key : resultList) {
                list.addAll(key.getHelpsList());
            }
            createHelps(list, records);
        } catch (Exception e) {
        }
        return records.toArray(new HelpRecord[records.size()]);
    }

    public static Status createHelpRecord(CreateHelpRecord msg) {

        if (DatabaseManager.helpExist(msg.getCode())) {
            return (new Status(false));
        } else {
            try {
                Mimetype mime = DatabaseManager.findOrCreateMimeType(msg.getMimeType());
                List<Keyslist> keys = DatabaseManager.findOrCereateKeys(msg.getKeys());
                List<Classeslist> classes = DatabaseManager.findOrCereateClasses(msg.getClasses());
                Users user = DatabaseManager.findUser("local@local.loc");
                DatabaseManager.addHelp(keys, classes, msg.getCode(), msg.getDescription(), mime, user);
                return (new Status(true));
            } catch (Exception e) {
                return (new Status(false));
            }
        }

    }

    public static Status editHelpRecord(EditHelpRecord msg) {

        try {
            Mimetype mime = DatabaseManager.findOrCreateMimeType(msg.getMimeType());
            List<Keyslist> keys = DatabaseManager.findOrCereateKeys(msg.getKeys());
            List<Classeslist> classes = DatabaseManager.findOrCereateClasses(msg.getClasses());
            Users user = DatabaseManager.findUser("local@local.loc");
            return (new Status(DatabaseManager.editHelp(msg.getId(), keys, classes, msg.getCode(), msg.getDescription(), mime, user)));
        } catch (Exception e) {
            e.printStackTrace();
            return (new Status(false));
        }

    }

    public static Status deleteHelpRecord(DeleteSnippet msg) {
        return new Status(DatabaseManager.deleteLocalHelp(msg.getSnippetId()));
    }

    public static HelpRecord[] findFullTextCode(FindFullTextCode msg) {
        List<HelpRecord> records = new ArrayList<>();
        try {
            Integer[] findHelp = LuceneManager.findHelp(msg.getText(), msg.getMaxRecords());
            List<Helps> helps = DatabaseManager.findHelpsById(findHelp, msg.getMimeType());
            createHelps(helps, records);
        } catch (Exception e) {
        }
        return records.toArray(new HelpRecord[records.size()]);

    }

    public static HelpRecord[] findFullTextDescription(FindFullTextDescription msg) {
        List<HelpRecord> records = new ArrayList<>();
        try {
            Integer[] findHelp = LuceneManager.findDescription(msg.getText(), msg.getMaxRecords());
            List<Helps> helps = DatabaseManager.findHelpsById(findHelp, msg.getMimeType());
            createHelps(helps, records);
        } catch (Exception e) {
        }
        return records.toArray(new HelpRecord[records.size()]);
    }

    private static void createHelps(List<Helps> helps, List<HelpRecord> records) {
        for (Helps help : helps) {
            int rank = 5;
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
    }

    public static void restore(String absolutePath) {
        DatabaseManager.restore(absolutePath);
    }

    public static MimeRecord[] getMimeTypes(GetMimeTypes msg) {
        Mimetype[] allMimeTypes = DatabaseManager.allMimeTypes();
        List<MimeRecord> records = new ArrayList<>();
        for (Mimetype mime : allMimeTypes) {
            records.add(new MimeRecord(mime.getMimetype(), mime.getDescription()));
        }
        return records.toArray(new MimeRecord[records.size()]);
    }

    public static DescriptionRecord[] getDescriptions(GetDescriptions msg) {
        List<Helps> helps = DatabaseManager.findHelpsByMimeType(msg.getMimeType());
        List<DescriptionRecord> records = new ArrayList<>();
        for (Helps help : helps) {
            records.add(new DescriptionRecord(help.getIdhelps(), help.getDescription(), help.getUser().getEmail()));
        }
        return records.toArray(new DescriptionRecord[records.size()]);
    }

    public static Object getSingleHelpRecord(GetSingleHelpRecord msg) {
        try {
            Helps help = DatabaseManager.getHelpById(msg.getId());
            if (help == null) {
                return (new Status(false));
            } else {
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
                HelpRecord rec = (new HelpRecord(help.getIdhelps(), help.getCreateddate(), help.getUser().getFirst() + " " + help.getUser().getLast(), help.getHelp(), rank, keys.toArray(new String[keys.size()]), cls.toArray(new String[cls.size()]), help.getDescription()));
                return (rec);
            }
        } catch (Exception e) {
            return (new Status(false));
        }
    }

}
