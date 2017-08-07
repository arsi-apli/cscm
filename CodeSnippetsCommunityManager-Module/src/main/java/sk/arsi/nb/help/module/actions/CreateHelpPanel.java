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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.ImageUtilities;
import sk.arsi.nb.help.module.client.HelpRecordProxy;
import sk.arsi.nb.help.module.client.NbDocClient;
import sk.arsi.nb.help.module.client.ServerType;
import sk.arsi.nb.help.module.client.SnippetTools;
import sk.arsi.nb.help.transfer.CreateHelpRecord;
import sk.arsi.nb.help.transfer.HelpRecord;

/**
 *
 * @author arsi
 */
public class CreateHelpPanel extends javax.swing.JPanel {

    private JEditorPane editorPane;
    private final String mimeTypeType;
    private HelpRecordProxy[] duplicateCodeArray;
    public static final ExecutorService pool = Executors.newFixedThreadPool(1);
    private HelpRecordProxy[] duplicateDescriptionsArray;
    private static ImageIcon iconMaster;
    private static ImageIcon iconTeam;
    private static ImageIcon iconLocal;

    /**
     * Creates new form CreateHelpPanel
     */
    public CreateHelpPanel(final String code, final String mimeType) {
        initComponents();
        if (iconMaster == null) {
            iconMaster = ImageUtilities.loadImageIcon("sk/arsi/nb/help/module/help_item.png", false); // NOI18N
        }
        if (iconTeam == null) {
            iconTeam = ImageUtilities.loadImageIcon("sk/arsi/nb/help/module/help_item_team.png", false); // NOI18N
        }
        if (iconLocal == null) {
            iconLocal = ImageUtilities.loadImageIcon("sk/arsi/nb/help/module/help_item_local.png", false); // NOI18N
        }
        this.mimeTypeType = mimeType;
        duplicateCode.setVisible(false);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                JComponent[] editorComponents = Tools.createEditorEditor(mimeType);
                JScrollPane sp = (JScrollPane) editorComponents[0];
                editorPane = (JEditorPane) editorComponents[1];
                jPanel1.add(sp);
                editorPane.setText(code);
                CreateHelpPanel.this.mimeType.setText(mimeTypeType);
                description.setText("");
                editorPane.setFocusable(true);
                editorPane.requestFocus();
                description.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateDescriptionDuplicates(description.getText());
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateDescriptionDuplicates(description.getText());
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateDescriptionDuplicates(description.getText());
                    }

                });
                duplicateDescriptions.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        if (value instanceof HelpRecordProxy) {
                            switch (((HelpRecordProxy) value).getServerType()) {
                                case MASTER:
                                    label.setIcon(iconMaster);
                                    break;
                                case TEAM:
                                    label.setIcon(iconTeam);
                                    break;
                                case LOCAL:
                                    label.setIcon(iconLocal);
                                    break;
                                default:
                                    throw new AssertionError(((HelpRecordProxy) value).getServerType().name());

                            }
                            label.setText(((HelpRecordProxy) value).getDescription());
                            label.setToolTipText(SnippetTools.snippetToHtml(((HelpRecordProxy) value)));
                        }
                        return label;
                    }
                });
            }
        };
        SwingUtilities.invokeLater(runnable);
        pool.execute(() -> {
            duplicateCodeArray = findDuplicateCode(code, mimeType);
            if (duplicateCodeArray.length > 0) {
                duplicateCode.setVisible(true);
            }
        });

    }

    private class DescriptionListModel implements ListModel<HelpRecordProxy> {

        private final HelpRecordProxy helps[];

        public DescriptionListModel(HelpRecordProxy[] helps) {
            this.helps = helps;
        }

        @Override
        public int getSize() {
            return helps.length;
        }

        @Override
        public HelpRecordProxy getElementAt(int index) {
            return helps[index];
        }

        @Override
        public void addListDataListener(ListDataListener l) {
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
        }

    }

    private void updateDescriptionDuplicates(final String text) {
        if (!"".equals(text) && text.length() > 1) {
            pool.execute(() -> {
                duplicateDescriptionsArray = findDuplicateDescription(text, mimeTypeType);
                HelpRecordProxy selectedValue = duplicateDescriptions.getSelectedValue();
                duplicateDescriptions.setModel(new DescriptionListModel(duplicateDescriptionsArray));
                if (selectedValue != null) {
                    for (int i = 0; i < duplicateCodeArray.length; i++) {
                        HelpRecordProxy rec = duplicateCodeArray[i];
                        if (selectedValue.equals(rec)) {
                            duplicateDescriptions.setSelectedIndex(i);
                        }
                    }
                }

            });
        } else {
            duplicateDescriptions.setModel(new DescriptionListModel(new HelpRecordProxy[0]));
        }

    }

    private HelpRecordProxy[] findDuplicateDescription(final String code, final String mimeType) {
        List<HelpRecordProxy> tmp = new ArrayList<>();
        Object fromMaster = NbDocClient.getByFullTextDescription(code, ServerType.MASTER, mimeType, 7);
        readRecords(tmp, fromMaster, ServerType.MASTER);
        Object fromTeam = NbDocClient.getByFullTextDescription(code, ServerType.TEAM, mimeType, 7);
        readRecords(tmp, fromTeam, ServerType.TEAM);
        Object fromLocal = NbDocClient.getByFullTextDescription(code, ServerType.LOCAL, mimeType, 7);
        readRecords(tmp, fromLocal, ServerType.LOCAL);

        return tmp.toArray(new HelpRecordProxy[tmp.size()]);
    }

    private HelpRecordProxy[] findDuplicateCode(final String code, final String mimeType) {
        List<HelpRecordProxy> tmp = new ArrayList<>();
        Object fromMaster = NbDocClient.getByFullTextCode(code, ServerType.MASTER, mimeType, 10);
        readRecords(tmp, fromMaster, ServerType.MASTER);
        Object fromTeam = NbDocClient.getByFullTextCode(code, ServerType.TEAM, mimeType, 10);
        readRecords(tmp, fromTeam, ServerType.TEAM);
        Object fromLocal = NbDocClient.getByFullTextCode(code, ServerType.LOCAL, mimeType, 10);
        readRecords(tmp, fromLocal, ServerType.LOCAL);

        return tmp.toArray(new HelpRecordProxy[tmp.size()]);
    }

    private void readRecords(List<HelpRecordProxy> tmp, Object helps, ServerType serverType) {
        if (helps instanceof HelpRecord[]) {
            for (HelpRecord rec : ((HelpRecord[]) helps)) {
                tmp.add(new HelpRecordProxy(rec, serverType));
            }
        }
    }

    public boolean isSendToGlobal() {
        return serverSelector1.isGlobalCreate();
    }

    public boolean isSendToTeam() {
        return serverSelector1.isTeamCreate();
    }

    public boolean isSendToLocal() {
        return serverSelector1.isLocalCreate();
    }

    public CreateHelpRecord getRecord() {
        CreateHelpRecord record = new CreateHelpRecord();
        //    record.setUser(user.getText());
        record.setCode(editorPane.getText());
        List<String> keysList = new ArrayList<>();
        List<String> classList = new ArrayList<>();
        StringTokenizer tok = new StringTokenizer(keys.getText(), ";", false);
        while (tok.hasMoreElements()) {
            keysList.add(tok.nextToken());
        }
        tok = new StringTokenizer(classes.getText(), ";", false);
        while (tok.hasMoreElements()) {
            classList.add(tok.nextToken());
        }
        record.setClasses(classList.toArray(new String[classList.size()]));
        record.setKeys(keysList.toArray(new String[keysList.size()]));
        record.setMimeType(mimeTypeType);
        record.setDescription(description.getText());
        return record;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        keys = new javax.swing.JTextField();
        classes = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        description = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        serverSelector1 = new sk.arsi.nb.help.module.actions.ServerSelector();
        jLabel5 = new javax.swing.JLabel();
        mimeType = new javax.swing.JLabel();
        duplicateCode = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        duplicateDescriptions = new javax.swing.JList<>();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateHelpPanel.class, "CreateHelpPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CreateHelpPanel.class, "CreateHelpPanel.jLabel2.text")); // NOI18N

        keys.setText(org.openide.util.NbBundle.getMessage(CreateHelpPanel.class, "CreateHelpPanel.keys.text")); // NOI18N

        classes.setText(org.openide.util.NbBundle.getMessage(CreateHelpPanel.class, "CreateHelpPanel.classes.text")); // NOI18N

        jPanel1.setLayout(new java.awt.CardLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CreateHelpPanel.class, "CreateHelpPanel.jLabel3.text")); // NOI18N

        description.setText(org.openide.util.NbBundle.getMessage(CreateHelpPanel.class, "CreateHelpPanel.description.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(CreateHelpPanel.class, "CreateHelpPanel.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(CreateHelpPanel.class, "CreateHelpPanel.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(mimeType, org.openide.util.NbBundle.getMessage(CreateHelpPanel.class, "CreateHelpPanel.mimeType.text")); // NOI18N

        duplicateCode.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/arsi/nb/help/module/alert.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(duplicateCode, org.openide.util.NbBundle.getMessage(CreateHelpPanel.class, "CreateHelpPanel.duplicateCode.text")); // NOI18N
        duplicateCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateCodeActionPerformed(evt);
            }
        });

        duplicateDescriptions.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                duplicateDescriptionMouseClick(evt);
            }
        });
        jScrollPane1.setViewportView(duplicateDescriptions);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(keys, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(serverSelector1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mimeType)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(duplicateCode))
                    .addComponent(classes, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(description))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(keys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(classes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel3)
                            .addComponent(description, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel4)
                            .addComponent(serverSelector1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(mimeType)
                            .addComponent(duplicateCode)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void duplicateCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateCodeActionPerformed
        // TODO add your handling code here:
        Object[] options = new Object[]{DialogDescriptor.OK_OPTION};
        DialogDescriptor dd = new DialogDescriptor(new SnippetsViewer(duplicateCodeArray, mimeTypeType), "Snippets viewer", true, options, DialogDescriptor.OK_OPTION, DialogDescriptor.RIGHT_ALIGN, null, null);
        Object notify = DialogDisplayer.getDefault().notify(dd);
    }//GEN-LAST:event_duplicateCodeActionPerformed

    private void duplicateDescriptionMouseClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_duplicateDescriptionMouseClick
        // TODO add your handling code here:
        JList list = (JList) evt.getSource();
        if (evt.getClickCount() == 2) {
            int index = list.locationToIndex(evt.getPoint());
            description.setText(duplicateDescriptions.getModel().getElementAt(index).getDescription());
        }
    }//GEN-LAST:event_duplicateDescriptionMouseClick


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField classes;
    private javax.swing.JTextField description;
    private javax.swing.JButton duplicateCode;
    private javax.swing.JList<HelpRecordProxy> duplicateDescriptions;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField keys;
    private javax.swing.JLabel mimeType;
    private sk.arsi.nb.help.module.actions.ServerSelector serverSelector1;
    // End of variables declaration//GEN-END:variables

}
