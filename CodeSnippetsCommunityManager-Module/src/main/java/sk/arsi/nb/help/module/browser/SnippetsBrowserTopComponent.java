/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.nb.help.module.browser;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import sk.arsi.nb.help.module.client.ServerType;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//sk.arsi.nb.help.module.browser//SnippetsBrowser//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "SnippetsBrowserTopComponent",
        iconBase = "sk/arsi/nb/help/module/help_icon.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "sk.arsi.nb.help.module.browser.SnippetsBrowserTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_SnippetsBrowserAction",
        preferredID = "SnippetsBrowserTopComponent"
)
@Messages({
    "CTL_SnippetsBrowserAction=Snippets browser",
    "CTL_SnippetsBrowserTopComponent=Snippets browser",
    "HINT_SnippetsBrowserTopComponent="
})
public final class SnippetsBrowserTopComponent extends TopComponent implements ItemListener {

    public SnippetsBrowserTopComponent() {
        initComponents();
        setName(Bundle.CTL_SnippetsBrowserTopComponent());
        reloadList.setContentAreaFilled(false);
        reloadMime.setContentAreaFilled(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        server = new javax.swing.JComboBox<>();
        mime = new javax.swing.JComboBox<>();
        reloadMime = new javax.swing.JButton();
        reloadList = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();
        filter = new javax.swing.JTextField();

        server.setModel(new DefaultComboBoxModel<>(ServerType.values()));

        reloadMime.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/arsi/nb/help/module/reload.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(reloadMime, org.openide.util.NbBundle.getMessage(SnippetsBrowserTopComponent.class, "SnippetsBrowserTopComponent.reloadMime.text")); // NOI18N

        reloadList.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/arsi/nb/help/module/reload.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(reloadList, org.openide.util.NbBundle.getMessage(SnippetsBrowserTopComponent.class, "SnippetsBrowserTopComponent.reloadList.text")); // NOI18N

        jScrollPane1.setViewportView(list);

        filter.setText(org.openide.util.NbBundle.getMessage(SnippetsBrowserTopComponent.class, "SnippetsBrowserTopComponent.filter.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(server, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(reloadMime, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(mime, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(reloadList, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
            .addComponent(filter)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(server, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reloadMime, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reloadList, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(filter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField filter;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<String> list;
    private javax.swing.JComboBox<String> mime;
    private javax.swing.JButton reloadList;
    private javax.swing.JButton reloadMime;
    private javax.swing.JComboBox<ServerType> server;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        server.addItemListener(this);
        mime.addItemListener(this);

    }

    @Override
    public void componentClosed() {
        server.removeItemListener(this);
        mime.removeItemListener(this);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (e.getSource().equals(server)) {
                ServerType serverType = (ServerType) e.getItem();
            } else if (e.getSource().equals(mime)) {
                String mimeString = (String) e.getItem();
            }
        }
    }
}