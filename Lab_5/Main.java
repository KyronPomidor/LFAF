import java.util.*;

public class Main {

    // Build Variant 3 grammar
    // G=(VN, VT, P, S)  VN={S,A,B,C,E}  VT={a,d}
    // 1.S→dB  2.S→A  3.A→d  4.A→dS  5.A→aAdAB
    // 6.B→aC  7.B→aS  8.B→AC  9.C→ε  10.E→AS
    static Grammar buildVariant3() {
        Set<String> vn = new LinkedHashSet<>(List.of("S","A","B","C","E"));
        Set<String> vt = new LinkedHashSet<>(List.of("a","d"));

        Map<String, List<String>> p = new LinkedHashMap<>();
        p.put("S", new ArrayList<>(List.of("dB","A")));
        p.put("A", new ArrayList<>(List.of("d","dS","aAdAB")));
        p.put("B", new ArrayList<>(List.of("aC","aS","AC")));
        p.put("C", new ArrayList<>(List.of("ε")));
        p.put("E", new ArrayList<>(List.of("AS")));

        return new Grammar(vn, vt, p, "S");
    }

    //  BONUS: accept any grammar 
    static Grammar buildCustomGrammar() {
        // Example from the lab PDF: VN={S,A,B,C,D} VT={a,b}
        Set<String> vn = new LinkedHashSet<>(List.of("S","A","B","C","D"));
        Set<String> vt = new LinkedHashSet<>(List.of("a","b"));

        Map<String, List<String>> p = new LinkedHashMap<>();
        p.put("S", new ArrayList<>(List.of("AC","bA","B","aA")));
        p.put("A", new ArrayList<>(List.of("ε","aS","ABAb")));
        p.put("B", new ArrayList<>(List.of("a","AbSA")));
        p.put("C", new ArrayList<>(List.of("abC")));
        p.put("D", new ArrayList<>(List.of("AB")));

        return new Grammar(vn, vt, p, "S");
    }

    static void runCNF(Grammar g, String label) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("GRAMMAR: " + label);
        System.out.println("=".repeat(60));
        System.out.println("\nInitial grammar:");
        g.printGrammar();

        g.eliminateEpsilonProductions();
        g.eliminateRenamings();
        g.eliminateInaccessibleSymbols();
        g.eliminateNonProductiveSymbols();
        g.convertToCNF();

        System.out.println("\n--- Final CNF Grammar ---");
        g.printGrammar();
    }

    public static void main(String[] args) {
        runCNF(buildVariant3(),   "Variant 3 (student grammar)");
        runCNF(buildCustomGrammar(), "Lab Example (bonus: custom grammar)");
    }
}