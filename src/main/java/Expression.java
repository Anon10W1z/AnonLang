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
 * 
 */

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

/**
 * <h1>EvalEx - Java Expression Evaluator</h1>
 * <p>
 * <h2>Introduction</h2>
 * EvalEx is a handy expression evaluator for Java, that allows to evaluate simple mathematical and boolean expressions.
 * <br>
 * Key Features:
 * <ul>
 * <li>Uses BigDecimal for calculation and result</li>
 * <li>Single class implementation, very compact</li>
 * <li>No dependencies to external libraries</li>
 * <li>Precision and rounding mode can be set</li>
 * <li>Supports variables</li>
 * <li>Standard boolean and mathematical operators</li>
 * <li>Standard basic mathematical and boolean functions</li>
 * <li>Custom functions and operators can be added at runtime</li>
 * </ul>
 * <br>
 * <h2>Examples</h2>
 * <pre>
 *  BigDecimal result = null;
 *
 *  Expression expression = new Expression("1+1/3");
 *  result = expression.evaluate():
 *  expression.setPrecision(2);
 *  result = expression.evaluate():
 *
 *  result = new Expression("(3.4 + -4.1)/2").evaluate();
 *
 *  result = new Expression("SQRT(a^2 + b^2").with("a","2.4").and("b","9.253").evaluate();
 *
 *  BigDecimal a = new BigDecimal("2.4");
 *  BigDecimal b = new BigDecimal("9.235");
 *  result = new Expression("SQRT(a^2 + b^2").with("a",a).and("b",b).evaluate();
 *
 *  result = new Expression("2.4/PI").setPrecision(128).setRoundingMode(RoundingMode.UP).evaluate();
 *
 *  result = new Expression("random() > 0.5").evaluate();
 *
 *  result = new Expression("not(x<7 || sqrt(max(x,9)) <= 3))").with("x","22.9").evaluate();
 * </pre>
 * <br>
 * <h2>Supported Operators</h2>
 * <table>
 * <tr><th>Mathematical Operators</th></tr>
 * <tr><th>Operator</th><th>Description</th></tr>
 * <tr><td>+</td><td>Additive operator</td></tr>
 * <tr><td>-</td><td>Subtraction operator</td></tr>
 * <tr><td>*</td><td>Multiplication operator</td></tr>
 * <tr><td>/</td><td>Division operator</td></tr>
 * <tr><td>%</td><td>Remainder operator (Modulo)</td></tr>
 * <tr><td>^</td><td>Power operator</td></tr>
 * </table>
 * <br>
 * <table>
 * <tr><th>Boolean Operators<sup>*</sup></th></tr>
 * <tr><th>Operator</th><th>Description</th></tr>
 * <tr><td>=</td><td>Equals</td></tr>
 * <tr><td>==</td><td>Equals</td></tr>
 * <tr><td>!=</td><td>Not equals</td></tr>
 * <tr><td>&lt;&gt;</td><td>Not equals</td></tr>
 * <tr><td>&lt;</td><td>Less than</td></tr>
 * <tr><td>&lt;=</td><td>Less than or equal to</td></tr>
 * <tr><td>&gt;</td><td>Greater than</td></tr>
 * <tr><td>&gt;=</td><td>Greater than or equal to</td></tr>
 * <tr><td>&amp;&amp;</td><td>Boolean and</td></tr>
 * <tr><td>||</td><td>Boolean or</td></tr>
 * </table>
 * *Boolean operators result always in a BigDecimal value of 1 or 0 (zero). Any non-zero value is treated as a _true_ value. Boolean _not_ is implemented by a function.
 * <br>
 * <h2>Supported Functions</h2>
 * <table>
 * <tr><th>Function<sup>*</sup></th><th>Description</th></tr>
 * <tr><td>NOT(<i>expression</i>)</td><td>Boolean negation, 1 (means true) if the expression is not zero</td></tr>
 * <tr><td>IF(<i>condition</i>,<i>value_if_true</i>,<i>value_if_false</i>)</td><td>Returns one value if the condition evaluates to true or the other if it evaluates to false</td></tr>
 * <tr><td>RANDOM()</td><td>Produces a random number between 0 and 1</td></tr>
 * <tr><td>MIN(<i>e1</i>,<i>e2</i>)</td><td>Returns the smaller of both expressions</td></tr>
 * <tr><td>MAX(<i>e1</i>,<i>e2</i>)</td><td>Returns the bigger of both expressions</td></tr>
 * <tr><td>ABS(<i>expression</i>)</td><td>Returns the absolute (non-negative) value of the expression</td></tr>
 * <tr><td>ROUND(<i>expression</i>,precision)</td><td>Rounds a value to a certain number of digits, uses the current rounding mode</td></tr>
 * <tr><td>FLOOR(<i>expression</i>)</td><td>Rounds the value down to the nearest integer</td></tr>
 * <tr><td>CEILING(<i>expression</i>)</td><td>Rounds the value up to the nearest integer</td></tr>
 * <tr><td>LOG(<i>expression</i>)</td><td>Returns the natural logarithm (base e) of an expression</td></tr>
 * <tr><td>LOG10(<i>expression</i>)</td><td>Returns the common logarithm (base 10) of an expression</td></tr>
 * <tr><td>SQRT(<i>expression</i>)</td><td>Returns the square root of an expression</td></tr>
 * <tr><td>SIN(<i>expression</i>)</td><td>Returns the trigonometric sine of an angle (in degrees)</td></tr>
 * <tr><td>COS(<i>expression</i>)</td><td>Returns the trigonometric cosine of an angle (in degrees)</td></tr>
 * <tr><td>TAN(<i>expression</i>)</td><td>Returns the trigonometric tangens of an angle (in degrees)</td></tr>
 * <tr><td>SINH(<i>expression</i>)</td><td>Returns the hyperbolic sine of a value</td></tr>
 * <tr><td>COSH(<i>expression</i>)</td><td>Returns the hyperbolic cosine of a value</td></tr>
 * <tr><td>TANH(<i>expression</i>)</td><td>Returns the hyperbolic tangens of a value</td></tr>
 * <tr><td>RAD(<i>expression</i>)</td><td>Converts an angle measured in degrees to an approximately equivalent angle measured in radians</td></tr>
 * <tr><td>DEG(<i>expression</i>)</td><td>Converts an angle measured in radians to an approximately equivalent angle measured in degrees</td></tr>
 * </table>
 * *Functions names are case insensitive.
 * <br>
 * <h2>Supported Constants</h2>
 * <table>
 * <tr><th>Constant</th><th>Description</th></tr>
 * <tr><td>PI</td><td>The value of <i>PI</i>, exact to 100 digits</td></tr>
 * <tr><td>TRUE</td><td>The value one</td></tr>
 * <tr><td>FALSE</td><td>The value zero</td></tr>
 * </table>
 * <p>
 * <h2>Add Custom Operators</h2>
 * <p>
 * Custom operators can be added easily, simply create an instance of `Expression.Operator` and add it to the expression.
 * Parameters are the operator string, its precedence and if it is left associative. The operators `evaluate()` method will be called with the BigDecimal values of the operands.
 * All existing operators can also be overridden.
 * <br>
 * For example, add an operator `x >> n`, that moves the decimal point of _x_ _n_ digits to the right:
 * <p>
 * <pre>
 * Expression e = new Expression("2.1234 >> 2");
 *
 * e.addOperator(e.new Operator(">>", 30, true) {
 *     {@literal @}Override
 *     public BigDecimal evaluate(BigDecimal v1, BigDecimal v2) {
 *         return v1.movePointRight(v2.toBigInteger().intValue());
 *     }
 * });
 *
 * e.evaluate(); // returns 212.34
 * </pre>
 * <br>
 * <h2>Add Custom Functions</h2>
 * <p>
 * Adding custom functions is as easy as adding custom operators. Create an instance of `Expression.Function`and add it to the expression.
 * Parameters are the function name and the count of required parameters. The functions `evaluate()` method will be called with a list of the BigDecimal parameters.
 * All existing functions can also be overridden.
 * <br>
 * For example, add a function `average(a,b,c)`, that will calculate the average value of a, b and c:
 * <br>
 * <pre>
 * Expression e = new Expression("2 * average(12,4,8)");
 *
 * e.addFunction(e.new Function("average", 3) {
 *     {@literal @}Override
 *     public BigDecimal evaluate(List<BigDecimal> parameters) {
 *         BigDecimal sum = parameters.get(0).add(parameters.get(1)).add(parameters.get(2));
 *         return sum.divide(new BigDecimal(3));
 *     }
 * });
 *
 * e.evaluate(); // returns 16
 * </pre>
 * The software is licensed under the MIT Open Source license (see LICENSE file).
 * <br>
 * <ul>
 * <li>The *power of* operator (^) implementation was copied from [Stack Overflow](http://stackoverflow.com/questions/3579779/how-to-do-a-fractional-power-on-bigdecimal-in-java) Thanks to Gene Marin</li>
 * <li>The SQRT() function implementation was taken from the book [The Java Programmers Guide To numerical Computing](http://www.amazon.de/Java-Number-Cruncher-Programmers-Numerical/dp/0130460419) (Ronald Mak, 2002)</li>
 * </ul>
 *
 * @author Udo Klimaschewski (http://about.me/udo.klimaschewski)
 */
