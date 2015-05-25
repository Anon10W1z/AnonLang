package io.github.anon10w1z.anonlang;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Formats code written in AnonLang
 */
public final class AnonCodeFormatter {
	private AnonCodeFormatter() {

	}

	public static void main(String[] arguments) {
		if (arguments.length == 0)
			throw new IllegalArgumentException("No AnonLang files specified");
		for (String fileName : arguments) {
			System.out.println("Starting formatting of file " + fileName);
			try {
				Path filePath = Paths.get(fileName);
				String linesCombined = String.join("", Files.readAllLines(filePath));
				String[] linesSplit = linesCombined.split(";");
				List<String> linesList = new ArrayList<>();
				for (String line : linesSplit)
					linesList.add(line.trim());
				PrintWriter writer = new PrintWriter(filePath.toFile());
				writer.print(""); //empty the file
				String lastWritten = "";
				for (String line : linesList) {
					String toWrite = line + ';';
					if (lastWritten.trim().startsWith("repeat")) {
						String tabs = "\t";
						int tabCount = lastWritten.length() - lastWritten.replaceAll("\t", "").length();
						for (int i = 0; i < tabCount; ++i)
							tabs += '\t';
						toWrite = tabs + line + ';';
					}
					writer.println(toWrite);
					lastWritten = toWrite;
				}
				writer.close();
				System.out.println("Finished formatting of file " + fileName);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Formatting of file " + fileName + " failed");
			}
		}
	}
}
