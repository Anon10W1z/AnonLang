package io.github.anon10w1z.anonlang;

import io.github.anon10w1z.anonlang.exceptions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The interpreter of AnonLang
 */
public final class AnonLang {
	/**
	 * The current index of the current line
	 */
	public static int currentIndex;
	/**
	 * Maps variable names to variables
	 */
	private static Map<String, AnonVariable> stringToVariableMap = new HashMap<>();
	/**
	 * Maps global variable names to global variables
	 */
	private static Map<String, AnonVariable> stringToGlobalVariableMap = new HashMap<>();
	/**
	 * A list of all line processors
	 */
	private static List<LineProcessor> lineProcessors = new ArrayList<>();
	/**
	 * A list of lines to skip when executed by the main method (used for repeat loops)
	 */
	private static List<Integer> linesToSkip = new ArrayList<>();
	/**
	 * The current list of lines to process
	 */
	private static List<String> currentLines = new ArrayList<>();

	/**
	 * The name of the current file that is being processed
	 */
	private static String currentFileName;

	/**
	 * A temporary value to keep track of repeats
	 */
	private static int repeatCounter = 0;

	private static boolean formatMode = false;

	/**
	 * Initialize the line processors
	 */
	static {
		addLineProcessor(new LineProcessor() { //write statements
			@Override
			public boolean processLineNoCheck(String line) {
				String toWrite = line.replaceFirst("(?i)write", "").trim();
				for (int i = 0; i < toWrite.length(); ++i)
					if (toWrite.charAt(i) == ' ')
						toWrite = toWrite.replaceFirst(" ", ""); //remove leading spaces
					else break;
				toWrite = parseEverything(toWrite).toString();
				System.out.print(toWrite);
				return true;
			}

			@Override
			public boolean canProcessLine(String line) {
				return line.toLowerCase().startsWith("write ");
			}
		});
		addLineProcessor(new LineProcessor() { //writeln statements
			@Override
			public boolean processLineNoCheck(String line) {
				if (line.equals("writeln"))
					System.out.println();
				else {
					String toWrite = line.replaceFirst("(?i)writeln", "");
					for (int i = 0; i < toWrite.length(); ++i)
						if (toWrite.charAt(i) == ' ')
							toWrite = toWrite.replaceFirst(" ", ""); //remove leading spaces
						else break;
					toWrite = parseEverything(toWrite).toString();
					System.out.println(toWrite);
				}
				return true;
			}

			@Override
			public boolean canProcessLine(String line) {
				return line.toLowerCase().startsWith("writeln");
			}
		});
		addLineProcessor(new LineProcessor() { //variable declarations
			@Override
			public boolean processLineNoCheck(String line) {
				String declaration = line.replaceFirst("(?i)var", "").trim();
				String variableName = "";
				for (int i = 0; i < declaration.length(); ++i) {
					if (declaration.charAt(i) == '=') {
						variableName = declaration.substring(0, i).trim();
						break;
					}
				}
				if (!stringToVariableMap.containsKey(variableName) && !variableName.equals("") && !variableName.contains(" ")) {
					String valueString = parseEverything(declaration.replaceFirst(variableName, "").replaceFirst("=", "").trim()).toString();
					if (valueString.equals(""))
						throw new MalformedDeclarationException("Initial value for variable " + variableName + " not set");
					Object value = parseVariable(valueString);
					setVariable(variableName, value);
					return true;
				} else throw new MalformedDeclarationException("Illegal variable declaration: " + variableName);
			}

			@Override
			public boolean canProcessLine(String line) {
				return line.toLowerCase().startsWith("var ");
			}
		});
		addLineProcessor(new LineProcessor() { //global variable declarations
			@Override
			public boolean processLineNoCheck(String line) {
				String declaration = line.replaceFirst("(?i)global var", "").trim();
				String variableName = "";
				for (int i = 0; i < declaration.length(); ++i) {
					if (declaration.charAt(i) == '=') {
						variableName = declaration.substring(0, i).trim();
						break;
					}
				}
				if (!stringToGlobalVariableMap.containsKey(variableName) && !variableName.equals("") && !variableName.contains(" ")) {
					String valueString = parseEverything(declaration.replaceFirst(variableName, "").replaceFirst("=", "").trim()).toString();
					if (valueString.equals(""))
						throw new MalformedDeclarationException("Initial value for variable " + variableName + " not set");
					Object value = parseVariable(valueString);
					setGlobalVariable(variableName, value);
					return true;
				} else throw new MalformedDeclarationException("Illegal variable declaration: " + variableName);
			}

			@Override
			public boolean canProcessLine(String line) {
				return line.toLowerCase().startsWith("global var ");
			}
		});
		addLineProcessor(new LineProcessor() { //increment prefix
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
				for (String variableName2 : stringToGlobalVariableMap.keySet())
					if (variableName.equals(variableName2)) {
						Object variableValue = stringToGlobalVariableMap.get(variableName).getValue();
						try {
							int i = (Integer) variableValue;
							setGlobalVariable(variableName, i + 1);
							return true;
						} catch (Exception e) {
							try {
								double d = (Double) variableValue;
								setGlobalVariable(variableName, d + 1);
								return true;
							} catch (Exception e1) {
								throw new MalformedPrefixException("Tried to decrement non-numeric variable " + variableName);
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
		addLineProcessor(new LineProcessor() { //decrement prefix
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
				for (String variableName2 : stringToGlobalVariableMap.keySet())
					if (variableName.equals(variableName2)) {
						Object variableValue = stringToGlobalVariableMap.get(variableName).getValue();
						try {
							int i = (Integer) variableValue;
							setGlobalVariable(variableName, i - 1);
							return true;
						} catch (Exception e) {
							try {
								double d = (Double) variableValue;
								setGlobalVariable(variableName, d - 1);
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
		addLineProcessor(new LineProcessor() { //repeat statements
			@Override
			public boolean processLineNoCheck(String line) {
				String repeatAmountString = line.replaceFirst("(?i)repeat", "").trim();
				try {
					int repeatAmount = Integer.parseInt(parseEverything(repeatAmountString).toString());
					if (repeatAmount <= 0)
						throw new MalformedRepeatException(repeatAmount + " is not a valid repeat amount");
					for (int i = 0; i < repeatAmount; ++i) {
						linesToSkip.add(++currentIndex);
						processLine(true);
					}
					return true;
				} catch (NumberFormatException e) {
					throw new MalformedRepeatException(parseEverything(repeatAmountString) + " is not a valid repeat amount");
				}
			}

			@Override
			public boolean canProcessLine(String line) {
				return line.toLowerCase().startsWith("repeat ");
			}
		});
		addLineProcessor(new LineProcessor() { //variable assignments
			@Override
			public boolean processLineNoCheck(String line) {
				Optional<String> optionalVariableName = stringToVariableMap.keySet().stream().filter(string -> line.replaceAll(" ", "").startsWith(string + "=")).findFirst();
				if (!optionalVariableName.isPresent())
					optionalVariableName = stringToGlobalVariableMap.keySet().stream().filter(string -> line.replaceAll(" ", "").startsWith(string + "=")).findFirst();
				String variableName = optionalVariableName.get();
				String variableValueString = line.replaceFirst(variableName, "").trim().replaceFirst("=", "");
				Object variableValue = parseEverything(variableValueString);
				setVariable(variableName, variableValue);
				return true;
			}

			@Override
			public boolean canProcessLine(String line) {
				boolean canProcess = stringToVariableMap.keySet().stream().filter(variableName -> line.replaceFirst(variableName, "").trim().startsWith("=")).findFirst().isPresent();
				canProcess = canProcess || stringToGlobalVariableMap.keySet().stream().filter(variableName -> line.replaceFirst(variableName, "").trim().startsWith("=")).findFirst().isPresent();
				return canProcess;
			}
		});
		addLineProcessor(new LineProcessor() { //comments
			@Override
			protected boolean processLineNoCheck(String line) {
				return true;
			}

			@Override
			protected boolean canProcessLine(String line) {
				return line.startsWith("//");
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
	 * @param arguments An array of paths to AnonLang files
	 */
	public static void main(String[] arguments) {
		if (arguments.length == 0)
			throw new IllegalArgumentException("No execution files specified");
		for (String fileName : arguments) {
			if (fileName.equals("--format")) {
				formatMode = true;
				continue;
			}
			if (formatMode)
				AnonCodeFormatter.main(new String[]{fileName});
			else {
				System.out.println("Starting execution of file " + fileName);
				try {
					Path filePath = Paths.get(fileName);
					String linesCombined = String.join("", Files.readAllLines(filePath));
					String[] linesSplit = linesCombined.split(";");
					for (String line : linesSplit)
						currentLines.add(line.trim());
					currentFileName = filePath.toString();
					currentLines.forEach(line -> processLine(false));
					currentLines.clear(); //reset current lines
					stringToVariableMap.clear(); //reset variables
					linesToSkip.clear(); //reset list of lines to skip
					System.out.println();
					System.out.println("Finished execution of file " + fileName);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Execution of " + fileName + " failed");
				}
			}
		}
	}

	/**
	 * Processes a line
	 * @param inRepeatLoop Whether or not this line is being processed in a repeat loop
	 */
	private static void processLine(boolean inRepeatLoop) {
		if (inRepeatLoop)
			setVariable("loopCounter", repeatCounter++);
		else {
			stringToVariableMap.remove("loopCounter");
			repeatCounter = 0;
		}
		if (!linesToSkip.contains(currentIndex) || inRepeatLoop) {
			String line = currentLines.get(currentIndex);
			line = line.trim();
			boolean lineProcessSuccess = false;
			for (LineProcessor lineProcessor : lineProcessors)
				lineProcessSuccess = lineProcessSuccess || lineProcessor.processLineWithCheck(line);
			if (!lineProcessSuccess)
				throw new MalformedLineException("Could not process line #" + (currentIndex + 1));
		}
		if (inRepeatLoop)
			--currentIndex;
		else ++currentIndex;
	}

	/**
	 * Sets the variable with the specified name to the specified value. <br>
	 * If the variable does not exist, it is created.
	 * @param name  The name of the variable
	 * @param value The value of the variable
	 */
	private static void setVariable(String name, Object value) {
		name = name.trim();
		if (stringToVariableMap.containsKey(name)) {
			AnonVariable variable = stringToVariableMap.get(name);
			if (value.getClass() == Integer.class && variable.getType() == Double.class)
				value = ((Integer) value).doubleValue();
			if (value.getClass() == Double.class && variable.getType() == Integer.class) {
				value = (int) Math.round((Double) value);
			}
			if (variable.getType() != value.getClass()) {
				String currentTypeName = variable.getType().getName().replaceFirst("java.lang.", "");
				String newTypeName = value.getClass().getName().replaceFirst("java.lang.", "");
				throw new IllegalAssignmentException("Variable " + name + " is of type " + currentTypeName + " but was assigned value " + value + " of type " + newTypeName);
			}
			variable.setValue(value);
		} else stringToVariableMap.put(name, AnonVariable.of(value));
	}

	/**
	 * Sets the global variable with the specified name to the specified value. <br>
	 * If the global variable does not exist, it is created.
	 * @param name  The name of the global variable
	 * @param value The value of the global variable
	 */
	private static void setGlobalVariable(String name, Object value) {
		name = name.trim();
		if (stringToGlobalVariableMap.containsKey(name)) {
			AnonVariable variable = stringToGlobalVariableMap.get(name);
			if (value.getClass() == Integer.class && variable.getType() == Double.class)
				value = ((Integer) value).doubleValue();
			if (variable.getType() != value.getClass()) {
				String currentTypeName = variable.getType().getName().replaceFirst("java.lang.", "");
				String newTypeName = value.getClass().getName().replaceFirst("java.lang.", "");
				throw new IllegalAssignmentException("Variable " + name + " is of type " + currentTypeName + " but was assigned value " + value + " of type " + newTypeName);
			}
			variable.setValue(value);
		}
		stringToGlobalVariableMap.put(currentFileName + '.' + name, AnonVariable.of(value));
	}

	/**
	 * Parses EVERYTHING possible from the given string
	 * @param string The string to parse
	 * @return The parsed object
	 */
	private static Object parseEverything(String string) {
		string = string.trim();
		for (String variableName : stringToVariableMap.keySet())
			string = string.replaceAll('&' + variableName + '&', stringToVariableMap.get(variableName).getValue().toString());
		for (String variableName : stringToGlobalVariableMap.keySet())
			string = string.replaceAll('&' + variableName + '&', stringToGlobalVariableMap.get(variableName).getValue().toString());
		String expressionResult = AnonExpression.evaluate(string);
		if (!expressionResult.equals(string))
			return parseVariable(expressionResult);
		String[] splitString = string.split("&conc&");
		String parsedString = "";
		for (String component : splitString)
			parsedString += AnonExpression.evaluate(component);
		return parseVariable(parsedString);
	}

	/**
	 * Parses a string into an object
	 * @param string The string to parse
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
			return canProcessLine(line) && processLineNoCheck(line);
		}

		/**
		 * Processes the given line without checking if it can
		 * @param line The line to process
		 * @return Whether or not processing was successful
		 */
		protected abstract boolean processLineNoCheck(String line);

		/**
		 * Returns whether or not this line processor can process the given line
		 * @param line The line to check
		 * @return Whether or not this line processor can process the given line
		 */
		protected abstract boolean canProcessLine(String line);
	}
}
