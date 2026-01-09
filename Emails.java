package com.example.sft;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.billdeskpro.EmailController.MailSenderController;
import com.example.sft.constants.GlobalConstants;
import com.example.sft.templates.Templates;

@Component
public class Emails {

	static Properties prop = Prop.getProp();

	@Autowired
	Templates templates;

	public void filesNotFound(ConcurrentMap<String, List<String>> missingFiles,String batchDate) {

		missingFiles.forEach((depart, files) -> {
			String toEmail = prop.getProperty("email." + depart);
			String subject = GlobalConstants.filesSubject;

			String body = templates.filesBody(files,batchDate);

			String[] args = { "NCDEX", null, null,
					"C:\\Users\\int30\\eclipse-workspace\\Sft\\src\\main\\resources\\application.properties", "n", null,
					null, subject, body };

			System.out.println(
					"Email is send to -> " + toEmail + "\n Subject is -> " + subject + "\n Body is -> " + body);

			try {
				MailSenderController.main(args);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

	}

	public  void connectionIssue(ConcurrentMap<String, List<String>> missingFiles,String batchDate) {

		String toEmail = "";
		for (String depart : missingFiles.keySet()) {
			toEmail += prop.getProperty("email." + depart) + ",";
		}

		if (toEmail.endsWith(",")) {
			toEmail = toEmail.substring(0, toEmail.length() - 1);
		}
		
		String subject=GlobalConstants.connectionSubject;
		String body = templates.connectionBody(batchDate);
		
		String[] args = { "NCDEX", null, null,
				"C:\\Users\\int30\\eclipse-workspace\\Sft\\src\\main\\resources\\application.properties", "n", null,
				null, subject, body , toEmail};
		
		System.out.println(
				"Email is send to -> " + toEmail + "\n Subject is -> " + subject + "\n Body is -> " + body);
		
		try {
			MailSenderController.main(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
