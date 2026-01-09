package com.example.sft;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.example.sft.constants.GlobalConstants;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Component
public class SftpConnection {

	private static final Logger log = LogManager.getLogger(SmbConnection.class);
	private final static Properties prop = Prop.getProp();

	private final static String ip = prop.getProperty("ip");
	private final static int port = Integer.parseInt(prop.getProperty("port"));
	private final static String user = prop.getProperty("user");
	private final static String password = prop.getProperty("pass"); // <-- add this in your properties

//private static final Logger log = LogManager.getLogger(SftpConnection.class);

	public static ChannelSftp connect() throws JSchException {
		Session session = null;
		Channel channel = null;

		JSch jsch = new JSch();

		// Optional but recommended: known hosts + strict checking
//       jsch.setKnownHosts(GlobalConstants.knowHostPath);

		session = jsch.getSession(user, ip, port);

		// Set the password for the session
		session.setPassword(password);

		session.setConfig("StrictHostKeyChecking", "no");

		// Connect with a timeout (10 seconds)
		session.connect();
//       log.info("SFTP connected successfully using password");

		// Open SFTP channel
		channel = session.openChannel("sftp");
		channel.connect();
		System.out.println("NSE connected successfully ");
		log.info("NSE connected successfully ");

		return (ChannelSftp) channel;
	}
}
