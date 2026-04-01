import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    static final int STAR_MAX = 5;
    static final Random rand = new Random();

    // --- TOKEN ---
    static class Token {
        String type;       // "char" or "group"
        String ch;         // for char tokens
        String[] alts;     // for group tokens
        String quantifier; // "1", "*", "+", "?", or a number like "2"

        Token(String type, String ch, String[] alts, String quantifier) {
            this.type = type;
            this.ch = ch;
            this.alts = alts;
            this.quantifier = quantifier;
        }
    }

    // --- STEP 1: TOKENIZER ---
    static List<Token> tokenize(String pattern) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;

        while (i < pattern.length()) {
            if (pattern.charAt(i) == '(') {
                // find closing ')'
                int j = i + 1, depth = 1;
                while (j < pattern.length() && depth > 0) {
                    if (pattern.charAt(j) == '(') depth++;
                    if (pattern.charAt(j) == ')') depth--;
                    j++;
                }
                String inner = pattern.substring(i + 1, j - 1);
                String[] alts = inner.split("\\|");
                String quantifier = "1";

                if (j < pattern.length()) {
                    char q = pattern.charAt(j);
                    if (q == '*' || q == '+' || q == '?') {
                        quantifier = String.valueOf(q);
                        j++;
                    } else if (q == '^' && j + 1 < pattern.length()
                               && Character.isDigit(pattern.charAt(j + 1))) {
                        quantifier = String.valueOf(pattern.charAt(j + 1));
                        j += 2;
                    }
                }

                tokens.add(new Token("group", null, alts, quantifier));
                i = j;

            } else {
                char ch = pattern.charAt(i);
                String quantifier = "1";
                int j = i + 1;

                if (j < pattern.length()) {
                    char q = pattern.charAt(j);
                    if (q == '*' || q == '+' || q == '?') {
                        quantifier = String.valueOf(q);
                        j++;
                    } else if (q == '^' && j + 1 < pattern.length()
                               && Character.isDigit(pattern.charAt(j + 1))) {
                        quantifier = String.valueOf(pattern.charAt(j + 1));
                        j += 2;
                    }
                }

                tokens.add(new Token("char", String.valueOf(ch), null, quantifier));
                i = j;
            }
        }

        return tokens;
    }

    // --- STEP 2: RESOLVE QUANTIFIER TO COUNT ---
    static int pickCount(String q) {
        switch (q) {
            case "*": return rand.nextInt(STAR_MAX + 1);
            case "+": return rand.nextInt(STAR_MAX) + 1;
            case "?": return rand.nextInt(2);
            default:  return Integer.parseInt(q);
        }
    }

    // --- STEP 3: GENERATE STRING FROM TOKENS ---
    static String generate(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();

        for (Token t : tokens) {
            int count = pickCount(t.quantifier);

            if (t.type.equals("char")) {
                for (int k = 0; k < count; k++) sb.append(t.ch);
            } else {
                for (int k = 0; k < count; k++) {
                    sb.append(t.alts[rand.nextInt(t.alts.length)]);
                }
            }
        }

        return sb.length() == 0 ? "(empty string)" : sb.toString();
    }

    // --- BONUS: SHOW PROCESSING STEPS ---
    static void showSteps(List<Token> tokens) {
        System.out.println("  Processing steps:");
        int step = 1;

        for (Token t : tokens) {
            String tokenStr, action;

            if (t.type.equals("char")) {
                tokenStr = t.ch;
                action = switch (t.quantifier) {
                    case "1" -> "write '" + t.ch + "' exactly once";
                    case "*" -> "write '" + t.ch + "' 0-" + STAR_MAX + " times (*)";
                    case "+" -> "write '" + t.ch + "' 1-" + STAR_MAX + " times (+)";
                    case "?" -> "write '" + t.ch + "' 0 or 1 times (?)";
                    default  -> "write '" + t.ch + "' exactly " + t.quantifier + " times";
                };
            } else {
                String opts = String.join(" | ", t.alts);
                tokenStr = "(" + String.join("|", t.alts) + ")";
                action = switch (t.quantifier) {
                    case "1" -> "pick one of (" + opts + ")";
                    case "*" -> "pick from (" + opts + ") 0-" + STAR_MAX + " times (*)";
                    case "+" -> "pick from (" + opts + ") 1-" + STAR_MAX + " times (+)";
                    case "?" -> "pick from (" + opts + ") 0 or 1 times (?)";
                    default  -> "pick from (" + opts + ") exactly " + t.quantifier + " times";
                };
            }

            System.out.printf("    Step %d: %-20s -> %s%n", step++, tokenStr, action);
        }
    }

    // --- MAIN ---
    public static void main(String[] args) {
        String[] patterns = {
            "O(P|Q|R)+2(3|4)",
            "A*B(C|D|E)F(G|H|I)^2",
            "J+K(L|M|N)*O?(P|Q)^3"
        };

        System.out.println("=== Regex String Generator - Variant 3 ===\n");

        for (int i = 0; i < patterns.length; i++) {
            String pattern = patterns[i];
            System.out.println("Expression " + (i + 1) + ": " + pattern);

            List<Token> tokens = tokenize(pattern);

            // show steps (bonus)
            showSteps(tokens);

            // generate 3 sample strings
            System.out.print("  Generated: ");
            for (int j = 0; j < 3; j++) {
                System.out.print("[" + generate(tokens) + "]  ");
            }
            System.out.println("\n");
        }
    }
}