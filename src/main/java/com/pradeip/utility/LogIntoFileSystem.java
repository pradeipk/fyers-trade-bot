package com.pradeip.utility;

import java.io.File;

public class LogIntoFileSystem {
	
	
	public void log(String message) {
		
		File file = new File("log.txt");
		// Check if file exists, if not create it
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				System.out.println("Error creating log file: " + e.getMessage());
			}
		}
		
		
		logToFileSystem(file, message);
		
		
	}

	//write to the file with the given message to new line
	private void logToFileSystem(File file, String message) {
		try {
			java.nio.file.Files.write(file.toPath(), (message + System.lineSeparator()).getBytes(), java.nio.file.StandardOpenOption.APPEND);
		} catch (Exception e) {
			System.out.println("Error writing to log file: " + e.getMessage());
		}
	}
	
	
	

}
