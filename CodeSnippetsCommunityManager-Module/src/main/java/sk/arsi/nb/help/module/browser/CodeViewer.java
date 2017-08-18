/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.nb.help.module.browser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import sk.arsi.nb.help.module.actions.CreateHelpPanel;
import sk.arsi.nb.help.module.actions.Tools;
import sk.arsi.nb.help.module.client.CommunitydocPanel;
import sk.arsi.nb.help.module.client.NbDocClient;
import sk.arsi.nb.help.module.client.ServerType;
import sk.arsi.nb.help.module.client.SnippetTools;
import sk.arsi.nb.help.transfer.DescriptionRecord;
import sk.arsi.nb.help.transfer.EditHelpRecord;
import sk.arsi.nb.help.transfer.HelpRecord;

/**
 *
 * @author arsi
 */
public class CodeViewer extends TopComponent {

    private JEditorPane editorPane;
    public static final Map<String, CodeViewer> ACTIVE = new HashMap<>();
    private final String key;
    private HelpRecord helpRecord;
    private final ServerType serverType;
    private static ImageIcon iconOwner;
    private static ImageIcon iconOther;

    /**
     * Creates new form CodeViewer
     */
    public CodeViewer(DescriptionRecord description, String mimeType, ServerType serverType) {
        initComponents();
        this.serverType = serverType;
        key = mimeType + description.getDescription() + serverType;
        ACTIVE.put(key, this);
        setName(description.getDescription());
        JComponent[] editorComponents = Tools.createEditorEditor(mimeType);
        JScrollPane sp = (JScrollPane) editorComponents[0];
        editorPane = (JEditorPane) editorComponents[1];
        editorPane.setEditable(false);
        editorPane.getCaret().setVisible(true);
        codePanel.add(sp);
        if (iconOwner == null) {
            iconOwner = ImageUtilities.loadImageIcon("sk/arsi/nb/help/module/owner.gif", false); // NOI18N
        }
        if (iconOther == null) {
            iconOther = ImageUtilities.loadImageIcon("sk/arsi/nb/help/module/globe.gif", false); // NOI18N
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Object help = NbDocClient.getSingleHelpRecord(description.getId(), serverType);
                if (help instanceof HelpRecord) {
                    helpRecord = (HelpRecord) help;
                    editorPane.setText(helpRecord.getCode());
                    author.setText(helpRecord.getUser());
                    keys.setText(SnippetTools.getKeys(helpRecord));
                    classes.setText(SnippetTools.getClasses(helpRecord));
                    mime.setText(mimeType);
                    descriptionField.setText(helpRecord.getDescription());
                    rank.setRank(helpRecord.getRank());
                    switch (serverType) {
                        case MASTER:
                        case TEAM: {
                            String email = NbPreferences.forModule(CommunitydocPanel.class).get(NbDocClient.EMAIL, "");
                            makeEditable(description.getEmail().equalsIgnoreCase(email));
                        }
                        break;
                        case LOCAL:
                            makeEditable(true);
                            break;
                        default:
                            throw new AssertionError(serverType.name());

                    }
                } else {
                    helpRecord = null;
                    editorPane.setText("No response from server!!!");
                    author.setText("");
                    descriptionField.setText("");
                    keys.setText("");
                    classes.setText("");
                    mime.setText(mimeType);
                    makeEditable(false);
                }
                loading.setVisible(false);
            }
        };
        CreateHelpPanel.pool.execute(runnable);
    }

    private void makeEditable(boolean editable) {
        editorPane.setEditable(editable);
        editorPane.getCaret().setVisible(true);
        keys.setEditable(editable);
        classes.setEditable(editable);
        descriptionField.setEditable(editable);
        save.setEnabled(editable);
        SwingUtilities.invokeLater(() -> {
            if (editable) {
                setIcon(iconOwner.getImage());
            } else {
                setIcon(iconOther.getImage());
            }
        });
        rank.setVisible(!editable);
        author.setVisible(!editable);
        authorLb.setVisible(!editable);

    }

    @Override
    public void componentClosed() {
        ACTIVE.remove(key);
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER; //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        authorLb = new javax.swing.JLabel();
        mime = new javax.swing.JLabel();
        author = new javax.swing.JLabel();
        loading = new javax.swing.JLabel();
        keys = new javax.swing.JTextField();
        classes = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        descriptionField = new javax.swing.JTextField();
        save = new javax.swing.JButton();
        rank = new sk.arsi.nb.help.module.browser.RankView();
        codePanel = new javax.swing.JPanel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CodeViewer.class, "CodeViewer.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CodeViewer.class, "CodeViewer.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CodeViewer.class, "CodeViewer.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(authorLb, org.openide.util.NbBundle.getMessage(CodeViewer.class, "CodeViewer.authorLb.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(mime, org.openide.util.NbBundle.getMessage(CodeViewer.class, "CodeViewer.mime.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(author, org.openide.util.NbBundle.getMessage(CodeViewer.class, "CodeViewer.author.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(loading, org.openide.util.NbBundle.getMessage(CodeViewer.class, "CodeViewer.loading.text")); // NOI18N

        keys.setText(org.openide.util.NbBundle.getMessage(CodeViewer.class, "CodeViewer.keys.text")); // NOI18N

        classes.setText(org.openide.util.NbBundle.getMessage(CodeViewer.class, "CodeViewer.classes.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(CodeViewer.class, "CodeViewer.jLabel5.text")); // NOI18N

        descriptionField.setText(org.openide.util.NbBundle.getMessage(CodeViewer.class, "CodeViewer.descriptionField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(save, org.openide.util.NbBundle.getMessage(CodeViewer.class, "CodeViewer.save.text")); // NOI18N
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(authorLb)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(mime)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(rank, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(author)
                        .addGap(460, 460, 460)
                        .addComponent(loading))
                    .addComponent(classes, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
                    .addComponent(keys)
                    .addComponent(descriptionField))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 429, Short.MAX_VALUE)
                .addComponent(save)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(mime)
                    .addComponent(jLabel1)
                    .addComponent(rank, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(descriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(keys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(classes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(authorLb, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(author, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loading, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(save))
                .addContainerGap())
        );

        codePanel.setLayout(new java.awt.CardLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(codePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
        // TODO add your handling code here:
        if (helpRecord != null) {
            String email = NbPreferences.forModule(CommunitydocPanel.class).get(NbDocClient.EMAIL, "");
            String password = NbPreferences.forModule(CommunitydocPanel.class).get(NbDocClient.PASSWORD_HASH, "");
            EditHelpRecord ed = new EditHelpRecord();
            ed.setId((int) helpRecord.getId());
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
            ed.setKeys(keysList.toArray(new String[keysList.size()]));
            ed.setClasses(classList.toArray(new String[classList.size()]));
            ed.setCode(editorPane.getText());
            ed.setDescription(descriptionField.getText());
            ed.setMimeType(mime.getText());
            ed.setEmail(email);
            ed.setPasswordHash(password);
            if (!NbDocClient.editHelp(ed, serverType).isOk()) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to write changes to server!", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
            }

        }
    }//GEN-LAST:event_saveActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel author;
    private javax.swing.JLabel authorLb;
    private javax.swing.JTextField classes;
    private javax.swing.JPanel codePanel;
    private javax.swing.JTextField descriptionField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField keys;
    private javax.swing.JLabel loading;
    private javax.swing.JLabel mime;
    private sk.arsi.nb.help.module.browser.RankView rank;
    private javax.swing.JButton save;
    // End of variables declaration//GEN-END:variables
}
