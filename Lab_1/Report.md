# Intro to Formal Languages. Regular Grammars. Finite Automata

**Course:** Formal Languages & Finite Automata  
**Author:** Kiril Boboc

---

## Theory

A formal language is a mathematically defined system used to describe structured communication between entities. Unlike natural languages, formal languages follow strict syntactic rules and are precisely defined.

A formal language is described using:

- **Alphabet (Σ)** – a finite set of valid symbols  
- **Vocabulary (Σ\*)** – all possible strings formed using the alphabet  
- **Grammar (G)** – a set of production rules defining valid strings  

A grammar is formally represented as:

G = (Vₙ, Vₜ, P, S)

Where:

- Vₙ – set of non-terminal symbols  
- Vₜ – set of terminal symbols  
- P – set of production rules  
- S – start symbol  

Regular grammars are the simplest type in the Chomsky hierarchy and are equivalent to finite automata. This means that any regular grammar can be transformed into a finite automaton that recognizes the same language.

---

## Objectives

- Understand what a formal language is  
- Identify the components of a grammar  
- Implement a Regular Grammar  
- Generate valid strings from the grammar  
- Convert Grammar into Finite Automaton  
- Verify whether a string belongs to the language  
- Organize the project in a GitHub repository  

---

## Grammar Definition Used

Non-terminals:

Vₙ = {S, D, R}

Terminals:

Vₜ = {a, b, c, d, f}

Productions:

- S → aS | bD | fR  
- D → cD | dR | d  
- R → bR | f  

Start symbol:

S

This grammar is a **right-linear grammar**, therefore it is a **regular grammar**.

---

## Implementation Description

### Grammar Class

The `Grammar` class models the formal grammar using sets and maps. It stores:

- Non-terminals
- Terminals
- Production rules
- Start symbol

It also includes a method that generates a valid string by repeatedly replacing non-terminals using randomly selected production rules until only terminal symbols remain.

### Simplified Structure
```
class Grammar {
    Set<String> VN;
    Set<String> VT;
    Map<String, List<String>> P;
    String startSymbol;

    String generateString() { ... }
}

### The `generateString()` Method

The `generateString()` method:

- Starts from the start symbol **S**
- Replaces non-terminals using production rules
- Continues until no non-terminals remain
- Returns the generated word

---

## Finite Automaton Class

The `FiniteAutomaton` class represents the automaton derived from the grammar.

It contains:

- **Q** – set of states
- **Σ** – alphabet
- **δ** – transition function
- **q₀** – start state
- **F** – final states

The constructor converts grammar productions into transitions.
```
### Simplified Structure

class FiniteAutomaton {
    Set<String> Q;
    Set<String> Sigma;
    Map<String, Map<String, String>> delta;
    String q0;
    Set<String> F;

    boolean stringBelongToLanguage(String input) { ... }
}
```
### The `stringBelongToLanguage()` Method

The method:

- Starts from **q₀**
- Processes each input symbol
- Follows transitions
- Accepts the string if the final state is reached

---
```
## Main Class

The `Main` class initializes the grammar and automaton, generates example strings, and checks user input.
```
public static void main(String[] args) {

    Grammar grammar = new Grammar(...);

    for (int i = 0; i < 5; i++)
        System.out.println(grammar.generateString());

    FiniteAutomaton fa = new FiniteAutomaton(grammar);

    String input = scanner.nextLine();
    System.out.println(fa.stringBelongToLanguage(input));
}
```
## Results

The program successfully:

- Generates valid strings from the grammar
- Converts the grammar into a finite automaton
- Validates strings using state transitions

### Example Generated Strings

- `abcf`
- `aaabdf`
- `fbbf`
- `abdf`
- `aabcf`

### Example Validation

Input: `abdf`  
Output: `true`

Input: `abc`  
Output: `false`

---

## Conclusions

This laboratory work demonstrates the equivalence between regular grammars and finite automata.

The grammar can generate strings, while the finite automaton can recognize them.

The implementation confirms the theoretical relationship between the two concepts and provides a solid foundation for further study of formal languages.
