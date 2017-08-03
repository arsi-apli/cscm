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
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "View",
        id = "sk.arsi.nb.help.module.actions.ServerSelectionToolbar"
)
@ActionRegistration(
        displayName = "Server selection", lazy = false
)
@ActionReferences({
    @ActionReference(path = "Editors/Toolbars/Default", position = 999998)

})
public class ServerSelectionToolbar extends AbstractAction implements ContextAwareAction, Presenter.Toolbar {

    private static final Map<Document, ServerSelector> documents = new WeakHashMap<>();

    public ServerSelector panel = null;

    public static final ServerSelector findPanel(Document document) {
        return documents.get(document);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        JTextComponent textComponent = actionContext.lookup(JTextComponent.class);
        Document document = textComponent.getDocument();
        JPanel p = documents.get(document);
        if (p == null) {
            DataObject dob = actionContext.lookup(DataObject.class);
            if (dob != null) {
                panel = new ServerSelector("" + dob.getPrimaryFile().hashCode());
            } else {
                panel = new ServerSelector("");
            }
            documents.put(document, panel);
        }

        return new ZeroAction();
    }

    @Override
    public Component getToolbarPresenter() {
        return panel;
    }

    private class ZeroAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
        }

    }

}
