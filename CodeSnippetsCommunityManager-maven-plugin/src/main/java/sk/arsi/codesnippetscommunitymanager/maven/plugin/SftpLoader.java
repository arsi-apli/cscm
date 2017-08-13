/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.codesnippetscommunitymanager.maven.plugin;

import com.jcraft.jsch.*;
import java.io.*;
import java.util.StringTokenizer;

/**
 * Created by krishna on 29/03/2016.
 */
public class SftpLoader {

    ChannelSftp channel;
    final String host;
    final String userName;
    final String password;
    private Session session;

    public SftpLoader(String host, String userName, String password) {
        this.host = host;
        this.userName = userName;
        this.password = password;
    }

    public void connect() throws JSchException {
        System.out.println("Connecting sftp: " + host);
        JSch jsch = new JSch();
        session = jsch.getSession(userName, host);
        session.setPassword(password);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        System.out.println("Connected " + host);
    }

    public void disconnect() {
        channel.disconnect();
        session.disconnect();
        System.out.println("Disconnected " + host);
    }

    private void cdOrCreateDirs(String remoteDir) throws SftpException {
        StringTokenizer tok = new StringTokenizer(remoteDir, "/", false);
        while (tok.hasMoreElements()) {
            String nextdir = tok.nextToken();
            try {
                channel.stat(nextdir);
            } catch (Exception e) {
                channel.mkdir(nextdir);
                System.out.println(host + " mkdir: " + nextdir);
            }
            channel.cd(nextdir);
            System.out.println(host + " cd: " + nextdir);
        }
    }

    public void transferDirToRemote(String localDir, String remoteDir) throws SftpException, FileNotFoundException {
        channel.cd("/");
        File localFile = new File(localDir);
        cdOrCreateDirs(remoteDir);

        // for each file  in local dir
        for (File localChildFile : localFile.listFiles()) {

            // if file is not dir copy file
            if (localChildFile.isFile()) {
                transferFileToRemote(localChildFile.getAbsolutePath(), remoteDir);

            } // if file is dir
            else if (localChildFile.isDirectory()) {

                // mkdir  the remote
                SftpATTRS attrs;
                try {
                    attrs = channel.stat(localChildFile.getName());
                } catch (Exception e) {
                    channel.mkdir(localChildFile.getName());
                    System.out.println(host + " mkdir: " + localChildFile.getName());
                }

                // repeat (recursive)
                transferDirToRemote(localChildFile.getAbsolutePath(), remoteDir + "/" + localChildFile.getName());
                channel.cd("..");
            }
        }

    }

    public void transferFileToRemote(String localFile, String remoteDir) throws SftpException, FileNotFoundException {
        System.out.println(host + " Transfer: " + localFile + " to: " + remoteDir);
        channel.cd(remoteDir);
        channel.put(new FileInputStream(new File(localFile)), new File(localFile).getName(), ChannelSftp.OVERWRITE);
    }

    public void transferToLocal(String remoteDir, String remoteFile, String localDir) throws SftpException, IOException {
        channel.cd(remoteDir);
        byte[] buffer = new byte[1024];
        BufferedInputStream bis = new BufferedInputStream(channel.get(remoteFile));

        File newFile = new File(localDir);
        OutputStream os = new FileOutputStream(newFile);
        BufferedOutputStream bos = new BufferedOutputStream(os);

        int readCount;
        while ((readCount = bis.read(buffer)) > 0) {
            bos.write(buffer, 0, readCount);
        }
        bis.close();
        bos.close();
    }

    void transferSingleFile(byte[] toByteArray, String outFileName, String remoteDir) throws SftpException, IOException {
        channel.cd("/");
        cdOrCreateDirs(remoteDir);
        ByteArrayInputStream bis = new ByteArrayInputStream(toByteArray);
        System.out.println(host + " Transfer: " + outFileName + " to: " + remoteDir);
        channel.put(bis, outFileName, ChannelSftp.OVERWRITE);
        bis.close();
    }
}
