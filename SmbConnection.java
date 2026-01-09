package com.example.sft;

import com.example.sft.constants.GlobalConstants;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;

import java.io.IOException;

//import com.hierynomus.msdfsc;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class SmbConnection {

	static SMBClient client = new SMBClient();
	
	private static final Logger log = LogManager.getLogger(SmbConnection.class);

	public static Session connect() throws Exception {
		

		try {

			Connection connection = client.connect(GlobalConstants.smbHost);

			AuthenticationContext auth = new AuthenticationContext(GlobalConstants.smbUser,
					GlobalConstants.smbPass.toCharArray(), "");

			Session session = connection.authenticate(auth);

			System.out.println("Connected to DC NAS successfully!");
			log.info("Connected to DC NAS successfully!");

			return session;

		} catch (Exception e) {
			try {
				System.out.println("DC connection failed");
				log.info("DC connection failed");
				System.out.println("Trying to connect with DR");
				log.info("Trying to connect with DR");
				Connection connection = client.connect(GlobalConstants.smbHost);

				AuthenticationContext auth = new AuthenticationContext(GlobalConstants.smbUser,
						GlobalConstants.smbPass.toCharArray(), "");

				Session session = connection.authenticate(auth);
				System.out.println("Connected to DR NAS successfully!");
				log.info("Connected to DR NAS successfully!");

				return session;
			}

			catch (Exception ex) {

				throw ex;
			}
		}

	}

}
