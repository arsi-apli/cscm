/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.nb.help.server.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import sk.arsi.nb.help.server.Main;

/**
 *
 * @author arsi
 */
public class LuceneManager {

    private static final String HELP_INDEX_DIR = "help_indexes";
    private static final String DESCRIPTION_INDEX_DIR = "description_indexes";
    private static IndexWriter HELP_WRITER;
    private static IndexWriter DESCRIPTION_WRITER;
    private static boolean local = true;

    public static final void openLucene() throws IOException {
        local = false;
        openLucene(new File(""));
    }

    private static DescriptionLocalStore descriptionLocalStore;
    private static HelpLocalStore helpLocalStore;

    private static DescriptionLocalStore findDescriptionLocalStore() {
        if (local) {
            if (descriptionLocalStore == null) {
                descriptionLocalStore = new DescriptionLocalStore();
            }
            return descriptionLocalStore;
        } else {
            return descriptionThreadLocal.get();
        }
    }

    private static HelpLocalStore findHelpLocalStore() {
        if (local) {
            if (helpLocalStore == null) {
                helpLocalStore = new HelpLocalStore();
            }
            return helpLocalStore;
        } else {
            return helpThreadLocal.get();
        }
    }

    public static final void openLucene(File homeDirectory) throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        File indexes = new File(homeDirectory.getAbsolutePath() + File.separator + HELP_INDEX_DIR);
        indexes.mkdirs();
        HELP_WRITER = new IndexWriter(FSDirectory.open(indexes.toPath()), config);
        StandardAnalyzer analyzer1 = new StandardAnalyzer();
        IndexWriterConfig config1 = new IndexWriterConfig(analyzer1);
        config1.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        File indexes1 = new File(homeDirectory.getAbsolutePath() + File.separator + DESCRIPTION_INDEX_DIR);
        indexes1.mkdirs();
        DESCRIPTION_WRITER = new IndexWriter(FSDirectory.open(indexes1.toPath()), config1);
        //
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HELP_WRITER.commit();
                    HELP_WRITER.close();
                    DESCRIPTION_WRITER.commit();
                    DESCRIPTION_WRITER.close();

                } catch (IOException ex) {
                    Logger.getLogger(LuceneManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }));
    }

    public static final void addHelp(int index, String code) {
        Document doc = new Document();
        doc.add(new TextField("help", code, Field.Store.NO));
        doc.add(new StoredField("id", index));
        try {
            HELP_WRITER.addDocument(doc);
            HELP_WRITER.commit();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static final void removeHelp(int index, String code) {
        HelpLocalStore localStore = findHelpLocalStore();
        localStore.updateIndex();
        Document doc = new Document();
        doc.add(new TextField("help", code, Field.Store.NO));
        doc.add(new StoredField("id", index));
        try {
            QueryParser parser = new QueryParser("id", localStore.HELP_ANALYZER);
            HELP_WRITER.deleteDocuments(parser.parse("" + index));
            HELP_WRITER.commit();
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static final void updateHelp(int index, String code) {
        removeHelp(index, code);
        addHelp(index, code);
    }

    public static final void addDescription(int index, String description) {
        Document doc = new Document();
        doc.add(new TextField("description", description, Field.Store.NO));
        doc.add(new StoredField("id", index));
        try {
            DESCRIPTION_WRITER.addDocument(doc);
            DESCRIPTION_WRITER.commit();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static final void removeDescription(int index, String description) {
        DescriptionLocalStore localStore = findDescriptionLocalStore();
        localStore.updateIndex();
        Document doc = new Document();
        doc.add(new TextField("description", description, Field.Store.NO));
        doc.add(new StoredField("id", index));
        try {
            QueryParser parser = new QueryParser("id", localStore.DESCRIPTION_ANALYZER);
            DESCRIPTION_WRITER.deleteDocuments(parser.parse("" + index));
            DESCRIPTION_WRITER.commit();
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static final void updateDescription(int index, String description) {
        removeDescription(index, description);
        addDescription(index, description);
    }

    private static class HelpLocalStore {

        private IndexSearcher HELP_SEARCHER;
        private StandardAnalyzer HELP_ANALYZER;
        private DirectoryReader HELP_READER;

        public HelpLocalStore() {
            try {
                HELP_READER = DirectoryReader.open(HELP_WRITER);
                HELP_SEARCHER = new IndexSearcher(HELP_READER);
                HELP_ANALYZER = new StandardAnalyzer();
            } catch (IOException ex) {
                Logger.getLogger(LuceneManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void updateIndex() {
            try {
                DirectoryReader instance = DirectoryReader.openIfChanged(HELP_READER);
                if (instance != null) {
                    HELP_READER.close();
                    HELP_READER = instance;
                    HELP_SEARCHER = new IndexSearcher(HELP_READER);
                    HELP_ANALYZER = new StandardAnalyzer();
                }
            } catch (IOException ex) {
                Logger.getLogger(LuceneManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static class DescriptionLocalStore {

        private IndexSearcher DESCRIPTION_SEARCHER;
        private StandardAnalyzer DESCRIPTION_ANALYZER;
        private DirectoryReader DESCRIPTION_READER;

        public DescriptionLocalStore() {
            try {
                DESCRIPTION_READER = DirectoryReader.open(DESCRIPTION_WRITER);
                DESCRIPTION_SEARCHER = new IndexSearcher(DESCRIPTION_READER);
                DESCRIPTION_ANALYZER = new StandardAnalyzer();
            } catch (IOException ex) {
                Logger.getLogger(LuceneManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void updateIndex() {
            try {
                DirectoryReader instance = DirectoryReader.openIfChanged(DESCRIPTION_READER);
                if (instance != null) {
                    DESCRIPTION_READER.close();
                    DESCRIPTION_READER = instance;
                    DESCRIPTION_SEARCHER = new IndexSearcher(DESCRIPTION_READER);
                    DESCRIPTION_ANALYZER = new StandardAnalyzer();
                }
            } catch (IOException ex) {
                Logger.getLogger(LuceneManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static final ThreadLocal<HelpLocalStore> helpThreadLocal = new ThreadLocal<HelpLocalStore>() {
        @Override
        protected HelpLocalStore initialValue() {
            return new HelpLocalStore();
        }
    };
    private static final ThreadLocal<DescriptionLocalStore> descriptionThreadLocal = new ThreadLocal<DescriptionLocalStore>() {
        @Override
        protected DescriptionLocalStore initialValue() {
            return new DescriptionLocalStore();
        }
    };

    public static final Integer[] findDescription(String search, int maxResults) {
        try {
            DescriptionLocalStore localStore = findDescriptionLocalStore();
            localStore.updateIndex();
            QueryParser parser = new QueryParser("description", localStore.DESCRIPTION_ANALYZER);
            TopDocs result = localStore.DESCRIPTION_SEARCHER.search(parser.parse(QueryParser.escape(search).replace("NOT", "\\NOT")), maxResults);
            List<Integer> indexes = new ArrayList<>();
            for (ScoreDoc scoreDoc : result.scoreDocs) {
                Document d = localStore.DESCRIPTION_SEARCHER.doc(scoreDoc.doc);

                indexes.add(((StoredField) d.getField("id")).numericValue().intValue());
            }
            return indexes.toArray(new Integer[indexes.size()]);
        } catch (Exception ex) {
            Logger.getLogger(LuceneManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new Integer[0];
    }

    public static final Integer[] findHelp(String search, int maxResults) {
        try {
            HelpLocalStore localStore = findHelpLocalStore();
            localStore.updateIndex();
            QueryParser parser = new QueryParser("help", localStore.HELP_ANALYZER);
            TopDocs result = localStore.HELP_SEARCHER.search(parser.parse(QueryParser.escape(search).replace("NOT", "\\NOT")), maxResults);
            List<Integer> indexes = new ArrayList<>();
            for (ScoreDoc scoreDoc : result.scoreDocs) {
                Document d = localStore.HELP_SEARCHER.doc(scoreDoc.doc);
                indexes.add(((StoredField) d.getField("id")).numericValue().intValue());
            }
            return indexes.toArray(new Integer[indexes.size()]);
        } catch (Exception ex) {
            Logger.getLogger(LuceneManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new Integer[0];
    }

}
