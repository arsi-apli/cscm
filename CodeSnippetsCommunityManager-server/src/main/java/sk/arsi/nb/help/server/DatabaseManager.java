/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.nb.help.server;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.eclipse.persistence.config.CacheType;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.queries.CursoredStream;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.h2.tools.Restore;
import sk.arsi.nb.help.server.config.ConfigManager;
import sk.arsi.nb.help.server.db.Classeslist;
import sk.arsi.nb.help.server.db.Helps;
import sk.arsi.nb.help.server.db.Keyslist;
import sk.arsi.nb.help.server.db.Mimetype;
import sk.arsi.nb.help.server.db.Ranks;
import sk.arsi.nb.help.server.db.Users;
import sk.arsi.nb.help.server.lucene.LuceneManager;
import sk.arsi.nb.help.transfer.AddRank;

/**
 *
 * @author arsi
 */
public class DatabaseManager {

    private static EntityManagerFactory factory;
    private static final ScheduledExecutorService schedulerPool = Executors.newScheduledThreadPool(1);
    private static boolean local = true;
    public static final String RESTORE = "sk.arsi.nb.help.server.restore";
    private static String databaseDirectoryPath = null;

    public static final void startDatabase(String databaseDirectory) {
        databaseDirectoryPath = databaseDirectory;
        Properties dbProps = new Properties();
        dbProps.put(PersistenceUnitProperties.LOGGING_LEVEL, "Fine");
        dbProps.put(PersistenceUnitProperties.LOGGING_EXCEPTIONS, "false");
        dbProps.put(PersistenceUnitProperties.CACHE_TYPE_DEFAULT, CacheType.Weak);
        dbProps.put(PersistenceUnitProperties.JDBC_CONNECTIONS_WAIT, "1");
        dbProps.put(PersistenceUnitProperties.JDBC_CONNECTIONS_INITIAL, "1");
        if (!databaseDirectory.endsWith("\\") && !databaseDirectory.endsWith("/")) {
            databaseDirectory = databaseDirectory + File.separator + "codesnippetsDB";
        } else {
            databaseDirectory = databaseDirectory + "codesnippetsDB";
        }
        dbProps.put(PersistenceUnitProperties.JDBC_URL, "jdbc:h2:" + databaseDirectory + ";TRACE_LEVEL_FILE=0");
        dbProps.put(PersistenceUnitProperties.JDBC_USER, "sa");
        dbProps.put(PersistenceUnitProperties.JDBC_PASSWORD, "");
        factory = Persistence.createEntityManagerFactory("nbHelp_jar_01.00PU", dbProps);
    }

    /**
     * Backup database for local NB client is caled from options, no other
     * access for server do it manualy
     *
     * @param sqlPath
     */
    public static void backup(String sqlPath) {
        JpaEntityManager manager = findManager();
        EntityTransaction transaction = manager.getTransaction();
        if (!transaction.isActive()) {
            transaction.begin();
        }
        Query naQuery = manager.createNativeQuery(String.format("BACKUP TO '%s'", sqlPath));
        naQuery.executeUpdate();
        transaction.commit();
    }

    public static void generateNewIndex() {
        LuceneManager.clearAll();
        ReadAllQuery query = new ReadAllQuery(Helps.class);
        query.useCursoredStream();
        CursoredStream stream = (CursoredStream) findManager().getSession().executeQuery(query);
        while (!stream.atEnd()) {
            Helps help = (Helps) stream.read();
            LuceneManager.addDescription(help.getIdhelps(), help.getDescription());
            LuceneManager.addHelp(help.getIdhelps(), help.getHelp());
            stream.releasePrevious();
        }
        stream.close();
    }

    public static Mimetype[] allMimeTypes() {
        JpaEntityManager manager = findManager();
        ReadAllQuery query = new ReadAllQuery(Mimetype.class);
        Vector<Mimetype> mimeTypes = (Vector<Mimetype>) manager.getSession().executeQuery(query);
        return mimeTypes.toArray(new Mimetype[mimeTypes.size()]);
    }

    public static void restore(String sqlPath) {
        if (local) {
            EntityTransaction transaction = localManager.getTransaction();
            if (!transaction.isActive()) {
                transaction.begin();
            }
            try {
                Query naQuery = localManager.createNativeQuery("SHUTDOWN");
                naQuery.executeUpdate();
                transaction.commit();
            } catch (Exception e) {
            }
            localManager.close();
            factory.close();
            localManager = null;
            Restore.execute(sqlPath, databaseDirectoryPath, "codesnippetsDB");
            startDatabase(databaseDirectoryPath);
            generateNewIndex();
        }
    }

