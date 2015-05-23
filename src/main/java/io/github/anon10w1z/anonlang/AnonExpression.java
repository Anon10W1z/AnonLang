package io.github.anon10w1z.anonlang;
/*
 * Copyright 2012 Udo Klimaschewski
 *
 * http://UdoJava.com/
 * http://about.me/udo.klimaschewski
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author Udo Klimaschewski and Anon10W1z
 */
public final class AnonExpression {
	/**
	 * The math context used for computation
	 */
	private static MathContext mathContext = MathContext.DECIMAL32;
	/**
	 * The decimal separator
	 */
	private final char DECIMAL_SEPARATOR = '.';
	/**
	 * The minus sign
	 */
	private final char MINUS_SIGN = '-';
	/**
	 * The expression string to evaluate
	 */
	private String expression = null;
	/**
	 * The components of the expression
	 */
	private List<String> rpn = null;

	/**
	 * A map of operator names to operators
	 */
	private Map<String, Operator> operators = new HashMap<>();
	/**
	 * A map of function names to functions
	 */
	private Map<String, Function> functions = new HashMap<>();

	/**
	 * Constructs a new expression
	 *
	 * @param expression The expression string to evaluate
	 */
	private AnonExpression(String expression) {
		this.expression = expression;
		addOperator(new Operator("+", 20, true) {
			@Override
			public BigDecimal evaluate(BigDecimal num1, BigDecimal num2) {
				return num1.add(num2, mathContext);
			}
		});
		addOperator(new Operator("-", 20, true) {
			@Override
			public BigDecimal evaluate(BigDecimal num1, BigDecimal num2) {
				return num1.subtract(num2, mathContext);
			}
		});
		addOperator(new Operator("*", 30, true) {
			@Override
			public BigDecimal evaluate(BigDecimal num1, BigDecimal num2) {
				return num1.multiply(num2, mathContext);
			}
		});
		addOperator(new Operator("/", 30, true) {
			@Override
			public BigDecimal evaluate(BigDecimal num1, BigDecimal num2) {
				return num1.divide(num2, mathContext);
			}
		});
		addOperator(new Operator("%", 30, true) {
			@Override
			public BigDecimal evaluate(BigDecimal num1, BigDecimal num2) {
				return num1.remainder(num2, mathContext);
			}
		});
		addOperator(new Operator("^", 40, false) {
			@Override
			public BigDecimal evaluate(BigDecimal num1, BigDecimal num2) {
				int signOf2 = num2.signum();
				double dn1 = num1.doubleValue();
				num2 = num2.multiply(new BigDecimal(signOf2));
				BigDecimal remainderOf2 = num2.remainder(BigDecimal.ONE);
				BigDecimal n2IntPart = num2.subtract(remainderOf2);
				BigDecimal intPow = num1.pow(n2IntPart.intValueExact(), mathContext);
				BigDecimal doublePow = new BigDecimal(Math.pow(dn1, remainderOf2.doubleValue()));

				BigDecimal result = intPow.multiply(doublePow, mathContext);
				if (signOf2 == -1)
					result = BigDecimal.ONE.divide(result, mathContext.getPrecision(), RoundingMode.HALF_UP);
				return result;
			}
		});

		addFunction(new Function("random", 0) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				double d = Math.random();
				return new BigDecimal(d, mathContext);
			}
		});
		addFunction(new Function("randomBetween", 2) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				double rangeMin = parameters.get(0).doubleValue();
				double rangeMax = parameters.get(1).doubleValue();
				double d = rangeMin + (rangeMax - rangeMin) * Math.random();
				return new BigDecimal(d, mathContext);
			}
		});
		addFunction(new Function("sin", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				double d = Math.sin(Math.toRadians(parameters.get(0).doubleValue()));
				return new BigDecimal(d, mathContext);
			}
		});
		addFunction(new Function("cos", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				double d = Math.cos(Math.toRadians(parameters.get(0).doubleValue()));
				return new BigDecimal(d, mathContext);
			}
		});
		addFunction(new Function("tan", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				double d = Math.tan(Math.toRadians(parameters.get(0).doubleValue()));
				return new BigDecimal(d, mathContext);
			}
		});
		addFunction(new Function("sinh", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				double d = Math.sinh(Math.toRadians(parameters.get(0).doubleValue()));
				return new BigDecimal(d, mathContext);
			}
		});
		addFunction(new Function("cosh", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				double d = Math.cosh(Math.toRadians(parameters.get(0).doubleValue()));
				return new BigDecimal(d, mathContext);
			}
		});
		addFunction(new Function("tanh", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				double d = Math.tanh(Math.toRadians(parameters.get(0).doubleValue()));
				return new BigDecimal(d, mathContext);
			}
		});
		addFunction(new Function("rad", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				double d = Math.toRadians(parameters.get(0).doubleValue());
				return new BigDecimal(d, mathContext);
			}
		});
		addFunction(new Function("deg", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				double d = Math.toDegrees(parameters.get(0).doubleValue());
				return new BigDecimal(d, mathContext);
			}
		});
		addFunction(new Function("max", 2) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				BigDecimal v1 = parameters.get(0);
				BigDecimal v2 = parameters.get(1);
				return v1.compareTo(v2) > 0 ? v1 : v2;
			}
		});
		addFunction(new Function("min", 2) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				BigDecimal v1 = parameters.get(0);
				BigDecimal v2 = parameters.get(1);
				return v1.compareTo(v2) < 0 ? v1 : v2;
			}
		});
		addFunction(new Function("abs", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				return parameters.get(0).abs(mathContext);
			}
		});
		addFunction(new Function("log", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				double d = Math.log(parameters.get(0).doubleValue());
				return new BigDecimal(d, mathContext);
			}
		});
		addFunction(new Function("log10", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				double d = Math.log10(parameters.get(0).doubleValue());
				return new BigDecimal(d, mathContext);
			}
		});
		addFunction(new Function("round", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				BigDecimal toRound = parameters.get(0);
				return toRound.setScale(0, mathContext.getRoundingMode());
			}
		});
		addFunction(new Function("floor", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				BigDecimal toRound = new BigDecimal(parameters.get(0).toString(), mathContext);
				return toRound.setScale(0, RoundingMode.FLOOR);
			}
		});
		addFunction(new Function("ceiling", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				BigDecimal toRound = parameters.get(0);
				return toRound.setScale(0, RoundingMode.CEILING);
			}
		});
		addFunction(new Function("sqrt", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				BigDecimal x = parameters.get(0);
				if (x.compareTo(BigDecimal.ZERO) == 0)
					return new BigDecimal(0);
				BigInteger n = x.movePointRight(mathContext.getPrecision() << 1).toBigInteger();

				int bits = (n.bitLength() + 1) >> 1;
				BigInteger ix = n.shiftRight(bits);
				BigInteger ixPrev;

				do {
					ixPrev = ix;
					ix = ix.add(n.divide(ix)).shiftRight(1);
					Thread.yield();
				} while (ix.compareTo(ixPrev) != 0);

				return new BigDecimal(ix, mathContext.getPrecision());
			}
		});
		addFunction(new Function("evaluate", 1) {
			@Override
			public BigDecimal evaluate(List<BigDecimal> parameters) {
				return parameters.get(0);
			}
		});
	}

	/**
	 * Evaluates the given expression string and returns the result
	 *
	 * @param expression The expression string to evaluate
	 *
	 * @return The result of the evaluation
	 */
	public static Object evaluate(String expression) {
		try {
			return Integer.parseInt(expression);
		} catch (Exception e) {
			try {
				return Double.parseDouble(expression);
			} catch (Exception e1) {
				return new AnonExpression(expression).evaluate();
			}
		}
	}

	/**
	 * Returns whether or not the given string is a number
	 *
	 * @param string The string to test
	 *
	 * @return Whether of not the given string is a number
	 */
	private boolean isNumber(String string) {
		if (string.charAt(0) == MINUS_SIGN && string.length() == 1)
			return false;
		for (char c : string.toCharArray())
			if (!Character.isDigit(c) && c != MINUS_SIGN && c != DECIMAL_SEPARATOR)
				return false;
		return true;
	}

	/**
	 * Converts the given expression string to an RPN expression
	 *
	 * @param expression The expression string to evaluate
	 *
	 * @return The result of the evaluation
	 */
	private List<String> shuntingYard(String expression) {
		List<String> outputQueue = new ArrayList<>();
		Stack<String> stack = new Stack<>();

		Tokenizer tokenizer = new Tokenizer(expression);

		String previousToken = null;
		while (tokenizer.hasNext()) {
			String token = tokenizer.next();
			if (isNumber(token))
				outputQueue.add(token);
			else if (functions.containsKey(token.toLowerCase())) {
				stack.push(token.toLowerCase());
			} else if (Character.isLetter(token.charAt(0)))
				stack.push(token);
			else if (",".equals(token)) {
				while (!stack.isEmpty() && !"(".equals(stack.peek()))
					outputQueue.add(stack.pop());
				if (stack.isEmpty())
					break;
			} else if (operators.containsKey(token)) {
				Operator o1 = operators.get(token);
				String token2 = stack.isEmpty() ? null : stack.peek();
				while (operators.containsKey(token2) && ((o1.isLeftAssociative() && o1.getPrecedence() <= operators.get(token2).getPrecedence()) || (o1.getPrecedence() < operators.get(token2).getPrecedence()))) {
					outputQueue.add(stack.pop());
					token2 = stack.isEmpty() ? null : stack.peek();
				}
				stack.push(token);
			} else if ("(".equals(token)) {
				if (previousToken != null && isNumber(previousToken))
					break;
				stack.push(token);
			} else if (")".equals(token)) {
				while (!stack.isEmpty() && !"(".equals(stack.peek()))
					outputQueue.add(stack.pop());
				if (stack.isEmpty())
					throw new RuntimeException("Mismatched parentheses");
				stack.pop();
				if (!stack.isEmpty() && functions.containsKey(stack.peek().toUpperCase()))
					outputQueue.add(stack.pop());
			}
			previousToken = token;
		}
		while (!stack.isEmpty()) {
			String element = stack.pop();
			if ("(".equals(element) || ")".equals(element))
				throw new RuntimeException("Mismatched parentheses");
			if (!operators.containsKey(element))
				throw new RuntimeException("Unknown operator or function: " + element);
			outputQueue.add(element);
		}
		return outputQueue;
	}

	/**
	 * Evaluates this expression
	 *
	 * @return The evaluation result of this expression
	 */
	private Object evaluate() {
		Stack<BigDecimal> stack = new Stack<>();
		for (String token : getRPN()) {
			if (operators.containsKey(token)) {
				BigDecimal num1 = stack.pop();
				BigDecimal num2 = stack.pop();
				stack.push(operators.get(token).evaluate(num2, num1));
			} else if (functions.containsKey(token.toUpperCase())) {
				Function function = functions.get(token.toUpperCase());
				ArrayList<BigDecimal> parameters = new ArrayList<>(function.getTotalParameters());
				for (int i = 0; i < function.totalParameters; ++i)
					parameters.add(0, stack.pop());
				BigDecimal functionResult = function.evaluate(parameters);
				stack.push(functionResult);
			} else {
				stack.push(new BigDecimal(token, mathContext));
			}
		}
		return stack.pop().stripTrailingZeros();
	}

	/**
	 * Adds an operator to this expression
	 *
	 * @param operator The operator to add
	 */
	private void addOperator(Operator operator) {
		operators.put(operator.getOperation(), operator);
	}

	/**
	 * Adds a function to this expression
	 *
	 * @param function The function to add
	 */
	private void addFunction(Function function) {
		functions.put(function.getName(), function);
	}

	/**
	 * Returns the RPN evaluation of this expression
	 *
	 * @return The result of the evaluation
	 */
	private List<String> getRPN() {
		if (rpn == null)
			rpn = shuntingYard(this.expression);
		return rpn;
	}

	/**
	 * A function in an expression
	 */
	private abstract class Function {
		/**
		 * The name of the function
		 */
		private String name;
		/**
		 * The number of parameters the function takes
		 */
		private int totalParameters;

		/**
		 * Constructs a function with the given name and total number of parameters
		 *
		 * @param name            The name of this function
		 * @param totalParameters The number of total parameters this function takes
		 */
		public Function(String name, int totalParameters) {
			this.name = name.toUpperCase();
			this.totalParameters = totalParameters;
		}

		/**
		 * Returns the name of this function
		 *
		 * @return The name of this function
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the total number of parameters this function takes
		 *
		 * @return The total number of parameters this function takes
		 */
		public int getTotalParameters() {
			return totalParameters;
		}

		/**
		 * Evaluates this function and returns the result
		 *
		 * @param parameters The parameters of this function
		 *
		 * @return The result of the evaluation
		 */
		public abstract BigDecimal evaluate(List<BigDecimal> parameters);
	}

	/**
	 * An operator in an expression
	 */
	private abstract class Operator {
		/**
		 * The operator as a string
		 */
		private String operation;
		/**
		 * The precedence this operator has relative to other operators
		 */
		private int precedence;
		/**
		 * Whether or not this operator is left associative
		 */
		private boolean leftAssociative;

		/**
		 * Constructs an operator with the given properties
		 *
		 * @param operation       The operator as a string
		 * @param precedence      The precedence of this operator
		 * @param leftAssociative Whether of not this operator is left associative
		 */
		public Operator(String operation, int precedence, boolean leftAssociative) {
			this.operation = operation;
			this.precedence = precedence;
			this.leftAssociative = leftAssociative;
		}

		/**
		 * Returns this operator as a string
		 *
		 * @return This operator as a string
		 */
		public String getOperation() {
			return operation;
		}

		/**
		 * Returns the precedence of this operator
		 *
		 * @return The precedence of this operator
		 */
		public int getPrecedence() {
			return precedence;
		}

		/**
		 * Returns whether of not this operator is left associative
		 *
		 * @return Whether of not this operator is left associative
		 */
		public boolean isLeftAssociative() {
			return leftAssociative;
		}

		/**
		 * Evaluates this operator on the two given BigDecimals
		 *
		 * @param num1 The first decimal
		 * @param num2 The second decimal
		 *
		 * @return The result of the evaluation
		 */
		public abstract BigDecimal evaluate(BigDecimal num1, BigDecimal num2);
	}

	/**
	 * A simple tokenizer
	 */
	private class Tokenizer implements Iterator<String> {
		private int pos = 0;
		private String input;
		private String previousToken;

		public Tokenizer(String input) {
			this.input = input.trim();
		}

		@Override
		public boolean hasNext() {
			return (pos < input.length());
		}

		private char peekNextChar() {
			if (pos < (input.length() - 1)) {
				return input.charAt(pos + 1);
			} else {
				return 0;
			}
		}

		@Override
		public String next() {
			StringBuilder token = new StringBuilder();
			if (pos >= input.length()) {
				return previousToken = null;
			}
			char ch = input.charAt(pos);
			while (Character.isWhitespace(ch) && pos < input.length())
				ch = input.charAt(++pos);
			if (Character.isDigit(ch)) {
				while ((Character.isDigit(ch) || ch == DECIMAL_SEPARATOR) && (pos < input.length())) {
					token.append(input.charAt(pos++));
					ch = pos == input.length() ? 0 : input.charAt(pos);
				}
			} else if (ch == MINUS_SIGN && Character.isDigit(peekNextChar()) && ("(".equals(previousToken) || ",".equals(previousToken) || previousToken == null || operators.containsKey(previousToken))) {
				token.append(MINUS_SIGN);
				pos++;
				token.append(next());
			} else if (Character.isLetter(ch) || (ch == '_')) {
				while ((Character.isLetter(ch) || Character.isDigit(ch) || (ch == '_')) && (pos < input.length())) {
					token.append(input.charAt(pos++));
					ch = pos == input.length() ? 0 : input.charAt(pos);
				}
			} else if (ch == '(' || ch == ')' || ch == ',') {
				token.append(ch);
				pos++;
			} else {
				while (!Character.isLetter(ch) && !Character.isDigit(ch) && ch != '_' && !Character.isWhitespace(ch) && ch != '(' && ch != ')' && ch != ',' && (pos < input.length())) {
					token.append(input.charAt(pos));
					pos++;
					ch = pos == input.length() ? 0 : input.charAt(pos);
					if (ch == MINUS_SIGN)
						break;
				}
			}
			return token.toString();
		}

		@Override
		public void remove() {
			//unsupported operation
		}
	}
}