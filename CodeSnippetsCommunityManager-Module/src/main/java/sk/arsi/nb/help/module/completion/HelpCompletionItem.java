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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.completion.RankProvider;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import sk.arsi.nb.help.module.actions.SearchSelectionToolbar;
import sk.arsi.nb.help.module.actions.SearchSelector;
import sk.arsi.nb.help.module.client.NbDocClient;
import sk.arsi.nb.help.module.client.ServerType;
import sk.arsi.nb.help.module.client.SnippetTools;
import sk.arsi.nb.help.transfer.HelpRecord;

/**
 *
 * @author arsi
 */
public class HelpCompletionItem implements CompletionItem {

    private final HelpRecord help;
    private final String searched;
    private static ImageIcon iconMaster;
    private static ImageIcon iconTeam;
    private static ImageIcon iconLocal;
    private final ServerType serverType;

    public HelpCompletionItem(HelpRecord help, String searched, ServerType serverType) {
        this.help = help;
        this.searched = searched;
        this.serverType = serverType;
    }

    public HelpRecord getHelp() {
        return help;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.help != null ? this.help.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HelpCompletionItem other = (HelpCompletionItem) obj;
        return !(this.help != other.help && (this.help == null || !this.help.equals(other.help)));
    }

    @Override
    public void defaultAction(JTextComponent component) {
        Completion.get().hideAll();
        // Remove the typed part
        Document doc = component.getDocument();
        int caretOffset = component.getSelectionStart();
        SearchSelector panelSearch = SearchSelectionToolbar.findPanel(doc);
        if (panelSearch == null || !panelSearch.isFullText()) {
            int prefixLength = 0;
            try {
                String ident = Utilities.getIdentifierBefore((BaseDocument) doc, caretOffset);
                if (ident != null) {
                    prefixLength = ident.length();
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (prefixLength > 0) {
                try {
                    // Remove the typed prefix
                    doc.remove(caretOffset - prefixLength, prefixLength);
                } catch (BadLocationException ble) {
                }
            }
        } else {
            try {
                String selectedText = component.getSelectedText();
                if (selectedText == null) {
                    //description search
                    int lineStart = LineDocumentUtils.getLineFirstNonWhitespace((BaseDocument) doc, caretOffset);
                    int lineEnd = LineDocumentUtils.getLineLastNonWhitespace((BaseDocument) doc, caretOffset);
                    if (lineStart != lineEnd) {
                        doc.remove(lineStart, (lineEnd - lineStart) + 1);
                    }
                } else {
                    //code search
                    int lineStart = component.getSelectionStart();
                    int lineEnd = component.getSelectionEnd();
                    if (lineStart != lineEnd) {
                        doc.remove(lineStart, (lineEnd - lineStart) + 1);
                    }
                }
            } catch (BadLocationException badLocationException) {
            }
        }
        try {
            doc.insertString(component.getCaretPosition(), help.getCode(), null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
    }

    private String getLeftText() {
        return help.getDescription();
    }

    private String getRightText() {
        switch (serverType) {
            case MASTER:
            case TEAM:
                return "[" + help.getRank() + "]";
            default:
                return "";
        }

    }

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftText(), getRightText(),
                g, defaultFont);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {

        if (iconMaster == null) {
            iconMaster = ImageUtilities.loadImageIcon("sk/arsi/nb/help/module/help_item.png", false); // NOI18N
        }
        if (iconTeam == null) {
            iconTeam = ImageUtilities.loadImageIcon("sk/arsi/nb/help/module/help_item_team.png", false); // NOI18N
        }
        if (iconLocal == null) {
            iconLocal = ImageUtilities.loadImageIcon("sk/arsi/nb/help/module/help_item_local.png", false); // NOI18N
        }
        switch (serverType) {
            case MASTER:
                CompletionUtilities.renderHtml(iconMaster, getLeftText(), getRightText(),
                        g, defaultFont, defaultColor, width, height, selected);
                break;
            case TEAM:
                CompletionUtilities.renderHtml(iconTeam, getLeftText(), getRightText(),
                        g, defaultFont, defaultColor, width, height, selected);
                break;
            case LOCAL:
                CompletionUtilities.renderHtml(iconLocal, getLeftText(), getRightText(),
                        g, defaultFont, defaultColor, width, height, selected);
                break;
            default:
                throw new AssertionError(serverType.name());

        }

    }

    @Override
    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new DocQuery());
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public boolean instantSubstitution(JTextComponent component) {
        return false;
    }

    @Override
    public int getSortPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public CharSequence getSortText() {
        return "";
    }

    @Override
    public CharSequence getInsertPrefix() {
        return "";
    }

    private class DocQuery extends AsyncCompletionQuery {

        public DocQuery() {
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            resultSet.setDocumentation(new DocItem(SnippetTools.snippetToHtml(help)));
            resultSet.finish();
        }
    }

    private final class DocItem implements CompletionDocumentation, RankProvider {

        private final String text;

        DocItem(String text) {
            this.text = text;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public CompletionDocumentation resolveLink(String link) {
            return null;
        }

        @Override
        public java.net.URL getURL() {
            return null;
        }

        @Override
        public javax.swing.Action getGotoSourceAction() {
            return null;
        }

        @Override
        public int getRank() {
            return help.getRank();
        }

        @Override
        public void setRank(int rank) {
            NbDocClient.addRank(help.getId(), rank, serverType);
        }

        @Override
        public long getHelpId() {
            return help.getId();
        }

        @Override
        public boolean isLocal() {
            switch (serverType) {
                case MASTER:
                case TEAM:
                    return false;
                default:
                    return true;

            }
        }
    }

}
