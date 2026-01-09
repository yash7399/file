package com.example.sft.config;

import java.util.List;
import java.util.Map;

public class DepartmentConfig {
	private Map<String,List<FileConfig>> department;

	public Map<String, List<FileConfig>> getDepartment() {
		return department;
	}

	public void setDepartment(Map<String, List<FileConfig>> department) {
		this.department = department;
	}
	
	
}
