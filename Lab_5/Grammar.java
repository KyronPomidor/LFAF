import java.util.*;

public class Grammar {
    private Set<String> nonTerminals;
    private Set<String> terminals;
    private Map<String, List<String>> productions;
    private String startSymbol;

    public Grammar(Set<String> nonTerminals, Set<String> terminals,
                   Map<String, List<String>> productions, String startSymbol) {
        this.nonTerminals = new LinkedHashSet<>(nonTerminals);
        this.terminals = new LinkedHashSet<>(terminals);
        this.productions = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> e : productions.entrySet())
            this.productions.put(e.getKey(), new ArrayList<>(e.getValue()));
        this.startSymbol = startSymbol;
    }

    //  Step 1: Eliminate ε-productions 
    public void eliminateEpsilonProductions() {
        System.out.println("\n=== Step 1: Eliminate ε-productions ===");

        // Find nullable symbols
        Set<String> nullable = new LinkedHashSet<>();
        for (Map.Entry<String, List<String>> e : productions.entrySet())
            if (e.getValue().contains("ε"))
                nullable.add(e.getKey());

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Map.Entry<String, List<String>> e : productions.entrySet()) {
                for (String rhs : e.getValue()) {
                    String[] syms = splitRHS(rhs);
                    boolean allNullable = true;
                    for (String s : syms)
                        if (!nullable.contains(s)) { allNullable = false; break; }
                    if (allNullable && !rhs.equals("ε") && nullable.add(e.getKey()))
                        changed = true;
                }
            }
        }
        System.out.println("Nullable symbols: " + nullable);

        // For each production, add versions with nullable symbols omitted
        Map<String, List<String>> newProds = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> e : productions.entrySet()) {
            Set<String> rhsSet = new LinkedHashSet<>();
            for (String rhs : e.getValue()) {
                if (!rhs.equals("ε"))
                    rhsSet.add(rhs);
                // Generate all subsets of nullable positions
                String[] syms = splitRHS(rhs);
                List<Integer> nullablePos = new ArrayList<>();
                for (int i = 0; i < syms.length; i++)
                    if (nullable.contains(syms[i])) nullablePos.add(i);
                int subsets = 1 << nullablePos.size();
                for (int mask = 1; mask < subsets; mask++) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < syms.length; i++) {
                        int idx = nullablePos.indexOf(i);
                        boolean omit = (idx >= 0) && ((mask >> idx & 1) == 1);
                        if (!omit) sb.append(syms[i]);
                    }
                    if (sb.length() > 0) rhsSet.add(sb.toString());
                }
            }
            newProds.put(e.getKey(), new ArrayList<>(rhsSet));
        }
        productions = newProds;
        printProductions();
    }

    //  Step 2: Eliminate renamings (unit productions A → B) 
    public void eliminateRenamings() {
        System.out.println("\n=== Step 2: Eliminate renamings ===");
        boolean changed = true;
        while (changed) {
            changed = false;
            Map<String, List<String>> newProds = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> e : productions.entrySet()) {
                Set<String> newRHS = new LinkedHashSet<>(e.getValue());
                for (String rhs : e.getValue()) {
                    if (isNonTerminal(rhs) && productions.containsKey(rhs)) {
                        // Replace A → B with all of B's productions
                        for (String sub : productions.get(rhs)) {
                            if (!sub.equals(rhs)) newRHS.add(sub);
                        }
                        newRHS.remove(rhs);
                        changed = true;
                    }
                }
                newProds.put(e.getKey(), new ArrayList<>(newRHS));
            }
            productions = newProds;
        }
        printProductions();
    }

    //  Step 3: Eliminate inaccessible symbols 
    public void eliminateInaccessibleSymbols() {
        System.out.println("\n=== Step 3: Eliminate inaccessible symbols ===");
        Set<String> accessible = new LinkedHashSet<>();
        accessible.add(startSymbol);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String nt : new ArrayList<>(accessible)) {
                if (!productions.containsKey(nt)) continue;
                for (String rhs : productions.get(nt)) {
                    for (String sym : splitRHS(rhs))
                        if (changed |= accessible.add(sym));
                }
            }
        }
        System.out.println("Accessible: " + accessible);
        Set<String> inaccessible = new LinkedHashSet<>(nonTerminals);
        inaccessible.removeAll(accessible);
        System.out.println("Inaccessible (removed): " + inaccessible);

        nonTerminals.retainAll(accessible);
        terminals.retainAll(accessible);
        productions.keySet().retainAll(nonTerminals);
        printProductions();
    }

    //  Step 4: Eliminate non-productive symbols 
    public void eliminateNonProductiveSymbols() {
        System.out.println("\n=== Step 4: Eliminate non-productive symbols ===");
        // Productive = can derive a string of terminals
        Set<String> productive = new LinkedHashSet<>(terminals);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Map.Entry<String, List<String>> e : productions.entrySet()) {
                for (String rhs : e.getValue()) {
                    boolean allProd = true;
                    for (String s : splitRHS(rhs))
                        if (!productive.contains(s)) { allProd = false; break; }
                    if (allProd) changed |= productive.add(e.getKey());
                }
            }
        }
        Set<String> nonProductive = new LinkedHashSet<>(nonTerminals);
        nonProductive.removeAll(productive);
        System.out.println("Non-productive (removed): " + nonProductive);

        nonTerminals.removeAll(nonProductive);
        productions.keySet().retainAll(nonTerminals);
        // Remove productions referencing non-productive symbols
        for (Map.Entry<String, List<String>> e : productions.entrySet()) {
            e.getValue().removeIf(rhs -> {
                for (String s : splitRHS(rhs))
                    if (nonProductive.contains(s)) return true;
                return false;
            });
        }
        productions.entrySet().removeIf(e -> e.getValue().isEmpty());
        printProductions();
    }

    //  Step 5: Convert to CNF 
    public void convertToCNF() {
        System.out.println("\n=== Step 5: Chomsky Normal Form ===");
        Map<String, String> terminalMap = new LinkedHashMap<>(); // terminal → new NT
        Map<String, String> newNTMap = new LinkedHashMap<>();    // production → new NT
        int[] counter = {1};

        Map<String, List<String>> cnfProds = new LinkedHashMap<>();
        for (String nt : productions.keySet()) cnfProds.put(nt, new ArrayList<>());

        for (Map.Entry<String, List<String>> e : productions.entrySet()) {
            for (String rhs : e.getValue()) {
                String[] syms = splitRHS(rhs);
                if (syms.length == 1) {
                    // Already A → a or A → B, keep as is
                    cnfProds.get(e.getKey()).add(rhs);
                    continue;
                }
                // Replace terminals in long productions with new NTs
                for (int i = 0; i < syms.length; i++) {
                    if (terminals.contains(syms[i])) {
                        String t = syms[i];
                        if (!terminalMap.containsKey(t)) {
                            String newNT = "X" + counter[0]++;
                            terminalMap.put(t, newNT);
                            nonTerminals.add(newNT);
                            cnfProds.put(newNT, new ArrayList<>(List.of(t)));
                        }
                        syms[i] = terminalMap.get(t);
                    }
                }
                // Binarize: A → B1 B2 B3 ... becomes A → B1 Y1, Y1 → B2 B3 ...
                String result = binarize(syms, cnfProds, counter, newNTMap);
                cnfProds.get(e.getKey()).add(result);
            }
        }
        productions = cnfProds;
        printProductions();
    }

    private String binarize(String[] syms, Map<String, List<String>> cnfProds,
                            int[] counter, Map<String, String> newNTMap) {
        if (syms.length == 2) return syms[0] + syms[1];
        // Combine last two first, then work left
        String key = String.join("", syms);
        if (newNTMap.containsKey(key)) return syms[0] + newNTMap.get(key);
        String[] rest = Arrays.copyOfRange(syms, 1, syms.length);
        String restKey = String.join("", rest);
        if (!newNTMap.containsKey(restKey)) {
            String newNT = "Y" + counter[0]++;
            newNTMap.put(restKey, newNT);
            nonTerminals.add(newNT);
            String sub = binarize(rest, cnfProds, counter, newNTMap);
            cnfProds.put(newNT, new ArrayList<>(List.of(sub)));
        }
        return syms[0] + newNTMap.get(restKey);
    }

    //  Helpers 
    private String[] splitRHS(String rhs) {
        // Each symbol is either a multi-char NT (uppercase letter(s)+digits) or single terminal
        List<String> parts = new ArrayList<>();
        int i = 0;
        while (i < rhs.length()) {
            char c = rhs.charAt(i);
            if (Character.isUpperCase(c)) {
                StringBuilder sb = new StringBuilder();
                sb.append(c);
                i++;
                while (i < rhs.length() && Character.isDigit(rhs.charAt(i)))
                    sb.append(rhs.charAt(i++));
                parts.add(sb.toString());
            } else {
                parts.add(String.valueOf(c));
                i++;
            }
        }
        return parts.toArray(new String[0]);
    }

    private boolean isNonTerminal(String s) {
        return s.length() >= 1 && Character.isUpperCase(s.charAt(0));
    }

    public void printProductions() {
        System.out.println("Productions:");
        for (Map.Entry<String, List<String>> e : productions.entrySet())
            for (String rhs : e.getValue())
                System.out.println("  " + e.getKey() + " → " + rhs);
    }

    public void printGrammar() {
        System.out.println("VN = " + nonTerminals);
        System.out.println("VT = " + terminals);
        System.out.println("S  = " + startSymbol);
        printProductions();
    }
}