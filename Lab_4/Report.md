# Regular Expressions. Implementing a String Generator.

**Course:** Formal Languages & Finite Automata  
**Author:** Kiril Boboc

---

## Theory

A **regular expression** is a formal notation for describing a regular language — the simplest class of languages in the Chomsky hierarchy. Regular expressions are built from an alphabet using three base operations: concatenation, union (`|`), and Kleene star (`*`). From these, additional quantifiers such as `+`, `?`, and fixed repetition (`^n`) are derived.

Every regular expression is equivalent to a finite automaton. This means any string that can be described by a regex can also be recognized by a DFA or NFA. In practical terms, this connection is what makes regex-based tools like lexers and pattern matchers possible — a regex is compiled into a finite automaton that processes input character by character.

This lab works in the opposite direction: instead of recognizing whether a string belongs to a language, the goal is to **generate** valid strings that satisfy a given regular expression. This requires parsing the expression into its structural components and then making random choices at each alternation or repetition point, within defined bounds.

---

## Objectives

1. Understand the structure of regular expressions and how they define languages.
2. Implement a dynamic parser that tokenizes a regex pattern into its components.
3. Implement a generator that produces valid strings conforming to a given regex.
4. Show the sequence of processing steps applied when generating a string (bonus).

---

## Language Overview

The generator targets a custom regex notation used in the lab assignment. The supported constructs are:

| Construct | Syntax | Meaning |
|-----------|--------|---------|
| Literal character | `A`, `2`, `O` | Matches exactly that character |
| Union group | `(A\|B\|C)` | Pick one alternative |
| Kleene star | `*` | Repeat 0 to 5 times (bounded) |
| One or more | `+` | Repeat 1 to 5 times (bounded) |
| Optional | `?` | Appear 0 or 1 times |
| Fixed repetition | `^n` | Repeat exactly n times |

The three expressions from Variant 3 are:

```
O(P|Q|R)+2(3|4)
A*B(C|D|E)F(G|H|I)^2
J+K(L|M|N)*O?(P|Q)^3
```

---

## Implementation Description

### Token Class

Each component of the regex is represented as a `Token` — a simple data object holding the token type (`"char"` or `"group"`), the character value for literal tokens, the array of alternatives for group tokens, and the quantifier:

```java
static class Token {
    String type;       // "char" or "group"
    String ch;         // for char tokens
    String[] alts;     // for group tokens
    String quantifier; // "1", "*", "+", "?", or "2", "3", ...
}
```

### Step 1 — Tokenizer

The `tokenize()` method walks the pattern string character by character. When it encounters `(`, it scans forward to find the matching `)`, extracts the inner string, and splits on `|` to get the alternatives. It then reads the character immediately following `)` to determine the quantifier. For literal characters the same quantifier detection is applied after the character itself:

```java
if (pattern.charAt(i) == '(') {
    // find closing ')', extract inner, split on '|'
    // read quantifier: *, +, ?, or ^n
    tokens.add(new Token("group", null, alts, quantifier));
} else {
    char ch = pattern.charAt(i);
    // read quantifier
    tokens.add(new Token("char", String.valueOf(ch), null, quantifier));
}
```

Order of quantifier detection matters — `^n` must be checked before falling back to a default of `"1"`, otherwise fixed repetitions are silently lost.

### Step 2 — Quantifier Resolution

The `pickCount()` method converts a quantifier string to a concrete integer at generation time:

```java
static int pickCount(String q) {
    switch (q) {
        case "*": return rand.nextInt(STAR_MAX + 1);   // 0 to 5
        case "+": return rand.nextInt(STAR_MAX) + 1;   // 1 to 5
        case "?": return rand.nextInt(2);              // 0 or 1
        default:  return Integer.parseInt(q);          // exact count
    }
}
```

The constant `STAR_MAX = 5` caps unbounded quantifiers (`*` and `+`) to prevent generating arbitrarily long strings, as required by the task specification.

