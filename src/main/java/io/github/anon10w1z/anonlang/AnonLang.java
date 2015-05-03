package io.github.anon10w1z.anonlang;

import io.github.anon10w1z.anonlang.exceptions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The interpreter of AnonLang
 */
public final class AnonLang {
	/**
	 * Maps variable names to their variables
	 */
	public static Map<String, AnonVariable> stringToVariableMap = new HashMap<>();

	/**
	 * A list of all line processors
	 */
	public static List<LineProcessor> lineProcessors = new ArrayList<>();
	public static int currentIndex;
	/**
	 * A list of lines to skip when executed by the main method (used for repeat loops)
	 */
	public static List<Integer> linesToSkip = new ArrayList<>();
	/**
	 * The current list of lines to process
	 */
	private static List<String> currentLines = new ArrayList<>();

	static {
		addLineProcessor(new LineProcessor() {
			@Override
			public boolean processLineNoCheck(String line) {
				String toWrite = line.replaceFirst("write", "").trim();
				for (String string : stringToVariableMap.keySet())
					toWrite = toWrite.replaceAll('&' + string + '&', stringToVariableMap.get(string).getValue().toString());
				toWrite = AnonExpression.evaluate(toWrite).toString();
				String[] toWriteArray = toWrite.split("&conc&");
				toWrite = "";
				for (String string : toWriteArray)
					toWrite += !string.equals(toWriteArray[0]) ? " " + AnonExpression.evaluate(string.trim()) : AnonExpression.evaluate(string.trim()).toString();
				System.out.print(toWrite);
				return true;
			}

			@Override
			public boolean canProcessLine(String line) {
				return line.startsWith("write ");
			}
		});
		addLineProcessor(new LineProcessor() {
			@Override
			public boolean processLineNoCheck(String line) {
				if (line.equals("writeln"))
					System.out.println();
				else {
					String toWrite = line.replaceFirst("writeln", "").trim();
					for (String string : stringToVariableMap.keySet())
						toWrite = toWrite.replaceAll('&' + string + '&', stringToVariableMap.get(string).getValue().toString());
					toWrite = AnonExpression.evaluate(toWrite).toString();
					String[] toWriteArray = toWrite.split("&conc&");
					toWrite = "";
					for (String string : toWriteArray)
						toWrite += !string.equals(toWriteArray[0]) ? " " + AnonExpression.evaluate(string.trim()) : AnonExpression.evaluate(string.trim()).toString();
					System.out.println(toWrite);
				}
				return true;
			}

			@Override
			public boolean canProcessLine(String line) {
				return line.startsWith("writeln");
			}
		});
		addLineProcessor(new LineProcessor() {
			@Override
			public boolean processLineNoCheck(String line) {
				String declaration = line.replaceFirst("var", "").trim();
				String variableName = "";
				for (int i = 0; i < declaration.length(); ++i) {
					if (declaration.charAt(i) == '=') {
						variableName = declaration.substring(0, i).trim();
						break;
					}
				}
				if (!stringToVariableMap.containsKey(variableName) && !variableName.equals("") && !variableName.contains(" ")) {
					String valueString = AnonExpression.evaluate(declaration.replaceFirst(variableName, "").replaceFirst("=", "").trim()).toString();
					if (valueString.equals(""))
						throw new MalformedDeclarationException("Initial value for variable " + variableName + " not set");
					Object value = parseVariable(valueString);
					setVariable(variableName, value);
					return true;
				} else throw new MalformedDeclarationException("Illegal variable declaration: " + variableName);
			}

			@Override
			public boolean canProcessLine(String line) {
				return line.startsWith("var ");
			}
		});
		addLineProcessor(new LineProcessor() {
			@Override
			public boolean processLineNoCheck(String line) {
				String variableName = line.replaceFirst("\\++", "").trim();
				for (String variableName2 : stringToVariableMap.keySet())
					if (variableName.equals(variableName2)) {
						Object variableValue = stringToVariableMap.get(variableName).getValue();
						try {
							int i = (Integer) variableValue;
							setVariable(variableName, i + 1);
							return true;
						} catch (Exception e) {
							try {
								double d = (Double) variableValue;
								setVariable(variableName, d + 1);
								return true;
							} catch (Exception e1) {
								throw new MalformedPrefixException("Tried to increment non-numeric variable " + variableName);
							}
						}
					}
				throw new MalformedPrefixException("Tried to increment non-existent variable " + variableName);
			}

			@Override
			public boolean canProcessLine(String line) {
				return line.startsWith("++");
			}
		});
		addLineProcessor(new LineProcessor() {
			@Override
			public boolean processLineNoCheck(String line) {
				String variableName = line.replaceFirst("--", "").trim();
				for (String variableName2 : stringToVariableMap.keySet())
					if (variableName.equals(variableName2)) {
						Object variableValue = stringToVariableMap.get(variableName).getValue();
						try {
							int i = (Integer) variableValue;
							setVariable(variableName, i - 1);
							return true;
						} catch (Exception e) {
							try {
								double d = (Double) variableValue;
								setVariable(variableName, d - 1);
								return true;
							} catch (Exception e1) {
								throw new MalformedPrefixException("Tried to decrement non-numeric variable " + variableName);
							}
						}
					}
				throw new MalformedPrefixException("Tried to decrement non-existent variable " + variableName);
			}

			@Override
			public boolean canProcessLine(String line) {
				return line.startsWith("--");
			}
		});
		addLineProcessor(new LineProcessor() {
			@Override
			public boolean processLineNoCheck(String line) {
				String repeatAmountString = line.replaceFirst("repeat", "").trim();
				try {
					int repeatAmount = Integer.parseInt(repeatAmountString);
					linesToSkip.add(currentIndex + 1);
					for (int i = 0; i < repeatAmount; ++i)
						processLine(currentIndex + 1, true);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					throw new MalformedRepeatException(repeatAmountString + " is not a valid repeat amount");
				}
			}

			@Override
			public boolean canProcessLine(String line) {
				return line.startsWith("repeat ");
			}
		});
		addLineProcessor(new LineProcessor() {
			@Override
			public boolean processLineNoCheck(String line) {
				String variableName = stringToVariableMap.keySet().stream().filter(string -> line.replaceAll(" ", "").startsWith(string + "=")).findFirst().get();
				String variableValueString = line.replaceAll(" ", "").replaceFirst(variableName + "=", "");
				for (String string : stringToVariableMap.keySet())
					variableValueString = variableValueString.replaceAll('&' + string + '&', stringToVariableMap.get(string).getValue().toString());
				Object variableValue = parseVariable(AnonExpression.evaluate(variableValueString).toString());
				setVariable(variableName, variableValue);
				return true;
			}

			@Override
			public boolean canProcessLine(String line) {
				return stringToVariableMap.keySet().stream().filter(string -> line.replaceAll(" ", "").startsWith(string + "=")).findFirst().isPresent();
			}
		});
	}

