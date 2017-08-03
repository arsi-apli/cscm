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
public class FindFullTextCode implements Serializable {

    String text;
    String mimeType;
    int maxRecords;

    public FindFullTextCode() {
    }

    public FindFullTextCode(String text, String mimeType, int maxRecords) {
        this.text = text;
        this.mimeType = mimeType;
        this.maxRecords = maxRecords;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getMaxRecords() {
        return maxRecords;
    }

    public void setMaxRecords(int maxRecords) {
        this.maxRecords = maxRecords;
    }

}
