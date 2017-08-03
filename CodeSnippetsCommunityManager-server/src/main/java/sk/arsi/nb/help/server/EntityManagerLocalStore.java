/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.nb.help.server;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.persistence.EntityManagerFactory;
import org.eclipse.persistence.jpa.JpaEntityManager;

/**
 *
 * @author arsi
 */
public class EntityManagerLocalStore {

    private final JpaEntityManager MANAGER;
    private final AtomicBoolean STATUS = new AtomicBoolean(false);

    public EntityManagerLocalStore(EntityManagerFactory factory, ScheduledExecutorService schedulerPool) {
        MANAGER = (JpaEntityManager) factory.createEntityManager();
        schedulerPool.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                STATUS.set(true);
            }
        }, 20, 20, TimeUnit.MINUTES);
    }

    public JpaEntityManager getManager() {
        if (STATUS.getAndSet(false)) {
            MANAGER.clear();
        }
        return MANAGER;
    }


}
