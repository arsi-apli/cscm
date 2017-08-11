/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.nb.help.server.backup;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ini4j.Ini;
import org.ini4j.Profile;
import sk.arsi.nb.help.server.DatabaseManager;
import sk.arsi.nb.help.server.config.ConfigManager;
import sk.arsi.nb.help.server.db.Classeslist;
import sk.arsi.nb.help.server.db.Helps;
import sk.arsi.nb.help.server.db.Keyslist;
import sk.arsi.nb.help.server.db.Mimetype;

/**
 *
 * @author arsi
 */
public class PublicDatabaseBackup {

    private static final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
    private static final ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

    public static void startPublicBackup() {
        Boolean enable = ConfigManager.BACKUP.get(ConfigManager.BACKUP_ENABLE_PUBLIC_BACKUP, Boolean.TYPE);
        if (enable) {
            createPublicBackup();
            ZoneId z = ZoneId.systemDefault();
            ZonedDateTime now = ZonedDateTime.now(z);
            LocalDate tomorrow = now.toLocalDate().plusDays(1);
            ZonedDateTime tomorrowStart = tomorrow.atStartOfDay(z);
            Duration duration = Duration.between(now, tomorrowStart);
            long millisecondsUntilTomorrow = duration.toMillis();
            pool.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    createPublicBackup();
                }
            }, millisecondsUntilTomorrow, 86400000, TimeUnit.MILLISECONDS);
        }
    }

    private static void createPublicBackup() {
        Ini ini = new Ini();
        ini.getConfig().setMultiSection(true);
        ini.getConfig().setEscape(true);
        ini.getConfig().setEscapeNewline(true);
        Mimetype[] allMimeTypes = DatabaseManager.allMimeTypes();
        for (Mimetype mime : allMimeTypes) {
            List<Helps> helps = DatabaseManager.findHelpsByMimeType(mime.getMimetype());
            for (Helps help : helps) {
                Profile.Section section = ini.add("Record");
                section.add("MimeType", help.getMimetype().getMimetype());
                section.add("User", help.getUser().getEmail());
                section.add("Description", help.getDescription());
                List<Keyslist> keyslistList = help.getKeyslistList();
                StringJoiner joiner = new StringJoiner(";");
                for (Keyslist k : keyslistList) {
                    joiner.add(k.getKeyname());
                }
                section.add("Keys", joiner.toString());
                List<Classeslist> classeslistList = help.getClasseslistList();
                joiner = new StringJoiner(";");
                for (Classeslist c : classeslistList) {
                    joiner.add(c.getClassname());
                }
                section.add("Classes", joiner.toString());
                section.add("Created", df.format(help.getCreateddate()));
                section.add("Code", help.getHelp());
            }
        }
        try {
            File file = new File(ConfigManager.BACKUP.get(ConfigManager.BACKUP_BACKUP_PATH));
            ini.store(file);
            storeToSftp(file);
        } catch (IOException ex) {
            Logger.getLogger(PublicDatabaseBackup.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void storeToSftp(File file) {
        System.out.println("public backup");
        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(ConfigManager.BACKUP.get(ConfigManager.BACKUP_SFTP_USER), ConfigManager.BACKUP.get(ConfigManager.BACKUP_SFTP_HOST));
            session.setPassword(ConfigManager.BACKUP.get(ConfigManager.BACKUP_SFTP_PASSWORD));
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            channelSftp.cd(ConfigManager.BACKUP.get(ConfigManager.BACKUP_SFTP_DIRECTORY));
            channelSftp.put(new FileInputStream(file), file.getName(), ChannelSftp.OVERWRITE);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            if (channelSftp != null) {
                channelSftp.exit();
            }
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }
}
