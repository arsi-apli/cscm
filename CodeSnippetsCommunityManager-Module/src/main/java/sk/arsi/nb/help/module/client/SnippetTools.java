/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.nb.help.module.client;

import sk.arsi.nb.help.transfer.HelpRecord;

/**
 *
 * @author arsi
 */
public class SnippetTools {

    public static final String snippetToHtml(HelpRecord rec) {
        String text = "<html>";
        text += "<b>Keys: </b>";
        String out = "";
        String[] keys = rec.getKeys();
        for (int i = 0; i < keys.length; i++) {
            out += keys[i];
            if (i < keys.length - 1) {
                out += ", ";
            }
        }
        text += out + "<br>";
        out = "";
        text += "<b>Classes: </b>";
        String[] classes = rec.getClasses();
        if (classes.length > 0) {
            for (int i = 0; i < classes.length; i++) {
                out += classes[i];
                if (i < classes.length - 1) {
                    out += ", ";
                }
            }
        }
        text += out + "<br>";
        if (rec.getUser() != null) {
            text += "<b>Created by: </b>";
            text += rec.getUser() + "<br>";
        }
        text += "<p style=\"background-color: #F5EFFB\">";
        text += rec.getCode().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>").replace(" ", "&nbsp;").replace("\t", "&#09;");
        text += "</p><html>";
        return text;
    }

    public static final String getKeys(HelpRecord rec) {
        String out = "";
        String[] keys = rec.getKeys();
        for (int i = 0; i < keys.length; i++) {
            out += keys[i];
            if (i < keys.length - 1) {
                out += ", ";
            }
        }
        return out;
    }

    public static final String getClasses(HelpRecord rec) {
        String out = "";
        String[] keys = rec.getClasses();
        for (int i = 0; i < keys.length; i++) {
            out += keys[i];
            if (i < keys.length - 1) {
                out += ", ";
            }
        }
        return out;
    }

}
