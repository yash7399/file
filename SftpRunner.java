package com.example.sft;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.sft.config.FileConfig;
import com.example.sft.constants.*;

import com.hierynomus.smbj.session.Session;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

@Component
public class SftpRunner implements CommandLineRunner {
	
	Properties prop=Prop.getProp();

	private static final Logger log = LogManager.getLogger(SftpRunner.class);

	@Autowired
	SftpConnection sftpConnection;

	@Autowired
	SmbConnection smbConnection;
//
	@Autowired
	Util util;
	
	@Autowired
	Emails emails;
//
	@Autowired
	FinalTransfer finalTransfer;

//	Map<String, List<String>> missingFiles = new HashMap<>();
	private final ConcurrentMap<String, List<String>> missingFiles = new ConcurrentHashMap<>();

	@Override
	public void run(String... str) throws InterruptedException, Exception {

		log.info("***********APP STARTED**********************");
		
		ExecutorService threadPool = Executors.newFixedThreadPool(2);

		Map<String, List<FileConfig>> departmentFiles = util.getMap();

		for (Map.Entry<String, List<FileConfig>> entry : departmentFiles.entrySet()) {
			String department = entry.getKey();
			List<FileConfig> records = entry.getValue();

			threadPool.execute(() -> {
				log.info(department+" Department execution started ");
				try {
					process(department, records,str);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			});

		}

		threadPool.shutdown(); // No more tasks accepted
		try {
			if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
				threadPool.shutdownNow(); // Force shutdown if tasks take too long
			}
		} catch (InterruptedException e) {
			threadPool.shutdownNow();
		}

		System.out.println("Missing files");
		emails.filesNotFound(missingFiles,str[0]);
		
		emails.connectionIssue(missingFiles,str[0]);
	
		

	}

	public void process(String department, List<FileConfig> records,String... str

	) throws Exception {

		ChannelSftp sftp = ConnectionManager.getSftp();
		Session session = ConnectionManager.getSmb();
		records.forEach((record) -> {

			String fileName = util.getFileNameWithDate(record.getFilename(),record.getTime(),str[0]);
			System.out.println(fileName);
			log.info("Transfering File ->"+fileName+ " of department "+ department);
			try {
				finalTransfer.transfer(sftp, session, fileName, "sharename", record.getSource(),
						record.getDestination(), record.getTime(), department, missingFiles);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		});

	}
}
