import java.util.*;

public class FiniteAutomaton {
    public Set<String> Q; // States
    public Set<String> Sigma; // Alphabet
    public Map<String, Map<String, String>> delta; // Transitions
    private String q0; // Start state
    private Set<String> F; // Final states

    // Constructor: builds FA from grammar
    public FiniteAutomaton(Grammar grammar) {

        this.Q = new HashSet<>(grammar.VN);
        this.Sigma = new HashSet<>(grammar.VT);
        this.q0 = grammar.startSymbol;
        this.F = new HashSet<>();
        this.delta = new HashMap<>();

        // Initialize transition map
        for (String state : Q) {
            delta.put(state, new HashMap<>());
        }

        // Convert productions into transitions
        for (String state : grammar.P.keySet()) {
            ArrayList<String> productions = grammar.P.get(state);

            for (String prod : productions) {

                // Case: terminal only
                if (prod.length() == 1 && Sigma.contains(prod)) {
                    F.add("FINAL"); // create a special final state
                    delta.get(state).put(prod, "FINAL"); // make transition to it
                }

                // Case: A - aB
                else if (prod.length() >= 2) {
                    String terminal = String.valueOf(prod.charAt(0));
                    String nextState = String.valueOf(prod.charAt(1));

                    delta.get(state).put(terminal, nextState);
                }
            }
        }
    }

    public boolean stringBelongToLanguage(String input) {
        String currentState = q0;

        for (char ch : input.toCharArray()) {
            String symbol = String.valueOf(ch);

            Map<String, String> transitions = delta.get(currentState);
            
            if (transitions != null && transitions.containsKey(symbol)) {
                currentState = transitions.get(symbol);
            } else {
                return false;
            }
        }

        // Accept only if in final state
        return F.contains(currentState);
    }
}
