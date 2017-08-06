/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.nb.help.transfer;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author arsi
 */
public class MimeRecord implements Serializable {

    private String mime;
    private String description;

    public MimeRecord() {
    }

    public MimeRecord(String mime, String description) {
        this.mime = mime;
        this.description = description;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.mime);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MimeRecord other = (MimeRecord) obj;
        if (!Objects.equals(this.mime, other.mime)) {
            return false;
        }
        return true;
    }

}
