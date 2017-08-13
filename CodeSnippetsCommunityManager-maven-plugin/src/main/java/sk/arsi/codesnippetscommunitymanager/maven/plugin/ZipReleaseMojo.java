/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.codesnippetscommunitymanager.maven.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

/**
 *
 * @author arsi
 */
@Mojo(name = "releasezip",
        defaultPhase = LifecyclePhase.DEPLOY,
        aggregator = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public class ZipReleaseMojo extends AbstractMojo {

    @Parameter(required = true, readonly = true, property = "project")
    private MavenProject project;

    @Parameter(required = true, readonly = true, property = "releases")
    private List<Release> releases;

    @Parameter(defaultValue = "${settings}", readonly = true)
    private Settings settings;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        for (Release release : releases) {
            String serverId = release.getServerId();
            String sftpUrl = release.getSftpUrl();
            String sftpPath = release.getSftpPath();
            Server server = settings.getServer(serverId);
            if (server == null) {
                throw new MojoExecutionException("No server defined!");
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.out.println("Building zip file: " + release.getOutFileName());
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                zos.setLevel(Deflater.BEST_COMPRESSION);
                List<SubDir> subDirs = release.getSubDirs();
                for (SubDir subDir : subDirs) {
                    ZipEntry entry = new ZipEntry(subDir.getPath() + "/");
                    zos.putNextEntry(entry);
                    File[] files = subDir.getFiles();
                    for (File file : files) {
                        byte[] buf = new byte[1024];
                        int len;
                        FileInputStream in = new FileInputStream(file);
                        zos.putNextEntry(new ZipEntry(subDir.getPath() + "/" + file.getName()));
                        while ((len = in.read(buf)) > 0) {
                            zos.write(buf, 0, len);
                        }
                        in.close();
                    }
                }
                zos.flush();
                zos.finish();
                System.out.println("Done..");
                SftpLoader loader = new SftpLoader(sftpUrl, server.getUsername(), server.getPassword());
                try {
                    loader.connect();
                    loader.transferSingleFile(baos.toByteArray(), release.getOutFileName() + ".zip", sftpPath);
                    loader.disconnect();
                } catch (Exception ex) {
                    throw new MojoExecutionException(ex.getMessage());
                }
                zos.close();
                baos.close();
            } catch (Exception ex) {
                throw new MojoFailureException("Exception", ex);
            }

        }

    }

}
