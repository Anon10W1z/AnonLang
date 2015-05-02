package io.github.anon10w1z.anonlang;

import io.github.anon10w1z.anonlang.exceptions.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * The interpreter of AnonLang
 */
public final class AnonLang {
	/**
	 * Maps variable names to their variables
	 */
	public static HashMap<String, AnonVariable> stringToVariableMap = new HashMap<>();

	/**
	 * A list of lines to skip when executed by the main method (used for repeat loops)
	 */
	public static ArrayList<Integer> linesToSkip = new ArrayList<>();

	/**
	 * Prevent instantiation
	 */
	private AnonLang() {

	}

	/**
	 * Takes an array of paths to AnonLang files and processes each file
	 *
	 * @param arguments An array of paths to AnonLang files
	 */
	public static void main(String[] arguments) {
		try {
			for (String fileName : arguments) {
				ArrayList<String> lines = Files.lines(Paths.get(fileName)).collect(Collectors.toCollection(ArrayList::new));
				int index = 0;
				for (int i = 0; i < lines.size(); ++i)
					processLine(lines, index++, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Processes a line
	 *
	 * @param lines             The list of lines to process
	 * @param index             The index number of the line to process (in the list of lines)
	 * @param inRepeatStatement Whether or not this line is being processed in a repeat loop
	 */
	private static void processLine(ArrayList<String> lines, int index, boolean inRepeatStatement) {
		boolean lineProcessSuccess = false;
		if (!linesToSkip.contains(index) || inRepeatStatement) {
			String line = lines.get(index).trim();
			if (line.startsWith("write ")) {
				String toWrite = line.replaceFirst("write", "").trim();
				for (String string : stringToVariableMap.keySet())
					toWrite = toWrite.replaceAll('"' + string + '"', stringToVariableMap.get(string).getValue().toString());
				toWrite = AnonExpression.evaluate(toWrite);
				String[] toWriteArray = toWrite.split("\\+");
				toWrite = "";
				for (String string : toWriteArray)
					toWrite += !string.equals(toWriteArray[0]) ? " " + AnonExpression.evaluate(string.trim()) : AnonExpression.evaluate(string.trim());
				System.out.print(toWrite);
				lineProcessSuccess = true;
			} else if (line.startsWith("writeln")) {
				if (line.equals("writeln"))
					System.out.println();
				else {
					String toWrite = line.replaceFirst("writeln", "").trim();
					for (String string : stringToVariableMap.keySet())
						toWrite = toWrite.replaceAll('"' + string + '"', stringToVariableMap.get(string).getValue().toString());
					toWrite = AnonExpression.evaluate(toWrite);
					String[] toWriteArray = toWrite.split("\\+");
					toWrite = "";
					for (String string : toWriteArray)
						toWrite += !string.equals(toWriteArray[0]) ? " " + AnonExpression.evaluate(string.trim()) : AnonExpression.evaluate(string.trim());
					System.out.println(toWrite);
					lineProcessSuccess = true;
				}
			} else if (line.startsWith("var ")) {
				String declaration = line.replaceFirst("var", "").trim();
				String variableName = "";
				for (int i = 0; i < declaration.length(); ++i) {
					if (declaration.charAt(i) == '=') {
						variableName = declaration.substring(0, i).trim();
						break;
					}
				}
				if (!stringToVariableMap.containsKey(variableName) && !variableName.equals("") && !variableName.contains(" ")) {
					String value = declaration.replaceFirst(variableName, "").replaceFirst("=", "").trim();
					if (value.equals(""))
						throw new MalformedDeclarationException("Initial value for variable " + variableName + " not set");
					setVariable(variableName, value);
					lineProcessSuccess = true;
				} else throw new MalformedDeclarationException("Illegal variable declaration: " + variableName);
			} else if (line.startsWith("++")) {
				String variableName = line.replaceFirst("\\++", "").trim();
				for (AnonVariable variable : stringToVariableMap.values()) {
					String variableValue = variable.getValue().toString();
					if (variableValue.equals(variableName)) {
						try {
							int i = Integer.parseInt(variableValue);
							setVariable(variableName, Integer.toString(i + 1));
							lineProcessSuccess = true;
						} catch (Exception e) {
							try {
								double d = Double.parseDouble(variableValue);
								setVariable(variableName, Double.toString(d + 1));
								lineProcessSuccess = true;
							} catch (Exception e1) {
								throw new MalformedPrefixException("Tried to increment non-numeric variable " + variableName);
							}
						}
					}
				}
			} else if (line.startsWith("--")) {
				String variableName = line.replaceFirst("--", "").trim();
				for (String string : stringToVariableMap.keySet()) {
					Object variableValue = stringToVariableMap.get(string).getValue();
					if (string.equals(variableName)) {
						try {
							int i = Integer.parseInt(variableValue.toString());
							setVariable(variableName, Integer.toString(i - 1));
							lineProcessSuccess = true;
						} catch (Exception e) {
							try {
								double d = Double.parseDouble(variableValue.toString());
								setVariable(variableName, Double.toString(d - 1));
								lineProcessSuccess = true;
							} catch (Exception e1) {
								throw new MalformedPrefixException("Tried to decrement non-numeric variable " + variableName);
							}
						}
					}
				}
			} else if (line.startsWith("repeat ")) {
				String repeatAmountString = line.replaceFirst("repeat", "").trim();
				try {
					int repeatAmount = Integer.parseInt(repeatAmountString);
					linesToSkip.add(index + 1);
					for (int i = 0; i < repeatAmount; ++i)
						processLine(lines, index + 1, true);
					lineProcessSuccess = true;
				} catch (Exception e) {
					throw new MalformedRepeatException(repeatAmountString + " is not a valid repeat amount");
				}
			}
			for (String variableName : stringToVariableMap.keySet())
				if (line.replaceAll(" ", "").startsWith(variableName + "=")) {
					String variableValue = line.replaceAll(" ", "").replaceFirst(variableName + "=", "");
					for (String string : stringToVariableMap.keySet())
						variableValue = variableValue.replaceAll(string, stringToVariableMap.get(string).getValue().toString());
					variableValue = AnonExpression.evaluate(variableValue);
					String[] variableValueArray = variableValue.split("\\+");
					variableValue = "";
					for (String string : variableValueArray) {
						String toAdd = AnonExpression.evaluate(string.trim());
						variableValue += toAdd;
					}
					setVariable(variableName, variableValue);
					lineProcessSuccess = true;
				}
			if (!lineProcessSuccess)
				throw new MalformedLineException("Could not process line #" + (index + 1));
		}
	}

	/**
	 * Sets the variable with the specified name to the specified value
	 *
	 * @param variableName The name of the variable
	 * @param value        The value of the variable, in string form
	 */
	@SuppressWarnings("unchecked")
	private static void setVariable(String variableName, String value) {
		variableName = variableName.trim();
		value = value.trim();
		if (stringToVariableMap.containsKey(variableName)) {
			AnonVariable variable = stringToVariableMap.get(variableName);
			if (variable.getType() == Integer.class) {
				try {
					variable.setValue(Integer.parseInt(value));
				} catch (Exception e) {
					throw new IllegalAssignmentException(variableName + " is an integer, but was assigned value " + value);
				}
			} else if (variable.getType() == Double.class) {
				try {
					variable.setValue(Double.parseDouble(value));
				} catch (Exception e) {
					throw new IllegalAssignmentException(variableName + " is a double, but was assigned value " + value);
				}
			} else variable.setValue(value);

		} else
			try {
				stringToVariableMap.put(variableName, AnonVariable.of(Integer.parseInt(value)));
			} catch (Exception e) { //not an integer
				try {
					stringToVariableMap.put(variableName, AnonVariable.of(Double.parseDouble(value)));
				} catch (Exception e1) { //not a number
					stringToVariableMap.put(variableName, AnonVariable.of(value));
				}
			}
	}
}
