/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.nb.help.module.client;

/**
 *
 * @author arsi
 */
public enum ServerType {
    MASTER("Community"),
    TEAM("Team"),
    LOCAL("Local");

    private final String name;

    private ServerType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