	/**
	 * Prevent instantiation of AnonLang
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
				currentLines = Files.lines(Paths.get(fileName)).collect(Collectors.toCollection(ArrayList::new));
				int index = 0;
				for (int i = 0; i < currentLines.size(); ++i)
					processLine(index++, false);
				stringToVariableMap = new HashMap<>();
				linesToSkip = new ArrayList<>();
				System.out.println();
				System.out.println("Finished execution of file " + fileName);
				System.out.println();
			}
		} catch (IOException e) { //do not catch any AnonLangException
			e.printStackTrace();
		}
	}

	/**
	 * Processes a line
	 *
	 * @param index             The index number of the line to process (in the list of lines)
	 * @param inRepeatStatement Whether or not this line is being processed in a repeat loop
	 */
	private static void processLine(int index, boolean inRepeatStatement) {
		if (!inRepeatStatement)
			currentIndex = index;
		if (!linesToSkip.contains(index) || inRepeatStatement) {
			String line = currentLines.get(index);
			boolean lineProcessSuccess = false;
			for (LineProcessor lineProcessor : lineProcessors)
				lineProcessSuccess = lineProcessSuccess || lineProcessor.processLineWithCheck(line);
			if (!lineProcessSuccess)
				throw new MalformedLineException("Could not process line #" + (index + 1));
		}
	}

	/**
	 * Sets the variable with the specified name to the specified value
	 *
	 * @param name  The name of the variable
	 * @param value The value of the variable
	 */
	@SuppressWarnings("unchecked")
	private static void setVariable(String name, Object value) {
		name = name.trim();
		if (stringToVariableMap.containsKey(name)) {
			AnonVariable variable = stringToVariableMap.get(name);
			if (variable.getType() != value.getClass()) {
				String currentTypeName = variable.getType().toString().replaceFirst("class java.lang.", "");
				String newTypeName = variable.getType().toString().replaceFirst("class java.lang.", "");
				throw new IllegalAssignmentException("Variable " + name + " is of type " + currentTypeName + " but was assigned value " + value + " of type " + newTypeName);
			}
		}
		stringToVariableMap.put(name, AnonVariable.of(value));
	}

	/**
	 * Parses a string into an object
	 *
	 * @param string The string to parse
	 *
	 * @return The parsed object
	 */
	private static Object parseVariable(String string) {
		try {
			return Integer.parseInt(string);
		} catch (Exception e) {
			try {
				return Double.parseDouble(string);
			} catch (Exception e1) {
				return string;
			}
		}
	}

	/**
	 * Adds a line processor to the list of line processors
	 *
	 * @param lineProcessor The line processor to add
	 */
	private static void addLineProcessor(LineProcessor lineProcessor) {
		lineProcessors.add(lineProcessor);
	}

	/**
	 * A line processor
	 */
	private abstract static class LineProcessor {
		/**
		 * Processes the given line
		 * @param line The line to process
		 * @return Whether or not processing was successful
		 */
		public boolean processLineWithCheck(String line) {
			line = line.trim();
			return canProcessLine(line) && processLineNoCheck(line);
		}

		/**
		 * Processes the given line without checking if it can
		 * @param line The line to process
		 * @return Whether or not processing was successful
		 */
		public abstract boolean processLineNoCheck(String line);

		/**
		 * Returns whether or not this line processor can process the given line
		 * @param line The line to check
		 * @return Whether or not this line processor can process the given line
		 */
		public abstract boolean canProcessLine(String line);
	}
}
