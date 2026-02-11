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

            if (!hasNonTerminal) break;
        }

        return current;
    }

}