/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.nb.help.module.client;

import org.openide.modules.OnStart;
import org.openide.modules.Places;
import sk.arsi.nb.help.server.local.LocalTransferManager;

/**
 *
 * @author arsi
 */
@OnStart
public class StartLocalCient implements Runnable {

    @Override
    public void run() {
        LocalTransferManager.Start(Places.getUserDirectory());
    }

}
