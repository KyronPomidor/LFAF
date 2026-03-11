# Lexical Analysis. Implementing a Lexer / Tokenizer.

**Course:** Formal Languages & Finite Automata  
**Author:** Cretu Dumitru

---

## Theory

Lexical analysis is the first phase of a compiler or interpreter. Its job is to read raw source code as a stream of characters and group those characters into meaningful units called **tokens**. The component that performs this task is called a **lexer**, **scanner**, or **tokenizer**.

A token is the smallest meaningful unit of a language — for example, a keyword like `if`, an operator like `+`, a number literal like `42`, or an identifier like `playerName`. The lexer does not care about whether the program is logically correct; it only cares about recognizing these atomic units and labeling them with a type.

Lexers are typically implemented using **regular expressions** or hand-written character-by-character scanning. Under the hood, regular expressions compile down to finite automata, which connects this lab directly to the theory studied in previous labs — a lexer is essentially a deterministic finite automaton operating over the characters of the source text.

The output of the lexer — a flat list of typed tokens — is then passed to the **parser**, which checks grammatical structure, and later to the **semantic analyzer** and code generator.

---

## Objectives

1. Understand what lexical analysis is.
2. Get familiar with the inner workings of a lexer / scanner / tokenizer.
3. Implement a sample lexer and show how it works.

---

## Language Overview

The lexer implemented in this lab targets a **Minecraft-themed DSL** (Domain Specific Language) — a small scripting language for describing in-game actions and logic. The language supports:

1. Variable assignment and arithmetic expressions
2. Comparison and equality operators
3. String and number literals
4. Identifiers and keywords
5. Comments (introduced by `--`)
6. Indentation-based block structure (like Python)

---

## Token Types

The lexer recognizes the following categories of tokens:

| Category      | Examples                          |
|---------------|-----------------------------------|
| Literals      | `42`, `"hello"`                   |
| Identifiers   | `player`, `health`, `myVar`       |
| Keywords      | `if`, `else`, `while`, `end`, … |
| Operators     | `+`, `-`, `*`, `/`, `==`, `!=`, `<=`, `>=`, `<`, `>`, `=` |
| Punctuation   | `(`, `)`, `,`, `.`               |
| Structure     | `INDENT`, `DEDENT`, `NEWLINE`    |
| Special       | `EOF`, `ILLEGAL`                 |

Comments beginning with `--` are consumed and discarded — they produce no token.

---

## Implementation Description

### Lexer Class

The `Lexer` class takes the full source string as input and exposes a single public method `tokenize()` that returns a `List<Token>`. Internally it splits the source into lines and processes each line in two passes: first measuring indentation, then scanning tokens using a compiled regex master pattern.

### Token Specifications

Each token type is described by a `TokenSpec` — a simple pair of a `TokenType` enum value and a regex pattern string. All specs are collected into a master `Pattern` by joining them with `|` into one large alternation:

```java
private static final List<TokenSpec> TOKEN_SPECS = List.of(
    new TokenSpec(TokenType.STRING,  "\"[^\"]*\""),
    new TokenSpec(TokenType.NUMBER,  "\\d+"),
    new TokenSpec(null,              "--[^\n]*"),   // comment -> skip
    new TokenSpec(TokenType.NEQ,     "!="),
    new TokenSpec(TokenType.EQ,      "=="),
    // ... etc
    new TokenSpec(TokenType.IDENT,   "[A-Za-z][A-Za-z0-9_]*")
);
```

Order is significant — longer or more specific patterns must come before shorter ones. For example, `!=` must appear before `=`, and `==` before `=`, otherwise the single-character pattern would match first and produce two wrong tokens instead of one correct one.

The master pattern is built once at class-load time:

```java
private static Pattern buildMaster() {
    List<String> parts = new ArrayList<>();
    for (TokenSpec spec : TOKEN_SPECS)
        parts.add("(" + spec.pattern + ")");
    return Pattern.compile(String.join("|", parts));
}
```

Each spec is wrapped in its own capturing group so that after a match, `specForMatch()` can identify which group fired and look up the corresponding `TokenSpec`.

### Indentation Handling

