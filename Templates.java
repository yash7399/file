package com.example.sft.templates;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.sft.constants.GlobalConstants;




@Component
public class Templates {
	
	public String filesBody(List<String> files, String batchDate) {

        try {
            String template = Files.readString(
                    Path.of(GlobalConstants.filesBodyPath)
            );

            String fileList = buildFileList(files);

            return template
                    .replace("${batchDate}", batchDate)
                    .replace("${files}", fileList);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to build email body", e
            );
        }
    }

    private String buildFileList(List<String> files) {
        StringBuilder sb = new StringBuilder();
        int i=1;
        for (String file : files) {
            sb.append("> ").append(file).append("\n");
        }
        return sb.toString();
    }
	
    
    public String connectionBody(String batchDate) {
    	try {
            String template = Files.readString(
                    Path.of(GlobalConstants.connectionBodyPath)
            );
            return template
                    .replace("${batchDate}", batchDate);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to build email body", e
            );
        }
    }
}