public class Expression {
    private final char DECIMAL_SEPARATOR = '.';
    private final char MINUS_SIGN = '-';
    private MathContext mc = MathContext.DECIMAL32;
    private String expression = null;
    private List<String> rpn = null;
    private Map<String, Operator> operators = new HashMap<>();
    private Map<String, Function> functions = new HashMap<>();

    public Expression(String expression) {
        this.expression = expression;
        addOperator(new Operator("+", 20, true) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.add(v2, mc);
            }
        });
        addOperator(new Operator("-", 20, true) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.subtract(v2, mc);
            }
        });
        addOperator(new Operator("*", 30, true) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.multiply(v2, mc);
            }
        });
        addOperator(new Operator("/", 30, true) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.divide(v2, mc);
            }
        });
        addOperator(new Operator("%", 30, true) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.remainder(v2, mc);
            }
        });
        addOperator(new Operator("^", 40, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                int signOf2 = v2.signum();
                double dn1 = v1.doubleValue();
                v2 = v2.multiply(new BigDecimal(signOf2));
                BigDecimal remainderOf2 = v2.remainder(BigDecimal.ONE);
                BigDecimal n2IntPart = v2.subtract(remainderOf2);
                BigDecimal intPow = v1.pow(n2IntPart.intValueExact(), mc);
                BigDecimal doublePow = new BigDecimal(Math.pow(dn1, remainderOf2.doubleValue()));

                BigDecimal result = intPow.multiply(doublePow, mc);
                if (signOf2 == -1)
                    result = BigDecimal.ONE.divide(result, mc.getPrecision(), RoundingMode.HALF_UP);
                return result;
            }
        });
        addOperator(new Operator("&&", 4, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                boolean b1 = !v1.equals(BigDecimal.ZERO);
                boolean b2 = !v2.equals(BigDecimal.ZERO);
                return b1 && b2 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator("||", 2, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                boolean b1 = !v1.equals(BigDecimal.ZERO);
                boolean b2 = !v2.equals(BigDecimal.ZERO);
                return b1 || b2 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator(">", 10, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.compareTo(v2) == 1 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator(">=", 10, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.compareTo(v2) >= 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator("<", 10, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.compareTo(v2) == -1 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator("<=", 10, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.compareTo(v2) <= 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator("=", 7, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.compareTo(v2) == 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });
        addOperator(new Operator("==", 7, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return operators.get("=").eval(v1, v2);
            }
        });

        addOperator(new Operator("!=", 7, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.compareTo(v2) != 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });
        addOperator(new Operator("<>", 7, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return operators.get("!=").eval(v1, v2);
            }
        });

        addFunction(new Function("NOT", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                boolean zero = parameters.get(0).compareTo(BigDecimal.ZERO) == 0;
                return zero ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addFunction(new Function("IF", 3) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                boolean isTrue = !parameters.get(0).equals(BigDecimal.ZERO);
                return isTrue ? parameters.get(1) : parameters.get(2);
            }
        });

        addFunction(new Function("RANDOM", 0) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.random();
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("SIN", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.sin(Math.toRadians(parameters.get(0).doubleValue()));
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("COS", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.cos(Math.toRadians(parameters.get(0).doubleValue()));
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("TAN", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.tan(Math.toRadians(parameters.get(0).doubleValue()));
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("SINH", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.sinh(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("COSH", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.cosh(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("TANH", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.tanh(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("RAD", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.toRadians(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("DEG", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.toDegrees(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("MAX", 2) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                BigDecimal v1 = parameters.get(0);
                BigDecimal v2 = parameters.get(1);
                return v1.compareTo(v2) > 0 ? v1 : v2;
            }
        });
        addFunction(new Function("MIN", 2) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                BigDecimal v1 = parameters.get(0);
                BigDecimal v2 = parameters.get(1);
                return v1.compareTo(v2) < 0 ? v1 : v2;
            }
        });
        addFunction(new Function("ABS", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                return parameters.get(0).abs(mc);
            }
        });
        addFunction(new Function("LOG", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.log(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("LOG10", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.log10(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("ROUND", 2) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                BigDecimal toRound = parameters.get(0);
                int precision = parameters.get(1).intValue();
                return toRound.setScale(precision, mc.getRoundingMode());
            }
        });
        addFunction(new Function("FLOOR", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                BigDecimal toRound = parameters.get(0);
                return toRound.setScale(0, RoundingMode.FLOOR);
            }
        });
        addFunction(new Function("CEILING", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                BigDecimal toRound = parameters.get(0);
                return toRound.setScale(0, RoundingMode.CEILING);
            }
        });
        addFunction(new Function("SQRT", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                BigDecimal x = parameters.get(0);
                if (x.compareTo(BigDecimal.ZERO) == 0)
                    return new BigDecimal(0);
                if (x.signum() < 0)
                    throw new ExpressionException("Argument to SQRT() function must not be negative");
                BigInteger n = x.movePointRight(mc.getPrecision() << 1).toBigInteger();

                int bits = (n.bitLength() + 1) >> 1;
                BigInteger ix = n.shiftRight(bits);
                BigInteger ixPrev;

                do {
                    ixPrev = ix;
                    ix = ix.add(n.divide(ix)).shiftRight(1);
                    Thread.yield();
                } while (ix.compareTo(ixPrev) != 0);

                return new BigDecimal(ix, mc.getPrecision());
            }
        });
    }

    private boolean isNumber(String st) {
        if (st.charAt(0) == MINUS_SIGN && st.length() == 1)
            return false;
        for (char ch : st.toCharArray())
            if (!Character.isDigit(ch) && ch != MINUS_SIGN && ch != DECIMAL_SEPARATOR)
                return false;
        return true;
    }

    private List<String> shuntingYard(String expression) {
        List<String> outputQueue = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        Tokenizer tokenizer = new Tokenizer(expression);

        String lastFunction = null;
        String previousToken = null;
        while (tokenizer.hasNext()) {
            String token = tokenizer.next();
            if (isNumber(token))
                outputQueue.add(token);
            else if (functions.containsKey(token.toUpperCase())) {
                stack.push(token);
                lastFunction = token;
            } else if (Character.isLetter(token.charAt(0)))
                stack.push(token);
            else if (",".equals(token)) {
                while (!stack.isEmpty() && !"(".equals(stack.peek()))
                    outputQueue.add(stack.pop());
                if (stack.isEmpty())
                    throw new ExpressionException("Parse error for function '" + lastFunction + "'");
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
                    throw new ExpressionException("Missing operator at character position " + tokenizer.getPos());
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

    public BigDecimal evaluate() {
        Stack<BigDecimal> stack = new Stack<>();
        for (String token : getRPN()) {
            if (operators.containsKey(token)) {
                BigDecimal v1 = stack.pop();
                BigDecimal v2 = stack.pop();
                stack.push(operators.get(token).eval(v2, v1));
            } else if (functions.containsKey(token.toUpperCase())) {
                Function f = functions.get(token.toUpperCase());
                ArrayList<BigDecimal> p = new ArrayList<>(f.getTotalParameters());
                for (int i = 0; i < f.totalParameters; i++)
                    p.add(0, stack.pop());
                BigDecimal fResult = f.evaluate(p);
                stack.push(fResult);
            } else {
                stack.push(new BigDecimal(token, mc));
            }
        }
        return stack.pop().stripTrailingZeros();
    }

    public Operator addOperator(Operator operator) {
        return operators.put(operator.getOperation(), operator);
    }

    public Function addFunction(Function function) {
        return functions.put(function.getName(), function);
    }

    private List<String> getRPN() {
        if (rpn == null)
            rpn = shuntingYard(this.expression);
        return rpn;
    }

    public class ExpressionException extends RuntimeException {
        private static final long serialVersionUID = 1118142866870779047L;

        public ExpressionException(String message) {
            super(message);
        }
    }

    public abstract class Function {
        private String name;
        private int totalParameters;

        public Function(String name, int totalParameters) {
            this.name = name.toUpperCase();
            this.totalParameters = totalParameters;
        }

        public String getName() {
            return name;
        }

        public int getTotalParameters() {
            return totalParameters;
        }

        public abstract BigDecimal evaluate(List<BigDecimal> parameters);
    }

    public abstract class Operator {
        private String operation;
        private int precedence;
        private boolean leftAssociative;

        public Operator(String operation, int precedence, boolean leftAssociative) {
            this.operation = operation;
            this.precedence = precedence;
            this.leftAssociative = leftAssociative;
        }

        public String getOperation() {
            return operation;
        }

        public int getPrecedence() {
            return precedence;
        }

        public boolean isLeftAssociative() {
            return leftAssociative;
        }

        public abstract BigDecimal eval(BigDecimal v1, BigDecimal v2);
    }

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
                if (!operators.containsKey(token.toString()))
                    throw new ExpressionException("Unknown operator '" + token + "' at position " + (pos - token.length() + 1));
            }
            return previousToken = token.toString();
        }

        @Override
        public void remove() {
            throw new ExpressionException("remove() not supported");
        }

        public int getPos() {
            return pos;
        }
    }
}