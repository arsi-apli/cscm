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
package org.netbeans.modules.editor.completion;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import org.openide.util.NbPreferences;

/**
 *
 * @author arsi
 */
public class RankEdit extends javax.swing.JPanel {

    RankProvider provider = null;

    /**
     * Creates new form RankEdit
     */
    public RankEdit() {
        initComponents();
        zero.setContentAreaFilled(false);
        rank1.setContentAreaFilled(false);
        rank2.setContentAreaFilled(false);
        rank3.setContentAreaFilled(false);
        rank4.setContentAreaFilled(false);
        rank5.setContentAreaFilled(false);
        rank1.setFocusable(false);
        rank2.setFocusable(false);
        rank3.setFocusable(false);
        rank4.setFocusable(false);
        rank5.setFocusable(false);
        zero.setFocusable(false);
        zero.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (NbPreferences.forModule(RankEdit.class).getInt("RANK_" + provider.getHelpId(), 100) != 100) {
                    return;
                }
                setRankSave(0);
                if (provider != null) {
                    provider.setRank(0);
                }
            }

        });
        rank1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (NbPreferences.forModule(RankEdit.class).getInt("RANK_" + provider.getHelpId(), 100) != 100) {
                    return;
                }
                setRankSave(1);
                if (provider != null) {
                    provider.setRank(1);
                }
            }

        });
        rank2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (NbPreferences.forModule(RankEdit.class).getInt("RANK_" + provider.getHelpId(), 100) != 100) {
                    return;
                }
                setRankSave(2);
                if (provider != null) {
                    provider.setRank(2);
                }
            }

        });
        rank3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (NbPreferences.forModule(RankEdit.class).getInt("RANK_" + provider.getHelpId(), 100) != 100) {
                    return;
                }
                setRankSave(3);
                if (provider != null) {
                    provider.setRank(3);
                }
            }

        });
        rank4.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (NbPreferences.forModule(RankEdit.class).getInt("RANK_" + provider.getHelpId(), 100) != 100) {
                    return;
                }
                setRankSave(4);
                if (provider != null) {
                    provider.setRank(4);
                }
            }

        });
        rank5.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (NbPreferences.forModule(RankEdit.class).getInt("RANK_" + provider.getHelpId(), 100) != 100) {
                    return;
                }
                setRankSave(5);
                if (provider != null) {
                    provider.setRank(5);
                }
            }

        });

    }

    private void setButtonRank(JButton button, boolean on) {
        if (on) {
            button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/editor/completion/resources/rank.png"))); // NOI18N
        } else {
            button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/editor/completion/resources/rank_off.png"))); // NOI18N

        }
    }

    public void setProvider(RankProvider provider) {
        this.provider = provider;
        if (provider == null) {
            setButtonRank(rank1, false);
            setButtonRank(rank2, false);
            setButtonRank(rank3, false);
            setButtonRank(rank4, false);
            setButtonRank(rank5, false);
        } else {
            int rank = NbPreferences.forModule(RankEdit.class).getInt("RANK_" + provider.getHelpId(), 100);
            if (rank == 100) {
                setRank(0);
            } else {
                setRank(rank);
            }
        }

    }

    private void setRankSave(int rank) {
        setRank(rank);
        NbPreferences.forModule(RankEdit.class).putInt("RANK_" + provider.getHelpId(), rank);
    }

    private void setRank(int rank) {
        switch (rank) {
            case 0:
                setButtonRank(rank1, false);
                setButtonRank(rank2, false);
                setButtonRank(rank3, false);
                setButtonRank(rank4, false);
                setButtonRank(rank5, false);
                break;
            case 1:
                setButtonRank(rank1, true);
                setButtonRank(rank2, false);
                setButtonRank(rank3, false);
                setButtonRank(rank4, false);
                setButtonRank(rank5, false);
                break;
            case 2:
                setButtonRank(rank1, true);
                setButtonRank(rank2, true);
                setButtonRank(rank3, false);
                setButtonRank(rank4, false);
                setButtonRank(rank5, false);
                break;
            case 3:
                setButtonRank(rank1, true);
                setButtonRank(rank2, true);
                setButtonRank(rank3, true);
                setButtonRank(rank4, false);
                setButtonRank(rank5, false);
                break;
            case 4:
                setButtonRank(rank1, true);
                setButtonRank(rank2, true);
                setButtonRank(rank3, true);
                setButtonRank(rank4, true);
                setButtonRank(rank5, false);
                break;
            case 5:
                setButtonRank(rank1, true);
                setButtonRank(rank2, true);
                setButtonRank(rank3, true);
                setButtonRank(rank4, true);
                setButtonRank(rank5, true);
                break;
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

        zero = new javax.swing.JButton();
        rank1 = new javax.swing.JButton();
        rank2 = new javax.swing.JButton();
        rank3 = new javax.swing.JButton();
        rank4 = new javax.swing.JButton();
        rank5 = new javax.swing.JButton();

        zero.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/editor/completion/resources/zero_rank.png"))); // NOI18N

        rank1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/editor/completion/resources/rank.png"))); // NOI18N

        rank2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/editor/completion/resources/rank.png"))); // NOI18N

        rank3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/editor/completion/resources/rank.png"))); // NOI18N

        rank4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/editor/completion/resources/rank.png"))); // NOI18N

        rank5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/editor/completion/resources/rank.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(zero, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(rank1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(rank2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(rank3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(rank4, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(rank5, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(rank1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rank2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rank3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rank4, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rank5, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zero, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton rank1;
    private javax.swing.JButton rank2;
    private javax.swing.JButton rank3;
    private javax.swing.JButton rank4;
    private javax.swing.JButton rank5;
    private javax.swing.JButton zero;
    // End of variables declaration//GEN-END:variables
}
