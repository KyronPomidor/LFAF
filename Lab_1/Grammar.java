import java.util.*;

public class Grammar {
    public Set<String> VN; // Non-terminals
    public Set<String> VT; // Terminals
    public Map<String, ArrayList<String>> P; // Productions
    public String startSymbol;

    public Grammar(Set<String> VN, Set<String> VT, Map<String, ArrayList<String>> P, String startSymbol) {
        this.VN = VN;
        this.VT = VT;
        this.P = P;
        this.startSymbol = startSymbol;
    }

    // Generate one string from grammar
    public String generateString() {
        Random rand = new Random();
        String current = startSymbol;

        while (true) {
            StringBuilder next = new StringBuilder();
            boolean hasNonTerminal = false;

            for (char c : current.toCharArray()) {
                String s = String.valueOf(c);
                if (VN.contains(s)) {
                    hasNonTerminal = true;
                    ArrayList<String> options = P.get(s);
                    String chosen = options.get(rand.nextInt(options.size()));
                    next.append(chosen);
                } else {
                    next.append(s);
                }
            }

            current = next.toString();

            if (!hasNonTerminal)
                break;
        }

        return current;
    }

    public String classifyGrammar() {
        boolean isType3 = true;
        boolean isType2 = true;
        boolean isType1 = true;

        for (Map.Entry<String, ArrayList<String>> entry : P.entrySet()) {
            String lhs = entry.getKey();
            ArrayList<String> productions = entry.getValue();

            // Type 2 (Context-Free): LHS must be a single non-terminal
            if (!VN.contains(lhs) || lhs.length() != 1) {
                isType2 = false;
                isType3 = false;
            }

            for (String rhs : productions) {
                // Type 1 (Context-Sensitive): |RHS| >= |LHS| (except S → ε if S never appears
                // on RHS)
                if (rhs.length() < lhs.length()) {
                    if (!(rhs.isEmpty() && lhs.equals(startSymbol))) {
                        isType1 = false;
                    }
                }

                // Type 3 (Regular): must be of the form A → a or A → aB (right-linear)
                if (isType3) {
                    boolean rightLinear = rhs.matches("[a-z]") ||
                            (rhs.length() == 2 &&
                                    VT.contains(String.valueOf(rhs.charAt(0))) &&
                                    VN.contains(String.valueOf(rhs.charAt(1))));

                    boolean leftLinear = rhs.matches("[a-z]") ||
                            (rhs.length() == 2 &&
                                    VN.contains(String.valueOf(rhs.charAt(0))) &&
                                    VT.contains(String.valueOf(rhs.charAt(1))));
                    boolean epsilon = rhs.isEmpty() && lhs.equals(startSymbol);
                    if (!rightLinear && !epsilon && !leftLinear) {
                        isType3 = false;
                    }
                }
            }
        }

        if (isType3)
            return "Type 3 - Regular Grammar";
        if (isType2)
            return "Type 2 - Context-Free Grammar";
        if (isType1)
            return "Type 1 - Context-Sensitive Grammar";
        return "Type 0 - Unrestricted Grammar";
    }

}