import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class AnonLang {
	public static HashMap<String, AnonField> stringToFieldMap = new HashMap<>();

	public static void main(String[] args) {
		if (args.length != 1)
			throw new IllegalArgumentException("Wrong number of arguments specified");
		try {
			Files.lines(Paths.get(args[0])).forEach(AnonLang::processLine);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void processLine(String line) {
		line = line.trim();
		if (line.startsWith("write ")) {
			String toWrite = line.replaceFirst("write", "").trim();
			for (String string : stringToFieldMap.keySet())
				toWrite = toWrite.replaceAll('"' + string + '"', stringToFieldMap.get(string).getValue().toString());
			toWrite = evaluate(toWrite);
			String[] toWriteArray = toWrite.split("\\+");
			toWrite = "";
			for (String string : toWriteArray)
				toWrite += !string.equals(toWriteArray[0]) ? " " + evaluate(string.trim()) : evaluate(string.trim());
			System.out.print(toWrite);
		} else if (line.startsWith("writeln"))
			if (line.equals("writeln"))
				System.out.println();
			else {
				String toWrite = line.replaceFirst("writeln", "").trim();
				for (String string : stringToFieldMap.keySet())
					toWrite = toWrite.replaceAll('"' + string + '"', stringToFieldMap.get(string).getValue().toString());
				toWrite = evaluate(toWrite);
				String[] toWriteArray = toWrite.split("\\+");
				toWrite = "";
				for (String string : toWriteArray)
					toWrite += !string.equals(toWriteArray[0]) ? " " + evaluate(string.trim()) : evaluate(string.trim());
				System.out.println(toWrite);
			}
		else if (line.startsWith("var ")) {
			String declaration = line.replaceFirst("var", "").trim();
			String fieldName = "";
			for (int i = 0; i < declaration.length(); ++i) {
				if (declaration.charAt(i) == '=') {
					fieldName = declaration.substring(0, i).trim();
					break;
				}
			}
			if (!stringToFieldMap.containsKey(fieldName)) {
				String value = declaration.replaceFirst(fieldName, "").replaceFirst("=", "").trim();
				setField(fieldName, value);
			}
		} else if (line.startsWith("++")) {
			String fieldName = line.replaceFirst("\\++", "").trim();
			for (String string : stringToFieldMap.keySet()) {
				Object fieldValue = stringToFieldMap.get(string).getValue();
				if (string.equals(fieldName)) {
					try {
						int i = Integer.parseInt(fieldValue.toString());
						setField(fieldName, String.valueOf(i + 1));
					} catch (Exception e) {
						try {
							double d = Double.parseDouble(fieldValue.toString());
							setField(fieldName, String.valueOf(d + 1));
						} catch (Exception ignored) {

						}
					}
				}
			}
		} else if (line.startsWith("--")) {
			String fieldName = line.replaceFirst("--", "").trim();
			for (String string : stringToFieldMap.keySet()) {
				Object fieldValue = stringToFieldMap.get(string).getValue();
				if (string.equals(fieldName)) {
					try {
						int i = Integer.parseInt(fieldValue.toString());
						setField(fieldName, String.valueOf(i - 1));
					} catch (Exception e) {
						try {
							double d = Double.parseDouble(fieldValue.toString());
							setField(fieldName, String.valueOf(d - 1));
						} catch (Exception ignored) {

						}
					}
				}
			}
		}
		final String finalLine = line;
		stringToFieldMap.keySet().stream().filter(fieldName -> finalLine.replaceAll(" ", "").startsWith(fieldName + "=")).forEach(fieldName -> {
			String fieldValue = finalLine.replaceAll(" ", "").replaceFirst(fieldName + "=", "");
			for (String string : stringToFieldMap.keySet())
				fieldValue = fieldValue.replaceAll(string, stringToFieldMap.get(string).getValue().toString());
			fieldValue = evaluate(fieldValue);
			String[] fieldValueArray = fieldValue.split("\\+");
			fieldValue = "";
			for (String string : fieldValueArray) {
				String toAdd = evaluate(string.trim());
				fieldValue += toAdd;
			}
			setField(fieldName, fieldValue);
		});
	}

	private static void setField(String fieldName, String value) {
		try {
			int i = Integer.parseInt(value);
			stringToFieldMap.put(fieldName, AnonField.of(i));
		} catch (Exception e) {
			try {
				double d = Double.parseDouble(value);
				stringToFieldMap.put(fieldName, AnonField.of(d));
			} catch (Exception e2) {
				stringToFieldMap.put(fieldName, AnonField.of(value));
			}
		}
	}

	private static String evaluate(String expression) {
		try {
			return new Expression(expression).evaluate().toString();
		} catch (Exception e) {
			return expression;
		}
	}
}
