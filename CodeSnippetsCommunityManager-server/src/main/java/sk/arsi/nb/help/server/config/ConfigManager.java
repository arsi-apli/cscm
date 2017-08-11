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
package sk.arsi.nb.help.server.config;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.ini4j.Ini;
import org.ini4j.Profile;

/**
 *
 * @author arsi
 */
public class ConfigManager {

    public static final String SMTP_SERVER = "SERVER";
    public static final String SMTP_USER = "USER";
    public static final String SMTP_PASSWORD = "PASSWORD";
    public static final String SMTP_FROM = "MAIL_ADDRESS";
    public static final String SMTP_ENABLE = "ENABLE";
    public static final String SERVER_PORT = "PORT";
    public static final String SERVER_TURN_OFF_AUTHENTICATION = "TURN_OFF_AUTHENTICATION";
    public static final String ADMIN_ENABLE = "ENABLE";
    public static final String ADMIN_MAIL = "MAIL";
    public static final String ADMIN_ADD_USER = "MAIL_ON_USER_ADD";
    public static final String ADMIN_ADD_HELP = "MAIL_ON_HELP_ADD";
    public static final String ADMIN_SEND_DAILY_STATISTICS = "SEND_DAILY_STATISTICS";
    public static final String DATABASE_DIRECTORY = "DIRECTORY";

    public static final String BACKUP_ENABLE_PUBLIC_BACKUP = "ENABLE_PUBLIC_BACKUP";
    public static final String BACKUP_BACKUP_PATH = "BACKUP_PATH";
    public static final String BACKUP_SFTP_HOST = "SFTP_HOST";
    public static final String BACKUP_SFTP_USER = "SFTP_USER";
    public static final String BACKUP_SFTP_PASSWORD = "SFTP_PASSWORD";
    public static final String BACKUP_SFTP_DIRECTORY = "SFTP_DIRECTORY";

    public static Profile.Section SMTP;
    public static Profile.Section SERVER;
    public static Profile.Section ADMIN;
    public static Profile.Section DATABASE;
    public static Profile.Section BACKUP;

    public static final void loadConfig(String path) throws IOException {
        Ini ini = new Ini();
        ini.getConfig().setMultiSection(true);
        ini.load(new File(path));
        Set<Map.Entry<String, Profile.Section>> sections = ini.entrySet();
        for (Map.Entry<String, Profile.Section> e : sections) {
            if ("SMTP".equalsIgnoreCase(e.getKey())) {
                SMTP = e.getValue();
            } else if ("SERVER".equalsIgnoreCase(e.getKey())) {
                SERVER = e.getValue();
            } else if ("ADMIN".equalsIgnoreCase(e.getKey())) {
                ADMIN = e.getValue();
            } else if ("DATABASE".equalsIgnoreCase(e.getKey())) {
                DATABASE = e.getValue();
            } else if ("BACKUP".equalsIgnoreCase(e.getKey())) {
                BACKUP = e.getValue();
            }
        }
    }

}
