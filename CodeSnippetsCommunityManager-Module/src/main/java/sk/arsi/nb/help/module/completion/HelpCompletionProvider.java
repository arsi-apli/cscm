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
package sk.arsi.nb.help.module.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.completion.GlobalCompletionProvider;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import sk.arsi.nb.help.module.actions.SearchSelectionToolbar;
import sk.arsi.nb.help.module.actions.SearchSelector;
import sk.arsi.nb.help.module.actions.ServerSelectionToolbar;
import sk.arsi.nb.help.module.actions.ServerSelector;
import sk.arsi.nb.help.module.client.CommunitydocPanel;
import static sk.arsi.nb.help.module.client.CommunitydocPanel.SECOND_PRESS;
import sk.arsi.nb.help.module.client.NbDocClient;
import sk.arsi.nb.help.module.client.ServerType;
import sk.arsi.nb.help.transfer.HelpRecord;

/**
 *
 * @author Arsi
 */
@ServiceProvider(service = GlobalCompletionProvider.class)
public final class HelpCompletionProvider implements GlobalCompletionProvider {

    public static final String FULLTEXTSEARCH = "FULLTEXTSEARCH";

    @Override
    public CompletionTask createTask(int type, JTextComponent component) {
        boolean secondPr = NbPreferences.forModule(CommunitydocPanel.class).getBoolean(SECOND_PRESS, true);
        if (secondPr) {
            if (type != CompletionProvider.COMPLETION_ALL_QUERY_TYPE) {
                return null;
            }
        } else if (type != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }

        return new AsyncCompletionTask(new Query(), component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    private static final class Query extends AsyncCompletionQuery
            implements ChangeListener {

        private JTextComponent component;

        private int queryAnchorOffset;

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                String langPath = null;
                String identifierBeforeCursor = null;
                if (doc instanceof AbstractDocument) {
                    AbstractDocument adoc = (AbstractDocument) doc;
                    adoc.readLock();
                    try {
                        try {
                            if (adoc instanceof BaseDocument) {
                                identifierBeforeCursor = Utilities.getIdentifierBefore((BaseDocument) adoc, caretOffset);
                            }
                        } catch (BadLocationException e) {
                            // leave identifierBeforeCursor null
                        }
                        List<TokenSequence<?>> list = TokenHierarchy.get(doc).embeddedTokenSequences(caretOffset, true);
                        if (list.size() > 1) {
                            langPath = list.get(list.size() - 1).languagePath().mimePath();
                        }
                    } finally {
                        adoc.readUnlock();
                    }
                }

                if (identifierBeforeCursor == null) {
                    identifierBeforeCursor = ""; //NOI18N
                }

                if (langPath == null) {
                    langPath = NbEditorUtilities.getMimeType(doc);
                }

                queryAnchorOffset = caretOffset - identifierBeforeCursor.length();
                if (langPath != null && identifierBeforeCursor.length() > 1) {//min 2 chars
                    String mimeType = DocumentUtilities.getMimeType(component);
                    MimePath mimePath = mimeType == null ? MimePath.EMPTY : MimePath.get(mimeType);
                    Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
                    String teamServer = NbPreferences.forModule(CommunitydocPanel.class).get(NbDocClient.TEAM_SERVER, "");
                    String masterServer = NbPreferences.forModule(CommunitydocPanel.class).get(NbDocClient.SERVER, "server.arsi.sk");
                    ServerSelector panel = ServerSelectionToolbar.findPanel(doc);
                    SearchSelector panelSearch = SearchSelectionToolbar.findPanel(doc);
                    //local  - key search
                    if (((panel == null || panel.isLocal()) && (panelSearch == null || !panelSearch.isFullText())) && !"".equals(identifierBeforeCursor)) {
                        // local  fulltext search
                        readByKey(identifierBeforeCursor, mimeType, resultSet, ServerType.LOCAL);
                    } else if ((panel == null || panel.isLocal()) && (panelSearch == null || panelSearch.isFullText())) {
                        readFullText(doc, caretOffset, mimeType, identifierBeforeCursor, resultSet, ServerType.LOCAL);

                    }
                    //team server - key search
                    if (((panel == null || panel.isTeam()) && (panelSearch == null || !panelSearch.isFullText())) && !"".equals(identifierBeforeCursor) && !"".equals(teamServer)) {
                        // team server fulltext search

                        readByKey(identifierBeforeCursor, mimeType, resultSet, ServerType.TEAM);
                    } else if ((panel == null || panel.isTeam()) && (panelSearch == null || panelSearch.isFullText())) {
                        readFullText(doc, caretOffset, mimeType, identifierBeforeCursor, resultSet, ServerType.TEAM);

                    }
                    if (((panel == null || panel.isGlobal()) && (panelSearch == null || !panelSearch.isFullText())) && !"".equals(identifierBeforeCursor) && !"".equals(masterServer)) {
                        readByKey(identifierBeforeCursor, mimeType, resultSet, ServerType.MASTER);
                    } else if ((panel == null || panel.isGlobal()) && (panelSearch == null || panelSearch.isFullText())) {
                        readFullText(doc, caretOffset, mimeType, identifierBeforeCursor, resultSet, ServerType.MASTER);
                    }

                }

                resultSet.setAnchorOffset(queryAnchorOffset);
            } catch (Exception e) {
            }
            resultSet.finish();
        }

        private void readFullText(Document doc, int caretOffset, String mimeType, String identifierBeforeCursor, CompletionResultSet resultSet, ServerType serverType) {
            String selectedText = component.getSelectedText();
            if (selectedText == null) {
                if (doc instanceof BaseDocument) {
                    //description search
                    ((AbstractDocument) doc).readLock();
                    int lineStart = 0;
                    int lineEnd = 0;
                    try {
                        lineStart = LineDocumentUtils.getLineFirstNonWhitespace((BaseDocument) doc, caretOffset);
                        lineEnd = LineDocumentUtils.getLineLastNonWhitespace((BaseDocument) doc, caretOffset);
                        if (lineStart != lineEnd) {
                            String text = doc.getText(lineStart, (lineEnd - lineStart) + 1);
                            Object helps = NbDocClient.getByFullTextDescription(text, serverType, mimeType);
                            addFullTextHelps(helps, text, resultSet, serverType);
                        }
                    } catch (Exception e) {
                    } finally {
                        ((AbstractDocument) doc).readUnlock();
                    }
                }

            } else {
                //code search
                Object helps = NbDocClient.getByFullTextCode(selectedText, serverType, mimeType);
                addFullTextHelps(helps, selectedText, resultSet, serverType);
            }
        }

        private void readByKey(String identifierBeforeCursor, String mimeType, CompletionResultSet resultSet, ServerType serverType) {
            //global server
            Object byKey = null;
            Object byClass = null;
            byKey = NbDocClient.getByKey(identifierBeforeCursor, serverType, mimeType);
            byClass = NbDocClient.getByClass(identifierBeforeCursor, serverType, mimeType);
            List<HelpCompletionItem> items = new ArrayList<>();
            if (byKey instanceof HelpRecord[]) {
                for (HelpRecord rec : ((HelpRecord[]) byKey)) {
                    HelpCompletionItem completionItem = new HelpCompletionItem(rec, identifierBeforeCursor, serverType);
                    if (!items.contains(completionItem)) {
                        items.add(completionItem);
                    }
                }
            }
            if (byClass instanceof HelpRecord[]) {
                for (HelpRecord rec : ((HelpRecord[]) byClass)) {
                    HelpCompletionItem completionItem = new HelpCompletionItem(rec, identifierBeforeCursor, serverType);
                    if (!items.contains(completionItem)) {
                        items.add(completionItem);
                    }
                }
            }
            Collections.sort(items, new Comparator<HelpCompletionItem>() {
                @Override
                public int compare(HelpCompletionItem o1, HelpCompletionItem o2) {
                    return Integer.compare(o2.getHelp().getRank(), o1.getHelp().getRank());
                }
            });
            resultSet.addAllItems(items);
        }

        private void addFullTextHelps(Object helps, String identifierBeforeCursor, CompletionResultSet resultSet, ServerType serverType) {
            if (helps instanceof HelpRecord[]) {
                List<HelpCompletionItem> itemsTeam = new ArrayList<>();
                for (HelpRecord rec : ((HelpRecord[]) helps)) {
                    HelpCompletionItem completionItem = new HelpCompletionItem(rec, identifierBeforeCursor, serverType);
                    if (!itemsTeam.contains(completionItem)) {
                        itemsTeam.add(completionItem);
                    }
                }
                Collections.sort(itemsTeam, new Comparator<HelpCompletionItem>() {
                    @Override
                    public int compare(HelpCompletionItem o1, HelpCompletionItem o2) {
                        return Integer.compare(o2.getHelp().getRank(), o1.getHelp().getRank());
                    }
                });
                resultSet.addAllItems(itemsTeam);
            }
        }

        @Override
        public void stateChanged(ChangeEvent evt) {
            synchronized (this) {
                notify();
            }
        }

    }

}
