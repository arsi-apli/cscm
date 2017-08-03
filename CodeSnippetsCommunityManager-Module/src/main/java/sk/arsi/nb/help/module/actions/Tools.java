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
package sk.arsi.nb.help.module.actions;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.EditorUI;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author arsi
 */
public class Tools {

    public static JComponent[] createSingleLineEditor(String mimeType) {
        assert SwingUtilities.isEventDispatchThread() : "Utilities.createSingleLineEditor must be called from AWT thread only"; // NOI18N

        EditorKit kit = MimeLookup.getLookup(mimeType).lookup(EditorKit.class);
        if (kit == null) {
            throw new IllegalArgumentException("No EditorKit for '" + mimeType + "' mimetype."); //NOI18N
        }

        final JEditorPane editorPane = new JEditorPane();
//        editorPane.putClientProperty(
//                "HighlightsLayerExcludes", //NOI18N
//                ".*(?<!TextSelectionHighlighting)$" //NOI18N
//        );
//        editorPane.putClientProperty("AsTextField", Boolean.TRUE);
        editorPane.setEditorKit(kit);

        editorPane.setBorder(
                new EmptyBorder(0, 0, 0, 0)
        );

        JTextField referenceTextField = new JTextField("M"); //NOI18N

        final Insets margin = referenceTextField.getMargin();
        final Insets borderInsets = referenceTextField.getBorder().getBorderInsets(referenceTextField);
        //logger.fine("createSingleLineEditor(): margin = "+margin+", borderInsets = "+borderInsets);
        final JScrollPane sp = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {

            @Override
            public void setViewportView(Component view) {
                if (view instanceof JComponent) {
                    //logger.fine("createSingleLineEditor() setViewportView(): setting empty border with insets = "+borderInsets);
                    ((JComponent) view).setBorder(new EmptyBorder(margin)); // borderInsets
                }
                if (view instanceof JEditorPane) {
                    adjustScrollPaneSize(this, (JEditorPane) view);
                }
                super.setViewportView(view);
            }

        };
        editorPane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("editorKit".equals(evt.getPropertyName())) { // NOI18N
                    adjustScrollPaneSize(sp, editorPane);
                }
            }
        });

        sp.setBorder(new DelegatingBorder(referenceTextField.getBorder(), borderInsets));
        sp.setBackground(referenceTextField.getBackground());

        int preferredHeight = referenceTextField.getPreferredSize().height;
        Dimension spDim = sp.getPreferredSize();
        spDim.height = preferredHeight;
        spDim.height += margin.bottom + margin.top;//borderInsets.top + borderInsets.bottom;
        sp.setPreferredSize(spDim);
        sp.setMinimumSize(spDim);
        sp.setMaximumSize(spDim);

        sp.setViewportView(editorPane);

        final DocumentListener manageViewListener = new ManageViewPositionListener(editorPane, sp);
        DocumentUtilities.addDocumentListener(editorPane.getDocument(), manageViewListener, DocumentListenerPriority.AFTER_CARET_UPDATE);
        editorPane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("document".equals(evt.getPropertyName())) { // NOI18N
                    Document oldDoc = (Document) evt.getOldValue();
                    if (oldDoc != null) {
                        DocumentUtilities.removeDocumentListener(oldDoc, manageViewListener, DocumentListenerPriority.AFTER_CARET_UPDATE);
                    }
                    Document newDoc = (Document) evt.getNewValue();
                    if (newDoc != null) {
                        DocumentUtilities.addDocumentListener(newDoc, manageViewListener, DocumentListenerPriority.AFTER_CARET_UPDATE);
                    }
                }
            }
        });

        return new JComponent[]{sp, editorPane};
    }

    private static final class ManageViewPositionListener implements DocumentListener {

        private JEditorPane editorPane;
        private JScrollPane sp;

        public ManageViewPositionListener(JEditorPane editorPane, JScrollPane sp) {
            this.editorPane = editorPane;
            this.sp = sp;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            changed();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changed();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            changed();
        }

        private void changed() {
            JViewport viewport = sp.getViewport();
            Point viewPosition = viewport.getViewPosition();
            if (viewPosition.x > 0) {
                try {
                    Rectangle textRect = editorPane.getUI().modelToView(editorPane, editorPane.getDocument().getLength());
                    int textLength = textRect.x + textRect.width;
                    int viewLength = viewport.getExtentSize().width;
                    //System.out.println("Utilities.createSingleLineEditor(): spLength = "+sp.getSize().width+", viewLength = "+viewLength+", textLength = "+textLength+", viewPosition = "+viewPosition);
                    if (textLength < (viewPosition.x + viewLength)) {
                        viewPosition.x = Math.max(textLength - viewLength, 0);
                        viewport.setViewPosition(viewPosition);
                        //System.out.println("Utilities.createSingleLineEditor(): setting new view position = "+viewPosition);
                    }
                } catch (BadLocationException blex) {
                    Exceptions.printStackTrace(blex);
                }
            }
        }
    }

    private static void adjustScrollPaneSize(JScrollPane sp, JEditorPane editorPane) {
        int height;
        //logger.fine("createSingleLineEditor(): editorPane's margin = "+editorPane.getMargin());
        //logger.fine("createSingleLineEditor(): editorPane's insets = "+editorPane.getInsets());
        Dimension prefSize = sp.getPreferredSize();
        Insets borderInsets = sp.getBorder().getBorderInsets(sp);//sp.getInsets();
        int vBorder = borderInsets.bottom + borderInsets.top;
        EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(editorPane);
        if (eui != null) {
            height = eui.getLineHeight();
            if (height < eui.getLineAscent()) {
                height = (eui.getLineAscent() * 4) / 3; // Hack for the case when line height = 1
            }
        } else {
            java.awt.Font font = editorPane.getFont();
            java.awt.FontMetrics fontMetrics = editorPane.getFontMetrics(font);
            height = fontMetrics.getHeight();
            //logger.fine("createSingleLineEditor(): editor's font = "+font+" with metrics = "+fontMetrics+", leading = "+fontMetrics.getLeading());
            //logger.fine("createSingleLineEditor(): font's height = "+height);
        }
        height += vBorder + getLFHeightAdjustment();
        //height += 2; // 2 for border
        if (prefSize.height < height) {
            prefSize.height = height;
            sp.setPreferredSize(prefSize);
            sp.setMinimumSize(prefSize);
            sp.setMaximumSize(prefSize);
            java.awt.Container c = sp.getParent();
            if (c instanceof JComponent) {
                ((JComponent) c).revalidate();
            }
        }
    }

    private static int getLFHeightAdjustment() {
        LookAndFeel lf = UIManager.getLookAndFeel();
        String lfID = lf.getID();
        if ("Metal".equals(lfID)) {
            return 0;
        }
        if ("GTK".equals(lfID)) {
            return 2;
        }
        if ("Motif".equals(lfID)) {
            return 3;
        }
        if ("Nimbus".equals(lfID)) {
            return 0;
        }
        if ("Aqua".equals(lfID)) {
            return -2;
        }
        return 0;
    }

    private static final class DelegatingBorder implements Border {

        private Border delegate;
        private Insets insets;

        public DelegatingBorder(Border delegate, Insets insets) {
            this.delegate = delegate;
            this.insets = insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            //logger.fine("Delegate paintBorder("+c+", "+g+", "+x+", "+y+", "+width+", "+height+")");
            delegate.paintBorder(c, g, x, y, width, height);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return insets;
        }

        @Override
        public boolean isBorderOpaque() {
            return delegate.isBorderOpaque();
        }

    }
}
