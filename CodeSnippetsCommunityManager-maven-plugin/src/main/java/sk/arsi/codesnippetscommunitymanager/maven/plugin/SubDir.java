/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.codesnippetscommunitymanager.maven.plugin;

import java.io.File;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author arsi
 */
public class SubDir {

    @Parameter(required = true, readonly = true, property = "path")
    private String path;

    @Parameter(property = "files")
    private File files[];

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public File[] getFiles() {
        return files;
    }

    public void setFiles(File[] files) {
        this.files = files;
    }

}
