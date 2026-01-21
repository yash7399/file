package com.example.sft;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

import javax.management.RuntimeErrorException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.sft.config.FileConfig;
import com.example.sft.constants.GlobalConstants;
import com.example.sft.util.ConnectionManager;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.jcraft.jsch.ChannelSftp;

@Component
public class ApplicationRunner implements CommandLineRunner {

	private static final Logger log = LogManager.getLogger(ApplicationRunner.class);

	Properties prop = Prop.getProp();

	private static String batchDate;

	@Autowired
	Util util;

	@Autowired
	Emails emails;

	@Autowired
	FileTransferService File_Transfer_Test;

	private final ConcurrentMap<String, List<String>> missingFiles = new ConcurrentHashMap<>();

	private final ConcurrentMap<String, List<String>> transferedFiles = new ConcurrentHashMap<>();

	private List<String> departments = new ArrayList();

	public static String getBatchDate() {
		return batchDate;
	}

	@Override
	public void run(String... args) {

		this.batchDate = args[0];

		System.out.println(GlobalConstants.log4j2);

		Path path = Paths.get(GlobalConstants.log4j2);

		if (!Files.exists(path)) {
			throw new RuntimeErrorException(null, "Log4j2.xml file does not exists " + path);

		}
		LoggerContext context = (LoggerContext) LogManager.getContext(false);

		URI configUri = Path.of(GlobalConstants.log4j2).toUri();

		context.setConfigLocation(configUri);

		log.info("*********** APPLICATION STARTED ***********");
		System.out.println("*********** APPLICATION STARTED ***********");

		ExecutorService threadPool = Executors.newFixedThreadPool(GlobalConstants.no_of_threads);

		try {

			Map<String, List<FileConfig>> departmentFiles = util.getMap();

			for (Map.Entry<String, List<FileConfig>> entry : departmentFiles.entrySet()) {
				String department = entry.getKey();
				List<FileConfig> records = entry.getValue();

				departments.add(department);

				threadPool.submit(() -> processDepartment(department, records, args));
				
//				System.out.println("Department -> "+department);
//				
//				for(FileConfig record :records) {
//					System.out.println("File -> "+record.getFilename());
//					System.out.println("Source -> "+record.getSource());
//					System.out.println("Share -> "+record.getShare());
//					System.out.println("Desiantion -> "+record.getDestination());
//				}
			}

			threadPool.shutdown();

			if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
				threadPool.shutdownNow();
			}

			emails.filesNotFound(missingFiles, transferedFiles, departments);
			
//			ConnectionManager.close();

			log.info("*********** APPLICATION FINISHED ***********");

			System.exit(1);

		} catch (Exception e) {

			System.err.println(" UNEXPECTED ERROR: " + e.getMessage());
			log.error("Unexpected error", e);

			emails.connectionIssue();
			System.exit(1);
		}
	}

	private void processDepartment(String department, List<FileConfig> records, String... args) {

		ChannelSftp sftp = ConnectionManager.getSftp();
		Session session = ConnectionManager.getSmb();
		log.info("Department execution started:" + department);
		System.out.println("Department execution started:" + department);
		DiskShare share = null;
		
		try {
			share = (DiskShare) session.connectShare(records.get(0).getShare());
		} catch (Exception e) {
			System.out.println("Share " + records.get(0).getShare() + " is not connected");
			log.info("Share " + records.get(0).getShare() + " is not connected");
			emails.connectionIssue();
		}
		for (FileConfig record : records) {

			String fileName = util.getFileNameWithDate(record.getFilename(), record.getTime(), args[0]);
			log.info("Trying to transfer file "+fileName);
			try {
				File_Transfer_Test.transfer(sftp, share, fileName, record.getShare(), record.getSource(),
						record.getDestination(), record.getTime(), department, missingFiles, transferedFiles);

			} catch (Exception e) {

				log.error("File transfer failed for {} in department {}", fileName, department, e);
			}
		}
		String triggerFolder = prop.getProperty("trigger.folder." + department);

		try {
			Trigger.makeTriggerFile(share, triggerFolder);
			System.out.println("Sucessfully created trigger file for " + department);
			log.info("Sucessfully created trigger file for " + department);
		} catch (Exception e) {
			System.out.println("Cannot create trigger file for department " + department);
			log.info("Cannot create trigger file for department " + department);
			e.printStackTrace();
		}
	}
}
