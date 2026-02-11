import java.util.*;

public class Main {
    public static void main(String[] args) {
        HashSet<String> VN = new HashSet<>(Arrays.asList("S", "D", "R"));
        HashSet<String> VT = new HashSet<>(Arrays.asList("a", "b", "c", "d", "f"));

        HashMap<String, ArrayList<String>> P = new HashMap<>();
        P.put("S", new ArrayList<>(Arrays.asList("aS", "bD", "fR")));
        P.put("D", new ArrayList<>(Arrays.asList("cD", "dR", "d")));
        P.put("R", new ArrayList<>(Arrays.asList("bR", "f")));

        Grammar grammar = new Grammar(VN, VT, P, "S");

        // Generate 5 strings
        System.out.println("Generated Strings:");
        for (int i = 0; i < 5; i++) {
            System.out.println(grammar.generateString());
        }


        FiniteAutomaton fa = new FiniteAutomaton(grammar);

        System.out.println("Enter a string: ");

        Scanner input = new Scanner(System.in);

        String toCheck = input.nextLine();
        System.out.println(toCheck);
        System.out.println(fa.stringBelongToLanguage(toCheck));
    }
}
