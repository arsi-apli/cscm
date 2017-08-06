/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.nb.help.module.actions;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.ImageUtilities;
import sk.arsi.nb.help.module.client.HelpRecordProxy;

/**
 *
 * @author arsi
 */
public class SnippetsViewer extends javax.swing.JPanel {

    private final HelpRecordProxy helps[];
    private static ImageIcon iconMaster;
    private static ImageIcon iconTeam;
    private static ImageIcon iconLocal;
    private final String mimeType;
    private final JEditorPane editorPane;

    /**
     * Creates new form SnippetsViewer
     */
    public SnippetsViewer(HelpRecordProxy helps[], String mimeType) {
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
        this.helps = helps;
        this.mimeType = mimeType;
        list.setModel(new DescriptionListModel());
        list.setCellRenderer(new DefaultListCellRenderer() {
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
                }
                return label;
            }
        });
        JComponent[] editorComponents = Tools.createSingleLineEditor(mimeType);
        JScrollPane sp = (JScrollPane) editorComponents[0];
        editorPane = (JEditorPane) editorComponents[1];
        editorPane.setEditable(false);
        editorPane.getCaret().setVisible(true);
        viewPanel.add(sp);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                HelpRecordProxy selectedValue = list.getSelectedValue();
                if (selectedValue != null) {
                    editorPane.setText(selectedValue.getCode());
                } else {
                    editorPane.setText("");
                }
            }
        });
        if (helps.length > 0) {
            list.setSelectedIndex(0);
        }
    }

    private class DescriptionListModel implements ListModel<HelpRecordProxy> {

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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();
        viewPanel = new javax.swing.JPanel();

        jScrollPane1.setViewportView(list);

        viewPanel.setLayout(new java.awt.CardLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(viewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 813, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 556, Short.MAX_VALUE)
            .addComponent(viewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<HelpRecordProxy> list;
    private javax.swing.JPanel viewPanel;
    // End of variables declaration//GEN-END:variables
}
