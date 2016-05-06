package com.sheffield.instrumenter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileHandler {

	public static void writeToFile(File file, String data) throws IOException {
		if (file.getParentFile() != null) {
			file.getParentFile().mkdirs();
		}
		FileWriter fw = new FileWriter(file);
		try {
			fw.write(data);
		} catch (IOException e) {
			throw e;
		} finally {
			fw.close();
		}
	}

	@Deprecated
	public static File generateFile() {
		File file = new File(""+System.currentTimeMillis());
		return file;
	}

	public static File generateFile(String subdirectory) {
		File file = new File(subdirectory + "/" + System.currentTimeMillis());
		return file;
	}


	public static File generateFileWithName(String subdirectory) {
		File file = new File(subdirectory);
		return file;
	}

	public static File[] getFiles(String path) {
		return new File(path).listFiles();
	}

	public static String readFile(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()));
	}

	public static void appendToFile(File file, String data) throws IOException {
		Files.write(Paths.get(file.getAbsolutePath()), data.getBytes(), StandardOpenOption.APPEND);
	}
}
