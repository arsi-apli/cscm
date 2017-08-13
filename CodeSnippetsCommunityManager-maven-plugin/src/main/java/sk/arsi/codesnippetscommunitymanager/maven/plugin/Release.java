/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.codesnippetscommunitymanager.maven.plugin;

import java.util.List;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author arsi
 */
public class Release {

    @Parameter(required = true, readonly = true, property = "serverId")
    private String serverId;

    @Parameter(required = true, readonly = true, property = "outFileName")
    private String outFileName;

    @Parameter(required = true, readonly = true, property = "sftpUrl")
    private String sftpUrl;

    @Parameter(required = true, readonly = true, property = "sftpPath")
    private String sftpPath;

    @Parameter(required = true, readonly = true, property = "subDirs")
    private List<SubDir> subDirs;

    public String getServerId() {
        return serverId;
    }

    public String getOutFileName() {
        return outFileName;
    }

    public String getSftpUrl() {
        return sftpUrl;
    }

    public String getSftpPath() {
        return sftpPath;
    }

    public List<SubDir> getSubDirs() {
        return subDirs;
    }

}
