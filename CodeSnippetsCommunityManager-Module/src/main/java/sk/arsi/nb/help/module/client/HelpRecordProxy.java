/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.nb.help.module.client;

import java.util.Date;
import sk.arsi.nb.help.transfer.HelpRecord;

/**
 *
 * @author arsi
 */
public class HelpRecordProxy extends HelpRecord {

    private final HelpRecord original;
    private final ServerType serverType;

    public HelpRecordProxy(HelpRecord original, ServerType serverType) {
        this.original = original;
        this.serverType = serverType;
    }

    @Override
    public void setDescription(String description) {
    }

    @Override
    public String getDescription() {
        return original.getDescription(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setRank(int rank) {
    }

    @Override
    public int getRank() {
        return original.getRank(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCode(String code) {
    }

    @Override
    public String getCode() {
        return original.getCode(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setUser(String user) {
    }

    @Override
    public String getUser() {
        return original.getUser(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCreated(Date created) {
    }

    @Override
    public Date getCreated() {
        return original.getCreated(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setId(long id) {
    }

    @Override
    public long getId() {
        return original.getId(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setClasses(String[] classes) {
    }

    @Override
    public String[] getClasses() {
        return original.getClasses(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setKeys(String[] keys) {
    }

    @Override
    public String[] getKeys() {
        return original.getKeys(); //To change body of generated methods, choose Tools | Templates.
    }

    public ServerType getServerType() {
        return serverType;
    }

}
