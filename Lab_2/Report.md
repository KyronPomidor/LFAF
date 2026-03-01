# Determinism in Finite Automata. Conversion from NDFA to DFA. Chomsky Hierarchy.

**Course:** Formal Languages & Finite Automata  
**Author:** Cretu Dumitru  
**Credits:** Vasile Drumea, Irina Cojuhari

---

## Theory

A finite automaton is a mechanism used to represent processes of different kinds. It can be compared to a state machine as they both have similar structures and purposes. The word *finite* signifies that an automaton has a starting state and a set of final states — meaning any process modeled by an automaton has a definite beginning and ending.

Based on the structure of an automaton, there are cases in which a single transition can lead to multiple states, which introduces **non-determinism**. In systems theory, determinism characterizes how predictable a system is. When random or ambiguous variables are involved, the system becomes stochastic or non-deterministic.

Automata can therefore be classified as **deterministic (DFA)** or **non-deterministic (NFA/NDFA)**, and there exist well-defined algorithms — such as the **Subset Construction Algorithm** — that convert an NDFA into an equivalent DFA.

---

## Objectives

1. Understand what an automaton is and what it can be used for.
2. Extend the existing grammar class with a method that classifies a grammar according to the **Chomsky Hierarchy**.
3. Given a finite automaton definition (per assigned variant), implement the following:
   - a. Convert a finite automaton to a regular grammar.
   - b. Determine whether the FA is deterministic or non-deterministic.
   - c. Convert an NDFA to a DFA using the Subset Construction Algorithm.
   - d. *(Bonus)* Represent the finite automaton graphically using an external library.

---

## Finite Automaton Definition

The FA used in this lab is defined as:

- **States:** Q = { q0, q1, q2, q3, q4 }
- **Alphabet:** Σ = { a, b }
- **Start state:** q0
- **Final states:** F = { q4 }
- **Transitions (δ):**

| From | Input | To       |
|------|-------|----------|
| q0   | a     | q1       |
| q1   | b     | q1       |
| q1   | a     | q2       |
| q2   | b     | q2, q3   |
| q3   | b     | q4       |
| q3   | a     | q1       |

Note that state `q2` on input `b` leads to **two** states (`q2` and `q3`), which is what makes this a **non-deterministic** automaton.

---

## Chomsky Hierarchy Classification

The Chomsky hierarchy classifies grammars into four types:

| Type | Name               | Production Rule Form              |
|------|--------------------|-----------------------------------|
| 0    | Unrestricted       | α → β (no restrictions)          |
| 1    | Context-Sensitive  | αAβ → αγβ                         |
| 2    | Context-Free       | A → γ                             |
| 3    | Regular            | A → aB or A → a (right-linear)   |

### Implementation

A `classifyGrammar()` method was added to the `Grammar` class. It inspects all production rules and applies the following logic:

```java
String classifyGrammar() {
    boolean isType3 = true, isType2 = true, isType1 = true;

    for (Map.Entry<String, List<String>> entry : P.entrySet()) {
        String lhs = entry.getKey();

        // Type 2 requires single non-terminal on LHS
        if (lhs.length() != 1 || !VN.contains(lhs))
            isType2 = isType3 = false;

        for (String rhs : entry.getValue()) {
            // Type 3: A -> aB or A -> a
            if (!(rhs.matches("[a-z][A-Z]?") || rhs.equals("ε")))
                isType3 = false;

            // Type 1: |lhs| <= |rhs|
            if (rhs.length() < lhs.length())
                isType1 = false;
        }
    }

    if (isType3) return "Type 3 - Regular Grammar";
    if (isType2) return "Type 2 - Context-Free Grammar";
    if (isType1) return "Type 1 - Context-Sensitive Grammar";
    return "Type 0 - Unrestricted Grammar";
}
```

For the FA in this lab, the derived grammar is **Type 3 (Regular)**, as all productions are right-linear.

---

## Implementation Description

The implementation is organized in a single file `FiniteAutomatonApp.java` containing a `FiniteAutomatonApp` main class and a `FAProcessor` helper class.

### FAProcessor Class

The `FAProcessor` class encapsulates all finite automaton operations. It stores:

- `states[]` — array of state names
- `alphabet[]` — input symbols
- `finalStates` — set of accepting states
- `nfa` — transition map of the form `Map<String, Map<Character, Set<String>>>`

The NFA is initialized in the `buildNFA()` method using an `addTransition()` helper, which populates the nested map structure.

```java
void addTransition(String from, char input, String to) {
    nfa.get(from).putIfAbsent(input, new HashSet<>());
    nfa.get(from).get(input).add(to);
}
```

---

### a) Conversion to Regular Grammar

The `convertToGrammar()` method maps each state to a capital letter (A, B, C, …) and prints production rules in the form `A → aB`. If a state is a final state, it also emits the epsilon production `A → ε`.

```java
void convertToGrammar() {
    HashMap<String, String> mapToLetters = new HashMap<>();
    int i = 0;
    for (String state : states)
        mapToLetters.put(state, String.valueOf((char)('A' + i++)));

    for (String state : states) {
        Map<Character, Set<String>> transitions = nfa.get(state);
        for (Character input : transitions.keySet())
            for (String next : transitions.get(input))
                System.out.println(mapToLetters.get(state) + " -> " + input + mapToLetters.get(next));

        if (finalStates.contains(state))
            System.out.println(mapToLetters.get(state) + " -> ε");
    }
}
```

#### Example Output

