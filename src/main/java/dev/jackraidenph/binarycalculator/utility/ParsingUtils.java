package dev.jackraidenph.binarycalculator.utility;

import dev.jackraidenph.binarycalculator.binarycontainer.BinaryContainer;
import dev.jackraidenph.binarycalculator.binarycontainer.BinaryFloat;
import dev.jackraidenph.binarycalculator.binarycontainer.BinaryInteger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsingUtils {
    public static final Map<String, Integer> PRECEDENCE = new HashMap<>() {{
        put("*", 3);
        put("/", 3);
        put("+", 2);
        put("-", 2);
        put("(", -1);
        put(")", -1);
    }};

    public static final String OPERATORS = String.join("", PRECEDENCE.keySet());

    public static final String RIGHT_ASSOC = "^";

    public static final Pattern TOKEN_PATTERN = Pattern.compile("(-?\\d+(\\.\\d+)?)|[/*+\\-()]");

    public static BinaryFloat bFloatFromStr(String str) {
        return new BinaryFloat(Float.parseFloat(str));
    }

    public static BinaryInteger bIntFromStr(String str) {
        return new BinaryInteger(Integer.parseInt(str));
    }

    public static <T extends Number, N extends BinaryContainer<T, N>> N operate(String left, String right, BiFunction<N, N, N> operation) {
        boolean isFloatingPointOp = left.contains(".") || right.contains(".");
        N lb = isFloatingPointOp ? (N) bFloatFromStr(left) : (N) bIntFromStr(left);
        N rb = isFloatingPointOp ? (N) bFloatFromStr(right) : (N) bIntFromStr(right);

        return operation.apply(lb, rb);
    }

    public static final HashMap<String, BiFunction<String, String, ?>> OPERATIONS_MAP = new HashMap<>() {{
        put("+", (left, right) -> operate(left, right, BinaryContainer::add));
        put("-", (left, right) -> operate(left, right, BinaryContainer::subtract));
        put("*", (left, right) -> operate(left, right, BinaryContainer::multiply));
        put("/", (left, right) -> operate(left, right, BinaryContainer::divideBy));
    }};

    public static Queue<String> infixToPostfix(String toConvert) {
        Stack<String> stack = new Stack<>();
        Matcher tokenMatcher = TOKEN_PATTERN.matcher(toConvert);
        Queue<String> output = new LinkedList<>();

        while (!toConvert.isEmpty()) {
            if (tokenMatcher.find()) {
                String token = tokenMatcher.group();
                if ((token.length() == 1) && OPERATORS.contains(token)) {
                    if (stack.empty()) {
                        stack.push(token);
                    }
                    else if (token.equals(")")) {
                        while (!stack.peek().equals("(")) {
                            output.add(stack.pop());
                        }
                        stack.pop();
                    } else {
                        while (!token.equals("(") &&
                                ((PRECEDENCE.get(stack.peek()) > PRECEDENCE.get(token)) ||
                                        (Objects.equals(PRECEDENCE.get(stack.peek()), PRECEDENCE.get(token)) && !RIGHT_ASSOC.contains(token)))
                        ) {
                            output.add(stack.pop());
                        }
                        stack.push(token);
                    }
                } else { output.add(token); }
                toConvert = toConvert.replaceFirst(Pattern.quote(token), "");
            } else {
                break;
            }
        }

        while (!stack.empty()) {
            output.add(stack.pop());
        }

        return output;
    }//3 + 4 * 2 / ( 1 - (5) ) * 2 * 3

    public static String parsePostfix(Queue<String> toParse) {
        Stack<String> stack = new Stack<>();
        while (!toParse.isEmpty()) {
            String token = toParse.poll();
            if (!((token.length() == 1) && OPERATORS.contains(token))) {
                stack.push(token);
            } else {
                String right = stack.pop();
                String left = stack.pop();

                Object result = OPERATIONS_MAP.get(token).apply(left, right);
                if(result instanceof BinaryInteger intResult)
                    result = intResult.getDecimal();
                else if(result instanceof BinaryFloat floatResult)
                    result = floatResult.getDecimal();
                else throw new RuntimeException("Type conversion error!");

                stack.push(result.toString());
            }
        }

        return stack.isEmpty() ? "" : stack.pop();
    }

    public static String floatFivePrecisionFormat(Float val) {
        return String.format(Locale.US, "%.5f", val);
    }
}