### Step 3 — Generator

The `generate()` method iterates over the token list. For each token it resolves the quantifier to a count, then either repeats the literal character or randomly selects from the alternatives that many times:

```java
static String generate(List<Token> tokens) {
    StringBuilder sb = new StringBuilder();
    for (Token t : tokens) {
        int count = pickCount(t.quantifier);
        if (t.type.equals("char")) {
            for (int k = 0; k < count; k++) sb.append(t.ch);
        } else {
            for (int k = 0; k < count; k++)
                sb.append(t.alts[rand.nextInt(t.alts.length)]);
        }
    }
    return sb.toString();
}
```

Because both tokenizing and generating are driven by the token list rather than the raw pattern string, the same code handles any valid input expression without modification.

### Bonus — Processing Steps

The `showSteps()` method iterates over the same token list before generation and prints a plain-English description of what each token will do. This makes the processing order visible and auditable:

```java
static void showSteps(List<Token> tokens) {
    int step = 1;
    for (Token t : tokens) {
        // describe token and quantifier in plain English
        System.out.printf("  Step %d: %-20s -> %s%n", step++, tokenStr, action);
    }
}
```

---

## Results

Running the program against all three expressions from Variant 3 produces the following output (one sample run):

```
=== Regex String Generator - Variant 3 ===

Expression 1: O(P|Q|R)+2(3|4)
  Processing steps:
    Step 1: O                    -> write 'O' exactly once
    Step 2: (P|Q|R)              -> pick from (P | Q | R) 1-5 times (+)
    Step 3: 2                    -> write '2' exactly once
    Step 4: (3|4)                -> pick one of (3 | 4)
  Generated: [OPQR24]  [OPP23]  [ORQQP24]

Expression 2: A*B(C|D|E)F(G|H|I)^2
  Processing steps:
    Step 1: A                    -> write 'A' 0-5 times (*)
    Step 2: B                    -> write 'B' exactly once
    Step 3: (C|D|E)              -> pick one of (C | D | E)
    Step 4: F                    -> write 'F' exactly once
    Step 5: (G|H|I)              -> pick from (G | H | I) exactly 2 times
  Generated: [AABCFGI]  [BCFHH]  [AAABDFIG]

Expression 3: J+K(L|M|N)*O?(P|Q)^3
  Processing steps:
    Step 1: J                    -> write 'J' 1-5 times (+)
    Step 2: K                    -> write 'K' exactly once
    Step 3: (L|M|N)              -> pick from (L | M | N) 0-5 times (*)
    Step 4: O                    -> write 'O' 0 or 1 times (?)
    Step 5: (P|Q)                -> pick from (P | Q) exactly 3 times
  Generated: [JJKOPQP]  [JKMLNPQQ]  [JJJKOPPP]
```

All outputs conform to their respective expressions. The step trace confirms the generator processes tokens left to right in the same order they appear in the pattern.

---

## Conclusions

This laboratory work demonstrates how the formal structure of regular expressions can be used not only to recognize strings but also to generate them.

The key design decisions were:

1. **Tokenizing the pattern dynamically** rather than hardcoding generation logic for each expression. This means any valid expression using the supported constructs can be passed as input and the generator adapts automatically.
2. **Separating quantifier resolution** (`pickCount`) from string assembly (`generate`), which keeps each concern isolated and easy to modify — for example, changing the cap from 5 to 10 requires changing one constant.
3. **Reusing the token list for both step display and generation**, so the processing trace always reflects the exact sequence the generator follows rather than being a separate description that could drift out of sync.

One challenge encountered was handling the `^n` quantifier correctly during tokenization. Because `^` is followed by a digit, it must be detected as a two-character sequence before the general character handling takes over, otherwise the caret would be treated as a literal token and the digit as a separate one.

Overall, the implementation connects regex theory directly to a practical generation task, reinforcing the idea that regular expressions define a language and that working with that language — whether recognizing or producing strings — follows from understanding its structure.