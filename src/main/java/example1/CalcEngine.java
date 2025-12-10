package example1;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CalcEngine {

    // 記号・演算子の変換
    public static String convertOperator(String expr) {
        expr = expr.replace("×", "*").replace("÷", "/");
        expr = expr.replaceAll("(\\d)\\(", "$1*(");
        expr = expr.replaceAll("\\)(\\d)", ")*$1");
        expr = expr.replaceAll("^-", "0-");
        return expr;
    }

    // 関数名の変換
    public static String convertFunction(String expr) {
        expr = expr.replace("|x|", "abc(0)");
        expr = expr.replaceAll("(\\d+(?:\\.\\d+)?)(abc)", "abc($1)");
        expr = expr.replaceAll("abc\\((\\d+(?:\\.\\d+)?)\\)", "$1 abc");

        expr = expr.replace("¹/x", "inv");
        expr = expr.replace("x²", "square");
        expr = expr.replace("ln", "ln");
        expr = expr.replace("log", "log");
        expr = expr.replace("exp", "exp");
        expr = expr.replace("+/-", "neg");
        expr = expr.replace("n!", "fact");
        expr = expr.replace("xʸ", "pow");
        expr = expr.replace("²√x", "root");
        expr = expr.replace("mod", "mod");
        expr = expr.replace("10ˣ", "tenpow");

        expr = expr.replace("^(\u221ax)", "sqrt(0)");
        expr = expr.replaceAll("^(x²)", "square(0)");
        expr = expr.replaceAll("(\\d+(?:\\.\\d+)?)(tenpow)(\\d+(?:\\.\\d+)?)", "$1 $3 tenpow *");
        expr = expr.replaceAll("^(\\|x\\||lxl)", "abs(0)");
        expr = expr.replaceAll("(\\d+(?:\\.\\d+)?)(inv)", "$2($1)");
        expr = expr.replaceAll("(\\d+(?:\\.\\d+)?)(root)", "$2($1)");
        expr = expr.replaceAll("(\\d+(?:\\.\\d+)?)(abc)", "$2($1)");
        expr = expr.replaceAll("(\\d+(?:\\.\\d+)?)(pow)(\\d+(?:\\.\\d+)?)", "pow($1,$3)");
        expr = expr.replaceAll("pow\\((\\d+(?:\\.\\d+)?),(\\d+(?:\\.\\d+)?)\\)", "$1 $2 pow");
        expr = expr.replaceAll("inv\\((\\d+(?:\\.\\d+)?)\\)", "$1 inv");
        expr = expr.replaceAll("(\\d+(?:\\.\\d+)?)(square|sqrt|abc|exp|ln|log|tenpow|neg|fact)", "$2($1)");
        return expr;
    }

    // トークン分割
    public static String[] tokenize(String expr) {
        return expr.split("(?<=[-+*/()])|(?=[-+*/()])|(?<=mod)|(?=mod)|(?<=[a-zA-Z])(?=\\d)|\\s+");
    }

    // 単項マイナスの前処理
    public static List<String> preprocessUnaryMinus(String[] tokens) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < tokens.length; ++i) {
            String t = tokens[i];
            if (t.equals("-")) {
                if (i == 0 || isOperator(tokens[i - 1]) || tokens[i - 1].equals("(")) {
                    result.add("neg");
                } else {
                    result.add("-");
                }
            } else {
                result.add(t);
            }
        }
        return result;
    }

    // 中置 → 後置（逆ポーランド記法変換）
    public static List<String> toRPN(String[] tokens) {
        List<String> output = new ArrayList<>();
        Stack<String> ops = new Stack<>();

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            if (token.matches("\\d+(\\.\\d+)?")) {
                output.add(token);
                while (!ops.isEmpty() && isFunction(ops.peek())) {
                    output.add(ops.pop());
                }
                continue;
            }

            if (isOperator(token)) {
                while (!(ops.isEmpty() || ops.peek().equals("(") ||
                        precedence(ops.peek()) <= precedence(token) &&
                        (precedence(ops.peek()) != precedence(token) || isRightAssociative(token)))) {
                    output.add(ops.pop());
                }
                ops.push(token);
                continue;
            }

            if (token.equals("(")) {
                ops.push(token);
                continue;
            }

            if (token.equals(")")) {
                while (!ops.isEmpty() && !ops.peek().equals("(")) {
                    output.add(ops.pop());
                }
                if (!ops.isEmpty() && ops.peek().equals("(")) {
                    ops.pop();
                }
                continue;
            }

            if (isFunction(token)) {
                while (!ops.isEmpty() && !isFunction(ops.peek()) &&
                        precedence(ops.peek()) >= precedence(token)) {
                    output.add(ops.pop());
                }
                ops.push(token);
            }
        }

        while (!ops.isEmpty()) {
            output.add(ops.pop());
        }
        return output;
    }

    // 評価
    public static double evaluate(String expr) {
        expr = convertOperator(expr);
        expr = convertFunction(expr);

        String[] tokens = tokenize(expr);
        List<String> processed = preprocessUnaryMinus(tokens);
        List<String> output = toRPN(processed.toArray(new String[0]));

        Stack<Double> stack = new Stack<>();

        for (String token : output) {
            if (token.matches("\\d+(\\.\\d+)?")) {
                stack.push(Double.parseDouble(token));
                continue;
            }

            switch (token) {
                case "+" -> {
                    double b = stack.pop();
                    double a = stack.isEmpty() ? 0 : stack.pop();
                    stack.push(a + b);
                }
                case "-" -> {
                    double b = stack.pop();
                    double a = stack.isEmpty() ? 0 : stack.pop();
                    stack.push(a - b);
                }
                case "*" -> stack.push(stack.pop() * stack.pop());
                case "/" -> {
                    double b = stack.pop();
                    double a = stack.pop();
                    if (b == 0.0) throw new ArithmeticException("0で割ることは出来ません");
                    stack.push(a / b);
                }
                case "mod" -> {
                    double b = stack.pop();
                    double a = stack.pop();
                    stack.push(a % b);
                }
                case "pow" -> {
                    double b = stack.pop();
                    double a = stack.pop();
                    stack.push(Math.pow(a, b));
                }
                case "root" -> {
                    if (stack.size() == 1) {
                        stack.push(Math.sqrt(stack.pop()));
                    } else {
                        double b = stack.pop();
                        double a = stack.pop();
                        stack.push(Math.pow(a, 1.0 / b));
                    }
                }
                case "inv" -> {
                    double v = stack.pop();
                    if (v == 0.0) {
                        throw new ArithmeticException("0で割ることは出来ません");
                    }
                    stack.push(1.0 / v);
                }
                case "square" -> {
                    double a = stack.pop();
                    stack.push(a * a);
                }
                case "abc" -> stack.push(Math.abs(stack.pop()));
                case "exp" -> stack.push(Math.exp(stack.pop()));
                case "ln" -> stack.push(Math.log(stack.pop()));
                case "log" -> stack.push(Math.log10(stack.pop()));
                case "tenpow" -> stack.push(Math.pow(10.0, stack.pop()));
                case "neg" -> stack.push(-stack.pop());
                case "fact" -> stack.push(factorial(stack.pop().intValue()));
                default -> {
                    // 予期しないトークン
                    System.err.println("未知のトークン: " + token);
                }
            }
        }

        return stack.isEmpty() ? 0.0 : stack.pop();
    }

    // 補助メソッド群
    private static int precedence(String op) {
        return switch (op) {
            case "+", "-" -> 1;
            case "*", "/", "mod" -> 2;
            case "pow", "root" -> 3;
            default -> isFunction(op) ? 4 : -1;
        };
    }

    private static boolean isRightAssociative(String token) {
        return token.equals("pow") || token.equals("root");
    }

    private static boolean isOperator(String token) {
        return switch (token) {
            case "+", "-", "*", "/", "mod", "pow", "root" -> true;
            default -> false;
        };
    }

    private static boolean isFunction(String token) {
        return switch (token) {
            case "inv", "square", "abc", "exp", "ln", "log", "tenpow", "neg", "fact" -> true;
            default -> false;
        };
    }

    private static double factorial(int n) {
        if (n < 0) throw new ArithmeticException("階乗は負の数に定義されていません");
        double result = 1.0;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }
}