The language uses indentation to delimit blocks. The lexer tracks indent levels using a stack of integers:

```java
Deque<Integer> indentStack = new ArrayDeque<>();
indentStack.push(0);
```

At the start of each line the leading whitespace is counted. If the new indent level is greater than the top of the stack, an `INDENT` token is emitted and the new level is pushed. If it is less, `DEDENT` tokens are emitted and levels are popped until the stack top matches. This mirrors the approach used by Python's tokenizer.

```java
if (indent > current) {
    indentStack.push(indent);
    tokens.add(new Token(TokenType.INDENT));
} else {
    while (indent < indentStack.peek()) {
        indentStack.pop();
        tokens.add(new Token(TokenType.DEDENT));
    }
}
```

At end-of-file, any remaining open indent levels are closed by emitting the corresponding `DEDENT` tokens.

### Scanning Tokens

After the indent phase, the content portion of the line is scanned left-to-right using the master `Matcher`. At each position the matcher is anchored with `lookingAt()` (match from current position only):

```java
m.region(pos, content.length());
if (m.lookingAt()) {
    String matched = m.group();
    TokenSpec spec = specForMatch(m);
    // ...
    pos = m.end();
} else {
    tokens.add(new Token(TokenType.ILLEGAL, String.valueOf(content.charAt(pos))));
    pos++;
}
```

If nothing matches, the character is emitted as an `ILLEGAL` token and scanning continues — this gives useful error reporting rather than crashing.

### Keyword Resolution

Identifiers are matched by the generic `IDENT` pattern first, then checked against a keyword table via `Keywords.lookup()`. If the string is a reserved word, the token type is upgraded to the corresponding keyword type; otherwise it stays as `IDENT`:

```java
if (type == TokenType.IDENT) {
    type = Keywords.lookup(matched);
    tokens.add(type == TokenType.IDENT
        ? new Token(TokenType.IDENT, matched)
        : new Token(type));
}
```

This keeps the regex list clean — only one `IDENT` pattern is needed instead of a separate pattern for every keyword.

### Special Literal Handling

1. **Strings:** the surrounding double-quotes are stripped before storing the value — `"hello"` is stored as `hello`.
2. **Numbers:** the raw matched string is stored as-is for later parsing.
3. **Comments:** when the matched spec has `type == null`, the rest of the line is discarded with a `break`.

---

## Results

Given the following sample input:

```
player.health = 100
if player.health > 50
    give player "diamond"
-- this is a comment
set score = score + 1
```

The lexer produces the following token stream:

```
IDENT(player)  DOT  IDENT(health)  ASSIGN  NUMBER(100)  NEWLINE
IF  IDENT(player)  DOT  IDENT(health)  GT  NUMBER(50)  NEWLINE
INDENT
  GIVE  IDENT(player)  STRING(diamond)  NEWLINE
DEDENT
SET  IDENT(score)  ASSIGN  IDENT(score)  PLUS  NUMBER(1)  NEWLINE
EOF
```

Comments produce no tokens. Indentation is correctly detected as a single `INDENT`/`DEDENT` pair wrapping the `give` statement. Keyword `if`, `give`, and `set` are correctly distinguished from identifiers.

---

## Conclusions

This laboratory work demonstrates how a lexer transforms raw source text into a structured token stream that downstream compiler phases can work with reliably.

The key design decisions were:

1. Using a **single compiled master regex** with ordered alternation groups for efficient and maintainable scanning, rather than trying each pattern separately in a loop.
2. Keeping **keyword resolution separate** from pattern matching, which avoids having to list every keyword as its own regex and makes the keyword table easy to extend.
3. Handling **indentation structurally** via a stack, producing explicit `INDENT`/`DEDENT` tokens so the parser can treat blocks uniformly without caring about whitespace.

One challenge encountered was ensuring correct operator precedence in the pattern list — multi-character operators like `!=` and `==` had to be listed before their single-character prefixes `!` and `=`. This is a common pitfall in lexer construction and underlines why token spec ordering matters.

Overall, the implementation shows how regular expressions and finite automaton theory apply directly to practical language tooling, bridging the gap between formal language theory and real compiler design.