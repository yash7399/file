package com.example.sft;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

import org.springframework.stereotype.Component;

import com.example.sft.config.DepartmentConfig;
import com.example.sft.config.FileConfig;
import com.example.sft.constants.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.ChannelSftp;

@Component
public class Util {

	static DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyy");
//	public static Object yesterday;

	public LocalDate getFileDate(String fileName) {

		Pattern DatePattern = Pattern.compile("(\\d{2})(\\d{2})(\\d{4})");

		Matcher matcher = DatePattern.matcher(fileName);

		if (matcher.find()) {
			String dateStr = matcher.group(0);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");

			try {
				return LocalDate.parse(dateStr, formatter);
			} catch (Exception e) {
				System.out.println("Error occured");
			}
		}
		return null;
	}

	public boolean canTransfer(String lastOcurred, int frequency, String fileName) {

		LocalDate lastOcurredDate = LocalDate.parse(lastOcurred, format);

		LocalDate yesterday = LocalDate.now(ZoneId.systemDefault()).minusDays(1);

		long diff = ChronoUnit.DAYS.between(lastOcurredDate, yesterday);

		return diff != 0 && diff % frequency == 0;

	}

	public String getFileNameWithDate(String Filename, int time, String inputDate) {

		DateTimeFormatter inputFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		DateTimeFormatter outputFmt = DateTimeFormatter.ofPattern("ddMMyyyy");

		// Parse, subtract days, format
		LocalDate date = LocalDate.parse(inputDate, inputFmt);
		LocalDate result = date.minusDays(time);
		String resultStr = result.format(outputFmt);

		Filename = Filename.replace("DDMMYYYY", resultStr);

		return Filename;
	}

	public Map<String, List<FileConfig>> getMap() throws Exception {
		ObjectMapper mapper = new ObjectMapper();

		DepartmentConfig config = mapper.readValue(new File(GlobalConstants.jsonPath), DepartmentConfig.class);

		return config.getDepartment();
	}
}
