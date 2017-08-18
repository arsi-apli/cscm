/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.nb.help.transfer;

import java.io.Serializable;

/**
 *
 * @author arsi
 */
public class EditHelpRecord extends CreateHelpRecord implements Serializable {

    private int id;

    public EditHelpRecord(int id, String[] keys, String[] classes, String user, String passwordHash, String code, String description, String mimeType) {
        super(keys, classes, user, passwordHash, code, description, mimeType);
        this.id = id;
    }

    public EditHelpRecord() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


}
