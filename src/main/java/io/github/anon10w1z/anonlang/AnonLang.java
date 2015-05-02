package io.github.anon10w1z.anonlang;

import io.github.anon10w1z.anonlang.exceptions.IllegalDeclarationException;
import io.github.anon10w1z.anonlang.exceptions.IllegalPrefixException;
import io.github.anon10w1z.anonlang.exceptions.IllegalRepeatAmountException;
import io.github.anon10w1z.anonlang.exceptions.LineProcessException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class AnonLang {
	public static HashMap<String, AnonVariable> stringToVariableMap = new HashMap<>();
	public static ArrayList<Integer> linesToSkip = new ArrayList<>();

	public static void main(String[] args) {
		if (args.length != 1)
			throw new IllegalArgumentException("Wrong number of arguments specified");
		try {
			ArrayList<String> lines = Files.lines(Paths.get(args[0])).collect(Collectors.toCollection(ArrayList::new));
			int index = 0;
			for (int i = 0; i < lines.size(); ++i)
				processLine(lines, index++, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void processLine(ArrayList<String> lines, int index, boolean inRepeatStatement) {
		boolean lineProcessSuccess = false;
		if (!linesToSkip.contains(index) || inRepeatStatement) {
			String line = lines.get(index).trim();
			if (line.startsWith("write ")) {
				String toWrite = line.replaceFirst("write", "").trim();
				for (String string : stringToVariableMap.keySet())
					toWrite = toWrite.replaceAll('"' + string + '"', stringToVariableMap.get(string).getValue().toString());
				toWrite = evaluate(toWrite);
				String[] toWriteArray = toWrite.split("\\+");
				toWrite = "";
				for (String string : toWriteArray)
					toWrite += !string.equals(toWriteArray[0]) ? " " + evaluate(string.trim()) : evaluate(string.trim());
				System.out.print(toWrite);
				lineProcessSuccess = true;
			} else if (line.startsWith("writeln")) {
				if (line.equals("writeln"))
					System.out.println();
				else {
					String toWrite = line.replaceFirst("writeln", "").trim();
					for (String string : stringToVariableMap.keySet())
						toWrite = toWrite.replaceAll('"' + string + '"', stringToVariableMap.get(string).getValue().toString());
					toWrite = evaluate(toWrite);
					String[] toWriteArray = toWrite.split("\\+");
					toWrite = "";
					for (String string : toWriteArray)
						toWrite += !string.equals(toWriteArray[0]) ? " " + evaluate(string.trim()) : evaluate(string.trim());
					System.out.println(toWrite);
					lineProcessSuccess = true;
				}
			}
			else if (line.startsWith("var ")) {
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
						throw new IllegalDeclarationException("Initial value for variable " + variableName + " not set");
					setVariable(variableName, value);
					lineProcessSuccess = true;
				} else throw new IllegalDeclarationException("Illegal variable declaration: " + variableName);
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
								throw new IllegalPrefixException("Tried to increment non-numeric variable " + variableName);
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
								throw new IllegalPrefixException("Tried to decrement non-numeric variable " + variableName);
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
					throw new IllegalRepeatAmountException(repeatAmountString + " is not a legal repeat amount");
				}
			}
			for (String variableName : stringToVariableMap.keySet())
				if (line.replaceAll(" ", "").startsWith(variableName + "=")) {
					String variableValue = line.replaceAll(" ", "").replaceFirst(variableName + "=", "");
					for (String string : stringToVariableMap.keySet())
						variableValue = variableValue.replaceAll(string, stringToVariableMap.get(string).getValue().toString());
					variableValue = evaluate(variableValue);
					String[] variableValueArray = variableValue.split("\\+");
					variableValue = "";
					for (String string : variableValueArray) {
						String toAdd = evaluate(string.trim());
						variableValue += toAdd;
					}
					setVariable(variableName, variableValue);
					lineProcessSuccess = true;
				}
			if (!lineProcessSuccess)
				throw new LineProcessException("Could not process line #" + (index + 1));
		}
	}

	private static void setVariable(String variableName, String value) {
		variableName = variableName.trim();
		value = value.trim();
		try {
			stringToVariableMap.put(variableName, AnonVariable.of(Integer.parseInt(value)));
		} catch (Exception e) {
			try {
				stringToVariableMap.put(variableName, AnonVariable.of(Double.parseDouble(value)));
			} catch (Exception e1) {
				stringToVariableMap.put(variableName, AnonVariable.of(value));
			}
		}
	}

	private static String evaluate(String expression) {
		try {
			return new AnonExpression(expression).evaluate().toString();
		} catch (Exception e) {
			return expression;
		}
	}
}
