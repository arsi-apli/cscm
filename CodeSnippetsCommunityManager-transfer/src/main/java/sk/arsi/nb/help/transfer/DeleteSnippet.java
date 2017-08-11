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
public class DeleteSnippet implements Serializable {

    private int snippetId;
    private String email;
    private String passwordHash;

    public DeleteSnippet() {
    }

    public DeleteSnippet(int snippetId, String email, String passwordHash) {
        this.snippetId = snippetId;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public int getSnippetId() {
        return snippetId;
    }

    public void setSnippetId(int snippetId) {
        this.snippetId = snippetId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }


}
