/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.codesnippetscommunitymanager.maven.plugin;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

/**
 *
 * @author arsi
 */
@Mojo(name = "updatesite",
        defaultPhase = LifecyclePhase.DEPLOY,
        aggregator = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public class UpdateSiteMojo extends AbstractMojo {

    @Parameter(required = true, readonly = true, property = "serverId")
    private String serverId;

    @Parameter(required = true, readonly = true, property = "sftpUrl")
    private String sftpUrl;

    @Parameter(required = true, readonly = true, property = "sftpPath")
    private String sftpPath;

    @Parameter(required = true, readonly = true, property = "netbeansSiteDir")
    private File netbeansSiteDir;


    @Parameter(defaultValue = "${settings}", readonly = true)
    private Settings settings;
    private Server server;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        server = settings.getServer(serverId);
        if (server == null) {
            throw new MojoExecutionException("No server defined!");
        }
        SftpLoader loader = new SftpLoader(sftpUrl, server.getUsername(), server.getPassword());
        try {
            loader.connect();
            loader.transferDirToRemote(netbeansSiteDir.getCanonicalPath(), sftpPath);
            loader.disconnect();
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getMessage());
        }
    }

}
