/**
 * Created by Meir on 11/2/2015.
 */
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;

public class Calculator {

    public static boolean isOperand(String s) {
        if (s.length() > 1 && s.substring(0,1).equals("-")) {
            return isPositiveOperand(s.substring(1,s.length()));
        } else {
            return isPositiveOperand(s);
        }
    }

    public static boolean isPositiveOperand(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!"1234567890".contains(s.substring(i,i+1))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isOperator(String s) {
        return "+-*/%".contains(s);
    }

    public static int priority(String operator) {
        assert isOperator(operator);
        if ("+-".contains(operator)) {
            return 0;
        } else { // operator is one of *, /, or %
            return 1;
        }
    }

    public static boolean firstAndLastAreParensAndMatch(ArrayList<String> infix) {
        if ((!infix.get(0).equals("(")) || (!infix.get(infix.size()-1).equals(")"))) {
            return false;
        }
        int count = 1;
        int pointer = infix.size() - 2;
        while (pointer > 0) {
            if (infix.get(pointer).equals(")")) {
                count++;
            } else if (infix.get(pointer).equals("(")) {
                count--;
            }
            if (count == 0) {
                return false;
            }
            pointer--;
        }
        return true;
    }

    public static ArrayList<String> subArrayList(ArrayList<String> infix, int startIndex, int endIndex) {
        // start is inclusive, end is exclusive
        ArrayList<String> ret = new ArrayList<String>();
        for (int i = startIndex; i < endIndex; i++) {
            ret.add(infix.get(i));
        }
        return ret;
    }

    public static int indexOfLastOperator(ArrayList<String> infix) {
        // assumes there are no extra enclosing parens; i.e. no ((4)) or (3)
        if (infix.size() == 1) {
            assert false; // should never happen
            return 42; // can never happen
        }
        int parenCount = 0;
        int goalDepth = 0;
        int goalPriority = 0;
        int maxPriority = 1;
        while (true) {
            for (int i = infix.size() - 1; i >= 0; i--) {
                if (infix.get(i).equals(")")) {
                    parenCount++;
                } else if (infix.get(i).equals("(")) {
                    parenCount--;
                } else if (isOperator(infix.get(i))) {
                    if ((parenCount == goalDepth) && (priority(infix.get(i)) == goalPriority)) {
                        return i;
                    }
                }
            }
            // have not found it, must update our search requirements
            if (goalPriority < maxPriority) {
                goalPriority++;
            } else { // cannot raise priority
                goalDepth++;
                goalPriority = 0;
            }
        }
        // will always terminate because if there is an operator in `infix`
        // because every combination of depth and priority is reached eventually
        // at the end of each loop, `parenCount` is naturally reset to 0
    }

    public static int recursivelyEvaluateInfix(ArrayList<String> infix) {
        if (infix.size() == 1) {
            return Integer.parseInt(infix.get(0));
        }
        if (firstAndLastAreParensAndMatch(infix)) {
            return recursivelyEvaluateInfix(subArrayList(infix, 1, infix.size()-1));
        }
        int mid = indexOfLastOperator(infix);
        String lastOperator = infix.get(mid);
        ArrayList<String> firstPart = subArrayList(infix, 0, mid);
        ArrayList<String> secondPart = subArrayList(infix, mid+1, infix.size());
        if (lastOperator.equals("+")) {
            return recursivelyEvaluateInfix(firstPart) + recursivelyEvaluateInfix(secondPart);
        } else if (lastOperator.equals("-")) {
            return recursivelyEvaluateInfix(firstPart) - recursivelyEvaluateInfix(secondPart);
        } else if (lastOperator.equals("*")) {
            return recursivelyEvaluateInfix(firstPart) * recursivelyEvaluateInfix(secondPart);
        } else if (lastOperator.equals("/")) {
            return recursivelyEvaluateInfix(firstPart) / recursivelyEvaluateInfix(secondPart);
        } else if (lastOperator.equals("%")) {
            return recursivelyEvaluateInfix(firstPart) % recursivelyEvaluateInfix(secondPart);
        } else {
            assert false; // should never happen
            return 42; // can never happen
        }
    }

    public static ArrayList<String> inputStringToInfixArrayList(String input) {
        if (input.contains(" ")) {
            return inputStringToInfixArrayList(input.replaceAll("\\s",""));
        }
        ArrayList<String> A = new ArrayList<String>();
        int pointer = 0;
        String s = "";
        while (pointer < input.length()) {
            String c = input.substring(pointer, pointer + 1);
            if ("+-*/%()".contains(c)) {
                if (s.length() > 0) {
                    A.add(s);
                    s = "";
                }
                if (c.equals("-") && (A.size() == 0 || "+-*/%(".contains(A.get(A.size()-1)))) {
                    s += c;
                } else {
                    A.add(c);
                }
            } else if ("1234567890".contains(c)) {
                s += c;
            }
            pointer++;
        }
        if (s.length() > 0) {
            A.add(s);
        }
        return A;
    }

    public static ArrayList<String> infixToPostfix(ArrayList<String> infix) {
        // `infix` must be contain only elements in "0123456789+-*/%()"
        // will return a string containing only elements in "0123456789+-*/%"
        // associates from left to right with operands that have equal priority (e.g. 7 - 1 + 3 = 9, not 3)
        Stack<String> stack = new Stack<String>(); // will contain operators
        ArrayList<String> postfix = new ArrayList<String>();
        int i = 0;

        while (i < infix.size()) {
            String c = infix.get(i);
            if (isOperand(c)) {
                postfix.add(c);
            } else if (stack.isEmpty() || ((stack.peek()).equals("(") && (!c.equals(")")))) {
                stack.push(c);
            } else if (c.equals("(")) {
                stack.push(c);
            } else if (c.equals(")")) {
                while (!(stack.peek()).equals("(")) {
                    postfix.add(stack.pop());
                }
                stack.pop();
            } else if (priority(c) > priority(stack.peek())) {
                stack.push(c);
            } else if (priority(c) ==  priority(stack.peek())) {
                postfix.add(stack.pop());
                stack.push(c);
            } else if (priority(c) < priority(stack.peek())) {
                postfix.add(stack.pop());
                i--;
            }
            i++;
        }
        while (!stack.isEmpty()) {
            postfix.add(stack.pop());
        }
        return postfix;
    }

    public static int evaluatePostfix(ArrayList<String> postfix) {
        Stack<String> stack = new Stack<String>();
        for (String c : postfix) {
            if (isOperand(c)) {
                stack.push(c);
            } else {
                String y = stack.pop();
                String x = stack.pop();
                if (c.equals("+")) {
                    stack.push(Integer.toString(Integer.parseInt(x) + (Integer.parseInt(y))));
                } else if (c.equals("-")) {
                    stack.push(Integer.toString(Integer.parseInt(x) - (Integer.parseInt(y))));
                } else if (c.equals("*")) {
                    stack.push(Integer.toString(Integer.parseInt(x) * (Integer.parseInt(y))));
                } else if (c.equals("/")) {
                    stack.push(Integer.toString(Integer.parseInt(x) / (Integer.parseInt(y))));
                } else if (c.equals("%")) {
                    stack.push(Integer.toString(Integer.parseInt(x) % (Integer.parseInt(y))));
                }
            }
        }
        return Integer.parseInt(stack.pop());
    }

    public static String generateExpression() {
        int bound = 100; // bound for the numbers we'll use
        Random random = new Random();
        String ret = random.nextInt(bound) + "";
        int size = random.nextInt(25); // approximate size of expression to be generated
        for (int i = 0; i < size; i++) {
            //maybe put the whole thing in parentheses
            if (random.nextInt(3) == 1){
                ret = "(" + ret + ")";
            } else {
                //maybe negate it (but don't put a `-` before a paren or before a `-`)
                if (random.nextInt(3) == 1 && !ret.substring(0,1).equals("(") && !ret.substring(0,1).equals("-")){
                    ret = "-" + ret;
                }
            }
            //choose an operator
            String[] ops = new String[]{"+","-","*","/","%"};
            String op = ops[random.nextInt(5)];
            int toAppend = random.nextInt(bound);
            //choose whether to append before or after
            if (random.nextInt(2) == 1) {
                if (!(op.equals("-") && ret.substring(0,1).equals("-"))) {
                    ret = toAppend + op + ret;
                }
            } else {
                ret = ret + op + toAppend;
            }
        }
        return ret;
    }

    public static void main(String[] args) {
        // first read the file
        Scanner sc = null;
        String fileName = "";
        if (args.length > 0) {
            fileName = args[0];
        } else {
            fileName = "calc_expressions.txt";
        }
        try {
            FileReader fr = new FileReader(fileName);
            sc = new Scanner(fr);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // dummy variable to get rid of unnecessary first line
        int N = Integer.parseInt(sc.nextLine());
        while (sc.hasNext()) {
            String expression = sc.nextLine();
            expression = expression.replaceAll("\\s","");
            try {
                if (!expression.equals("") && !expression.substring(0, 1).equals("#")) { // short circuiting ftw
                    ArrayList<String> infix = inputStringToInfixArrayList(expression);
                    ArrayList<String> postfix = infixToPostfix(infix);
                    int stackAnswer = evaluatePostfix(postfix);
                    System.out.println("With stack:      " + expression + " = " + stackAnswer);
                    int recursiveAnswer = recursivelyEvaluateInfix(infix);
                    System.out.println("With recursion:  " + expression + " = " + recursiveAnswer);
                    System.out.println();
                }
            } catch (ArithmeticException e) {
                System.out.println("Error: \"" + expression + "\" involves a division or modulo by zero.");
                System.out.println();
            }
        }
        //uncomment the next three lines if you want the program to generate a bunch of new expressions
//        for (int i = 0; i < 10; i++) {
//            System.out.println(generateExpression());
//        }
    }
}