    public static final void startDatabase() {
        local = false;
        startDatabase((String) ConfigManager.DATABASE.get(ConfigManager.DATABASE_DIRECTORY));
    }

    private static JpaEntityManager localManager;

    private static JpaEntityManager findManager() {
        if (local) {
            if (localManager == null) {
                localManager = (JpaEntityManager) factory.createEntityManager();
                Users user = localManager.find(Users.class, "local@local.loc");
                if (user == null) {
                    EntityTransaction transaction = localManager.getTransaction();
                    if (!transaction.isActive()) {
                        transaction.begin();
                    }
                    user = new Users("local@local.loc");
                    localManager.persist(user);
                    user.setFirst("local");
                    user.setAllow(true);
                    user.setLast("");
                    localManager.flush();
                    transaction.commit();
                    localManager.refresh(user);

                }
            }
            return localManager;
        } else {
            return THREAD_LOCAL.get().getManager();
        }
    }

    public static final ThreadLocal<EntityManagerLocalStore> THREAD_LOCAL = new ThreadLocal<EntityManagerLocalStore>() {
        @Override
        protected EntityManagerLocalStore initialValue() {
            return new EntityManagerLocalStore(factory, schedulerPool);
        }
    };

    public static void createMimeIfNotExist(String mimeType) {
        try {
            JpaEntityManager manager = findManager();
            Mimetype type = manager.find(Mimetype.class, mimeType);
            if (type == null) {
                EntityTransaction transaction = manager.getTransaction();
                if (!transaction.isActive()) {
                    transaction.begin();
                }

                Mimetype mime = new Mimetype(mimeType);
                if (mimeType.contains("-")) {
                    mime.setDescription(mimeType.substring(mimeType.lastIndexOf('-') + 1));
                } else {
                    mime.setDescription(mimeType.substring(mimeType.lastIndexOf('/') + 1));
                }
                manager.persist(mime);
                manager.flush();
                transaction.commit();
                manager.refresh(mime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Mimetype findOrCreateMimeType(String mimeType) {
        JpaEntityManager manager = DatabaseManager.findManager();
        Mimetype find = manager.find(Mimetype.class, mimeType);
        if (find == null) {
            EntityTransaction transaction = manager.getTransaction();
            if (!transaction.isActive()) {
                transaction.begin();
            }
            find = new Mimetype(mimeType);
            manager.persist(find);
            manager.flush();
            transaction.commit();
            manager.refresh(find);
        }
        return find;
    }

    public static Users findUser(String email) {
        JpaEntityManager manager = DatabaseManager.findManager();
        return manager.find(Users.class, email);
    }

    public static List<Classeslist> findOrCereateClasses(String[] classes) {
        JpaEntityManager manager = DatabaseManager.findManager();
        List<Classeslist> tmp = new ArrayList<>();
        for (String cls : classes) {
            Classeslist find = manager.find(Classeslist.class, cls);
            if (find == null) {
                find = new Classeslist(cls);
            }
            tmp.add(find);
        }
        return tmp;
    }

    public static List<Keyslist> findOrCereateKeys(String[] keys) {
        JpaEntityManager manager = DatabaseManager.findManager();
        List<Keyslist> tmp = new ArrayList<>();
        for (String cls : keys) {
            Keyslist find = manager.find(Keyslist.class, cls);
            if (find == null) {
                find = new Keyslist(cls);
            }
            tmp.add(find);
        }
        return tmp;
    }

    public static void addHelp(List<Keyslist> keys, List<Classeslist> classes, String code, String description, Mimetype mimetype, Users user) {
        JpaEntityManager manager = DatabaseManager.findManager();
        EntityTransaction transaction = manager.getTransaction();
        if (!transaction.isActive()) {
            transaction.begin();
        }
        Helps tmp = new Helps();
        manager.persist(tmp);
        tmp.getClasseslistList().addAll(classes);
        tmp.getKeyslistList().addAll(keys);
        tmp.setCreateddate(new Date());
        tmp.setHelp(code);
        tmp.setUser(user);
        tmp.setMimetype(mimetype);
        tmp.setDescription(description);
        manager.flush();
        transaction.commit();
        manager.refresh(tmp);
    }

    public static void addRankToDb(AddRank msg, Helps help) {
        JpaEntityManager manager = DatabaseManager.findManager();
        EntityTransaction transaction = manager.getTransaction();
        if (!transaction.isActive()) {
            transaction.begin();
        }
        Ranks rank = new Ranks();
        rank.setHelpsIdhelps(help);
        rank.setUser((msg).getUser());
        rank.setRank((msg).getRank());
        manager.persist(rank);
        manager.flush();
        transaction.commit();
        manager.refresh(rank);
        manager.refresh(help);
        manager.detach(help);
    }

    public static Helps findHelpById(long id) {
        return DatabaseManager.THREAD_LOCAL.get().getManager().find(Helps.class, (int) id);
    }

    public static boolean rankExist(long helpId, String user) {
        JpaEntityManager manager = DatabaseManager.findManager();
        Query query1 = manager.createNativeQuery("SELECT * FROM help.RANKS where helps_idhelps='" + helpId + "' and USER='" + user + "';", Ranks.class);
        return !query1.getResultList().isEmpty();
    }

    public static List<Keyslist> findKeys(String key, String mimeType) {
        JpaEntityManager manager = DatabaseManager.findManager();
        Query query = manager.createNativeQuery("SELECT * FROM KEYSLIST m\n"
                + "inner join KEYSLIST_has_HELPS am on m.KEYNAME = am.KEYSLIST_KEYNAME\n"
                + "inner join HELPS hl on am.HELPS_IDHELPS = hl.IDHELPS where m.KEYNAME like '" + key + "%' and hl.MIMETYPE_MIMETYPE like '" + mimeType + "'", Keyslist.class);
        return query.getResultList();
    }

    public static List<Classeslist> findClasses(String className, String mimeType) {
        JpaEntityManager manager = DatabaseManager.findManager();
        Query query = manager.createNativeQuery("SELECT * FROM CLASSESLIST m\n"
                + "inner join CLASSESLIST_has_HELPS am on m.CLASSNAME = am.CLASSESLIST_CLASSNAME\n"
                + "inner join HELPS hl on am.HELPS_IDHELPS = hl.IDHELPS where m.CLASSNAME like '" + className + "%' and hl.MIMETYPE_MIMETYPE like '" + mimeType + "'", Classeslist.class);
        return query.getResultList();
    }

    public static int computeRankForHelp(long id) {
        JpaEntityManager manager = DatabaseManager.findManager();
        Query query = manager.createNativeQuery("select helps_idhelps,AVG(RANK) from RANKS where helps_idhelps=" + id + " group by helps_idhelps");
        Object[] singleResult = null;
        try {
            singleResult = (Object[]) query.getSingleResult();
        } catch (Exception e) {
        }
        if (singleResult != null && singleResult.length != 0) {
            return ((BigDecimal) singleResult[1]).intValue();
        } else {
            return 0;
        }
    }

    public static boolean helpExist(String code) {
        JpaEntityManager manager = DatabaseManager.findManager();
        TypedQuery<Helps> query = manager.createNamedQuery("Helps.findByHelp", Helps.class);
        query.setParameter("help", code);
        return !query.getResultList().isEmpty();
    }

    public static void addUser(String email, String first, String last, String password) {
        JpaEntityManager manager = DatabaseManager.findManager();
        EntityTransaction transaction = manager.getTransaction();
        if (!transaction.isActive()) {
            transaction.begin();
        }
        Users tmp = new Users(email);
        tmp.setAllow(true);
        tmp.setFirst(first);
        tmp.setLast(last);
        tmp.setPassword(password);
        manager.persist(tmp);
        manager.flush();
        transaction.commit();
        manager.refresh(tmp);

    }

    public static List<Helps> findHelpsById(Integer ids[], String mime) {
        JpaEntityManager manager = DatabaseManager.findManager();
        if (ids.length == 0) {
            return new ArrayList<>();
        }
        String sql = "SELECT * FROM HELPS h where ( h.IDHELPS='" + ids[0] + "'";
        for (int i = 1; i < ids.length; i++) {
            Integer id = ids[i];
            sql += " or IDHELPS='" + id + "'";
        }
        sql += ") and h.MIMETYPE_MIMETYPE='" + mime + "' ;";
        Query query = manager.createNativeQuery(sql, Helps.class);
        return query.getResultList();
    }

    public static List<Helps> findHelpsByMimeType(String mime) {
        JpaEntityManager manager = DatabaseManager.findManager();
        String sql = "SELECT * FROM HELPS h where  h.MIMETYPE_MIMETYPE='" + mime + "' ;";
        Query query = manager.createNativeQuery(sql, Helps.class);
        return query.getResultList();
    }

    public static Helps getHelpById(int id) {
        JpaEntityManager manager = DatabaseManager.findManager();
        return manager.find(Helps.class, id);
    }

}
