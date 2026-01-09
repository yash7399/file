package com.example.sft;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

@Component
public class FinalTransfer {

	private static final int BUFFER_SIZE = 8192;
	
	private static final Logger log = LogManager.getLogger(FinalTransfer.class);

	public void transfer(ChannelSftp sftp, Session session, String fileName, String smbShareName, String sourcePath,
			String destinationPath, int time, String department, Map<String, List<String>> missingFiles)
			throws Exception {

		DiskShare share = (DiskShare) session.connectShare("nseit");

		try {
			if (fileName.contains("N.")) {
				transferMultipleFiles(sftp, share, fileName, sourcePath, destinationPath, time, department,
						missingFiles);
			} else {
				transferSingleFile(sftp, share, fileName, sourcePath, destinationPath, time, department, missingFiles);
			}
		} finally {
			share.close();
		}
	}

	private void transferSingleFile(
			ChannelSftp sftp, 
			DiskShare share, 
			String fileName, 
			String sourcePath,
			String destinationPath, 
			int time, 
			String department, 
			Map<String, List<String>> missingFiles)
			throws SftpException, IOException {

		String sftpFilePath = sourcePath + "/" + fileName;

		try {
			sftp.stat(sftpFilePath);
			copyFile(sftp, share, fileName, sftpFilePath, destinationPath, time, department, missingFiles);
			System.out.println(fileName + " transferred successfully");
			log.info(fileName+" Transfered sucessfully");
			
			
		}

		catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				missingFiles.computeIfAbsent(department, k -> new ArrayList<>()).add(fileName);
				System.out.println("Missing file is " + fileName);
				log.info(fileName+" added to missing");
			} else {
				throw e;
			}
		}

	}

	private void transferMultipleFiles(ChannelSftp sftp, DiskShare share, String templateFileName, String sourcePath,
			String destinationPath, int time, String department, Map<String, List<String>> missingFiles)
			throws SftpException, Exception {

		String basename = templateFileName.substring(0, templateFileName.lastIndexOf('_'));
//		System.out.println(templateFileName);
//		System.out.println(templateFileName.lastIndexOf('.' + 1));

		String extension = templateFileName.substring(templateFileName.lastIndexOf('.')+1);

		Pattern pattern = buildPattern(templateFileName);
		if (pattern == null) {
			return;
		}

		String glob = basename + "_*" + "." + extension;

		Vector<ChannelSftp.LsEntry> files = sftp.ls(sourcePath + "/" + glob);

		int count = 0;

		for (ChannelSftp.LsEntry entry : files) {

			if (entry.getAttrs().isDir()) {
				continue;
			}

			String currentFileName = entry.getFilename();

			if (pattern.matcher(currentFileName).matches()) {
				String sftpFilePath = sourcePath + "/" + currentFileName;
				copyFile(sftp, share,currentFileName, sftpFilePath, destinationPath, time, department, missingFiles);

				System.out.println(currentFileName + " transferred successfully");
				log.info(currentFileName+" Transfered sucessfully");
				count++;
			}
		}
		
		if (count == 0) {
			missingFiles.computeIfAbsent(department, k -> new ArrayList<>()).add(templateFileName);
			log.info(templateFileName+" added to missing files ");
		}
		else {
			
		}
	}

	private void copyFile(ChannelSftp sftp, DiskShare share, String fileName, String sftpFilePath,
			String destinationPath, int time, String department, Map<String, List<String>> missingFiles)
			throws SftpException, IOException {


		if (!share.folderExists(destinationPath)) {
			share.mkdir(destinationPath);
		}

		String localTempDir = "C:\\Users\\int30\\eclipse-workspace\\Sft\\localFiles"; 
																						
		String localFilePath = localTempDir + "\\" + fileName;
		System.out.println("Local temp path: " + localFilePath);

		boolean downloaded = false;
		boolean uploaded = false;

// Step 1: Download from SFTP to local file
		try (InputStream inputStream = sftp.get(sftpFilePath);
				OutputStream localOutputStream = new FileOutputStream(localFilePath)) {
			
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				localOutputStream.write(buffer, 0, bytesRead);
			}
			localOutputStream.flush();
			downloaded = true;
			System.out.println("Downloaded to local: " + localFilePath);
			
			log.info(fileName+" downloaded locally to  "+localFilePath);
			
		} catch (Exception e) {
			System.err.println("SFTP->Local download failed: " + e.getMessage());
			log.info("SFTP->Local download failed: " + e.getMessage());
			log.error(e);
			e.printStackTrace();
		}

// Step 2: Upload from local file to SMB
		if (downloaded) {
			try (InputStream localInputStream = new FileInputStream(localFilePath);
					com.hierynomus.smbj.share.File smbFile = share.openFile(destinationPath + "\\" + fileName,
							EnumSet.of(AccessMask.FILE_WRITE_DATA, AccessMask.FILE_READ_ATTRIBUTES,
									AccessMask.FILE_APPEND_DATA),
							EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL), SMB2ShareAccess.ALL,
							SMB2CreateDisposition.FILE_OVERWRITE_IF, null);
					OutputStream smbOutputStream = smbFile.getOutputStream()) {
				byte[] buffer = new byte[BUFFER_SIZE];
				int bytesRead;
				while ((bytesRead = localInputStream.read(buffer)) != -1) {
					smbOutputStream.write(buffer, 0, bytesRead);
				}
				smbOutputStream.flush(); // ensure data is pushed
				uploaded = true;
				System.out.println("Uploaded to SMB: " + destinationPath + "\\" + fileName);
				
				log.info("Uploaded to SMB: " + destinationPath + "\\" + fileName);
				
				
			} catch (Exception e) {
				System.err.println("Local->SMB upload failed: " + e.getMessage());
				log.info("Local->SMB upload failed: " + e.getMessage());
				log.error(e);
			}
		}

// Step 3: Delete local temp file only if upload succeeded


		if (uploaded) {
			try {
				java.nio.file.Path p = java.nio.file.Paths.get(localFilePath);
				java.nio.file.Files.deleteIfExists(p);
				System.out.println("Deleted local temp file: " + localFilePath);
				log.info("Deleted local temp file: " + localFilePath);
				
			} catch (Exception e) {
				System.err.println("Failed to delete local temp file: " + e.getMessage());
				log.error("Failed to delete local temp file: " + e.getMessage());
				log.error(e);
			}
		} else {
			System.out.println("Keeping local file for troubleshooting: " + localFilePath);
			log.info("Keeping local file for troubleshooting: " + localFilePath);
		}

	}

	private Pattern buildPattern(String fileName) {

		String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		int digitCount = fileName.lastIndexOf('.') - fileName.lastIndexOf('_') - 1;

		// Only allow N or NN
		if (digitCount > 2 || digitCount <= 0) {
			return null;
		}

		String digitRegex = "[0-9]{" + digitCount + "}";
		String baseName = fileName.substring(0, fileName.lastIndexOf('_'));

		return Pattern.compile("^" + baseName + "_" + digitRegex + "\\." + extension + "$");
	}
}
