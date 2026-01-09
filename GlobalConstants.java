package com.example.sft.constants;

import java.util.Properties;

import com.example.sft.Prop;

public class GlobalConstants {
		
		static Properties prop= Prop.getProp();
	

	 	public static String ip = prop.getProperty("ip");
	    public static String port = prop.getProperty("port");
	    public static String user = prop.getProperty("user");
	    public static String pass = prop.getProperty("pass");
	    public static String sourceFolder = prop.getProperty("sourceFolder");

	    public static String smbHost = prop.getProperty("smbHost");
	    public static String smbUser = prop.getProperty("smbUser");
	    public static String smbPass = prop.getProperty("smbPass");
	    public static String smbDomain = prop.getProperty("smbDomain");
	    
	    public static String filesSubject = prop.getProperty("email.Files.Subject");
	    public static String connectionSubject = prop.getProperty("email.Connection.Subject");
	    
	    public static String jsonPath = prop.getProperty("path.json");
	    public static String filesBodyPath = prop.getProperty("path.filesBody");
	    public static String connectionBodyPath = prop.getProperty("path.connectionBody");

}

