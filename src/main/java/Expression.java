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

        addFunction(new Function("RANDOM", 0) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.random();
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("sin", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.sin(Math.toRadians(parameters.get(0).doubleValue()));
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("cos", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.cos(Math.toRadians(parameters.get(0).doubleValue()));
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("tan", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.tan(Math.toRadians(parameters.get(0).doubleValue()));
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("sinh", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.sinh(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("cosh", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.cosh(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("tanh", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.tanh(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("rad", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.toRadians(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("deg", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.toDegrees(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
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
                return parameters.get(0).abs(mc);
            }
        });
        addFunction(new Function("log", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.log(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("log10", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                double d = Math.log10(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("round", 2) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                BigDecimal toRound = parameters.get(0);
                int precision = parameters.get(1).intValue();
                return toRound.setScale(precision, mc.getRoundingMode());
            }
        });
        addFunction(new Function("floor", 1) {
            @Override
            public BigDecimal evaluate(List<BigDecimal> parameters) {
                BigDecimal toRound = parameters.get(0);
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
            else if (functions.containsKey(token.toLowerCase())) {
                stack.push(token.toLowerCase());
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