```
A -> aB
B -> bB
B -> aC
C -> bC
C -> bD
D -> bE
D -> aB
E -> ε
```

This output represents a **right-linear regular grammar** equivalent to the original FA.

---

### b) Determinism Check

The `checkDeterminism()` method iterates over all states and alphabet symbols and checks whether any transition leads to more than one state. If so, the automaton is classified as an NFA.

```java
void checkDeterminism() {
    boolean isDFA = true;
    for (String state : states)
        for (char symbol : alphabet) {
            Set<String> next = nfa.get(state).getOrDefault(symbol, new HashSet<>());
            if (next.size() > 1) isDFA = false;
        }
    System.out.println(isDFA ? "DFA" : "NFA (NDFA)");
}
```

#### Result

```
NFA (NDFA)
```

The automaton is confirmed to be **non-deterministic** due to the transition `q2 --b--> {q2, q3}`.

---

### c) NFA to DFA Conversion (Subset Construction)

The `convertNFAtoDFA()` method implements the **Subset Construction Algorithm**. Starting from the set `{q0}`, it processes each symbol of the alphabet and computes the set of reachable states. New composite states are added to a queue until no new states are discovered.

```java
void convertNFAtoDFA() {
    Set<Set<String>> dfaStates = new HashSet<>();
    Queue<Set<String>> queue = new LinkedList<>();
    Set<String> start = new HashSet<>();
    start.add("q0");
    queue.add(start);
    dfaStates.add(start);

    while (!queue.isEmpty()) {
        Set<String> current = queue.poll();
        for (char symbol : alphabet) {
            Set<String> newState = new HashSet<>();
            for (String s : current)
                newState.addAll(nfa.get(s).getOrDefault(symbol, new HashSet<>()));
            System.out.println(current + " --" + symbol + "--> " + newState);
            if (!newState.isEmpty() && !dfaStates.contains(newState)) {
                dfaStates.add(newState);
                queue.add(newState);
            }
        }
    }
}
```

#### Resulting DFA Transitions

| DFA State     | Input `a` | Input `b`      |
|---------------|-----------|----------------|
| {q0}          | {q1}      | ∅              |
| {q1}          | {q2}      | {q1}           |
| {q2}          | ∅         | {q2, q3}       |
| {q2, q3}      | {q1}      | {q2, q3, q4}   |
| {q2, q3, q4}  | {q1}      | {q2, q3, q4}   |

Any DFA state containing `q4` is a **final state** in the resulting DFA. The composite state `{q2, q3, q4}` is therefore accepting.

---

### d) Graphical Representation (Bonus)

The `generateGraph()` method uses the **graphviz-java** library (`guru.nidi.graphviz`) to produce a PNG diagram of the NFA. Final states are rendered with a double circle, and each edge is labeled with its input symbol.

```java
void generateGraph(String filename) throws Exception {
    MutableGraph g = mutGraph("FA").setDirected(true);
    Map<String, MutableNode> nodes = new HashMap<>();

    for (String s : states) {
        MutableNode n = mutNode(s);
        if (finalStates.contains(s)) n.add(Shape.DOUBLE_CIRCLE);
        nodes.put(s, n);
        g.add(n);
    }

    for (String from : nfa.keySet())
        for (Character c : nfa.get(from).keySet())
            for (String to : nfa.get(from).get(c))
                nodes.get(from).addLink(to(nodes.get(to)).with(Label.of("" + c)));

    Graphviz.fromGraph(g).render(Format.PNG).toFile(new java.io.File(filename));
}
```

The output is saved as `fa.png` and visually depicts all states, transitions, and final states of the original NFA.

---

## Results

The program successfully:

1. Built the NFA from the given transition definition.
2. Converted it to a **right-linear regular grammar**.
3. Identified the automaton as **non-deterministic (NFA)**.
4. Applied the **Subset Construction Algorithm** to produce an equivalent DFA.
5. Generated a **PNG visualization** of the NFA graph.

### Console Output Summary

```
Running NEW VERSION

=== REGULAR GRAMMAR ===
A -> aB
B -> bB
B -> aC
C -> bC
C -> bD
D -> bE
D -> aB
E -> ε

=== DFA OR NFA? ===
NFA (NDFA)

=== NFA TO DFA ===
[q0] --a--> [q1]
[q0] --b--> []
[q1] --a--> [q2]
[q1] --b--> [q1]
[q2] --a--> []
[q2] --b--> [q2, q3]
[q2, q3] --a--> [q1]
[q2, q3] --b--> [q2, q3, q4]
[q2, q3, q4] --a--> [q1]
[q2, q3, q4] --b--> [q2, q3, q4]

=== GENERATING VISUAL ===
Graph saved as fa.png
```

---

## Conclusions

This laboratory work explored non-determinism in finite automata and demonstrated the process of converting an NDFA to an equivalent DFA using the Subset Construction Algorithm.

The key insight is that while non-determinism may appear to give an automaton more expressive power, any NDFA can be transformed into a DFA that recognizes exactly the same language — the two models are computationally equivalent.

The implementation also reinforced the relationship between the Chomsky hierarchy and finite automata: regular grammars (Type 3) correspond directly to finite automata, and the conversion between them is both straightforward and systematic.

One challenge encountered was correctly handling composite states during the subset construction — in particular, ensuring that any composite DFA state containing an original final state is itself marked as a final state. This was resolved by checking for set intersection with `finalStates` when labeling DFA states.

Overall, the lab provided a solid practical foundation for understanding determinism, grammar classification, and the structural equivalence of different formal language representations.