# Intro to Formal Languages. Regular Grammars. Finite Automata

**Course:** Formal Languages & Finite Automata  
**Author:** Kiril Boboc

---

## Theory

A formal language is a mathematically defined system used to describe structured communication between entities. Unlike natural languages, formal languages follow strict syntactic rules and are precisely defined.

A formal language is described using:

**Alphabet (Σ)** – a finite set of valid symbols  
**Vocabulary (Σ\*)** – all possible strings formed using the alphabet  
**Grammar (G)** – a set of production rules defining valid strings  

A grammar is formally represented as:

G = (Vₙ, Vₜ, P, S)

Where:

1) Vₙ – set of non-terminal symbols  
2) Vₜ – set of terminal symbols  
3) P – set of production rules  
4) S – start symbol  

Regular grammars are the simplest type in the Chomsky hierarchy and are equivalent to finite automata. This means that any regular grammar can be transformed into a finite automaton that recognizes the same language.

---

## Objectives

1) Understand what a formal language is  
2) Identify the components of a grammar  
3) Implement a Regular Grammar  
4) Generate valid strings from the grammar  
5) Convert Grammar into Finite Automaton  
6) Verify whether a string belongs to the language  
7) Organize the project in a GitHub repository  

---

## Grammar Definition Used

Non-terminals:

Vₙ = {S, D, R}

Terminals:

Vₜ = {a, b, c, d, f}

Productions:

1) S → aS | bD | fR  
2) D → cD | dR | d  
3) R → bR | f  

Start symbol:

S

This grammar is a **right-linear grammar**, therefore it is a **regular grammar**.

---

## Implementation Description

### Grammar Class

The `Grammar` class models the formal grammar using sets and maps. It stores:

1) Non-terminals
2) Terminals
3) Production rules
4) Start symbol

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
```
### The `generateString()` Method

The `generateString()` method:

1) Starts from the start symbol **S**
2) Replaces non-terminals using production rules
3) Continues until no non-terminals remain
4) Returns the generated word

---

## Finite Automaton Class

The `FiniteAutomaton` class represents the automaton derived from the grammar.

A **Finite Automaton (FA)** is a mathematical model used to recognize patterns in strings. It is an abstract machine that reads an input string symbol by symbol and changes its state according to predefined transition rules.
Finite automata are equivalent to regular grammars and are commonly used in text processing, compilers, and pattern matching.

A finite automaton is formally defined as:

FA = (Q, Σ, δ, q₀, F)

It contains:

1) **Q** – set of states
2) **Σ** – alphabet
3) **δ** – transition function
4) **q₀** – start state
5) **F** – final states

The constructor converts grammar productions into transitions. Later the stringBelongToLanguage method checks if the input is valid according to the rules from grammar.

### Simplified Structure
```
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

1) Starts from **q₀**
2) Processes each input symbol
3) Follows transitions
4) Accepts the string if the final state is reached

---

## Main Class

The `Main` class initializes the grammar and automaton, generates example strings, and checks user input. I used random to generate string based on the rules based in the grammar in HashMaps above. To check the validity of the input it uses FiniteAutomation object, that gets grammar object.
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

1) Generates valid strings from the grammar
2) Converts the grammar into a finite automaton
3) Validates strings using state transitions

### Example Generated Strings

1) `abcf`
2) `aaabdf`
3) `fbbf`
4) `abdf`
5) `aabcf`

### Example Validation

Input: `abdf`  
Output: `true`

Input: `abc`  
Output: `false`

---

## Conclusions

This laboratory work demonstrates the equivalence between regular grammars and finite automata.

The grammar is capable of generating valid strings, while the finite automaton recognizes and validates them through state transitions.

During the implementation, I encountered some difficulties while designing the finite automaton, especially in representing transitions correctly. However, by combining different data structures (such as sets and hash maps) and doing additional research on automaton construction, I was able to successfully complete the realization.

The implementation confirms the theoretical relationship between regular grammars and finite automata and provides a solid foundation for further study of formal languages.