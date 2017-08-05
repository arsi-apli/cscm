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

import javax.swing.JEditorPane;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.NodeAction;
import sk.arsi.nb.help.module.client.CommunitydocPanel;
import sk.arsi.nb.help.module.client.NbDocClient;
import sk.arsi.nb.help.module.client.ServerType;
import sk.arsi.nb.help.transfer.CreateHelpRecord;

/**
 *
 * @author arsi
 */
@ActionID(id = "sk.arsi.nb.help.module.actions.CreateHelpAction", category = "Community help")
@ActionRegistration(displayName = "#LBL_Action_CreateHelpAction", lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Editors/Popup", position = 99999998, separatorBefore = 99999997, separatorAfter = 99999999),})
@NbBundle.Messages({"LBL_Action_CreateHelpAction=Create community help"})
public class CreateHelpAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        JEditorPane pane = getPane(activatedNodes);
        if (pane != null && pane.getSelectedText() != null) {
            String selectedText = pane.getSelectedText();
            CreateHelpPanel panel = new CreateHelpPanel(selectedText, DocumentUtilities.getMimeType(pane));
            DialogDescriptor dd = new DialogDescriptor(panel, "Create community help record");
            Object notify = DialogDisplayer.getDefault().notify(dd);
            if (notify.equals(NotifyDescriptor.OK_OPTION)) {
                CreateHelpRecord record = panel.getRecord();
                if (panel.isSendToGlobal()) {
                    record.setEmail(NbPreferences.forModule(CommunitydocPanel.class).get(NbDocClient.EMAIL, ""));
                    record.setPasswordHash(NbPreferences.forModule(CommunitydocPanel.class).get(NbDocClient.PASSWORD_HASH, ""));
                    NbDocClient.addHelp(record, ServerType.MASTER);
                }
                if (panel.isSendToTeam()) {
                    record.setEmail(NbPreferences.forModule(CommunitydocPanel.class).get(NbDocClient.TEAM_EMAIL, ""));
                    record.setPasswordHash(NbPreferences.forModule(CommunitydocPanel.class).get(NbDocClient.TEAM_PASSWORD_HASH, ""));
                    NbDocClient.addHelp(record, ServerType.TEAM);
                }
                if (panel.isSendToLocal()) {
                    record.setEmail("local@local.loc");
                    record.setPasswordHash("");
                    NbDocClient.addHelp(record, ServerType.LOCAL);
                }

            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        JEditorPane pane = getPane(activatedNodes);
        if (pane != null && pane.getSelectedText() != null) {
            return true;
        }
        return false;
    }

    private JEditorPane getPane(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {
            final Node activeNode = activatedNodes[0];
            final DataObject dobj = activeNode.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                final EditorCookie ec = activeNode.getLookup().lookup(EditorCookie.class);
                if (ec != null) {
                    JEditorPane pane = Mutex.EVENT.readAccess(new Mutex.Action<JEditorPane>() {
                        @Override
                        public JEditorPane run() {
                            return NbDocument.findRecentEditorPane(ec);
                        }
                    });
                    if (pane != null) {
                        return pane;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "Create community help";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

}
