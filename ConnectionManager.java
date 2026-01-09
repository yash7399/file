package com.example.sft;

import java.io.IOException;

import com.hierynomus.smbj.session.Session;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;

public class ConnectionManager {

    private static final ThreadLocal<ChannelSftp> sftpHolder =
            ThreadLocal.withInitial(() -> {
				try {
					return SftpConnection.connect();
				} catch (JSchException e) {
					e.printStackTrace();
				}
				return null;
			});

    private static final ThreadLocal<Session> smbHolder =
            ThreadLocal.withInitial(() -> {
				try {
					return SmbConnection.connect();
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				return null;
			});

    public static ChannelSftp getSftp() {
        return sftpHolder.get();
    }

    public static Session getSmb() {
        return smbHolder.get();
    }

    public static void close() {
        ChannelSftp sftp = sftpHolder.get();
        Session smb = smbHolder.get();

        if (sftp != null) sftp.disconnect();
        if (smb != null)
			try {
				smb.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

        sftpHolder.remove();
        smbHolder.remove();
    }
}
