# Syntax Analysis. Implementing a Parser and AST.

**Course:** Formal Languages & Finite Automata  
**Author:** Kiril Boboc

---

## Theory

Syntax analysis is the second phase of a compiler or interpreter, immediately following lexical analysis. Where the lexer groups characters into tokens, the **parser** groups tokens into grammatical structures and verifies that their arrangement conforms to the rules of the language. The result of this phase is an **Abstract Syntax Tree (AST)** — a hierarchical representation of the program's structure.

The AST is "abstract" in the sense that it omits syntactic noise that carries no semantic meaning: parentheses, colons, commas, and whitespace are not present in the tree. What remains is the logical skeleton of the program — the nesting of loops, the operands of expressions, the branches of conditionals.

Parsers are commonly implemented as **recursive-descent parsers**, where each grammar rule is encoded as a method that calls other methods corresponding to sub-rules. This technique maps naturally onto context-free grammars (CFGs), which are the standard formalism for describing programming language syntax — just as regular expressions and finite automata underlie lexical analysis, CFGs and pushdown automata underlie parsing.

The output of the parser — the AST — is passed to later compiler phases: semantic analysis, type checking, optimization, and code generation.

---

## Objectives

1. Understand what an Abstract Syntax Tree is and why it is used.
2. Design AST node types that cover all constructs of the target language.
3. Implement a recursive-descent parser that builds an AST from the token stream produced in Lab 3.

---

## Language Overview

The parser targets the same **Minecraft-themed DSL** introduced in Lab 3. The constructs that must be parsed are:

1. `for` loops — `for i from 1 to 5`
2. `while` loops — `while x > 0`
3. `if / else` conditionals — `if(a == 10) / else:`
4. Variable assignments — `done = true`
5. Function / built-in calls — `log("hello")`, `print("...")`
6. Arithmetic and comparison expressions — `x - 1`, `a == 10`, `x > 0`
7. Literals — numbers, strings, booleans
8. Indentation-based block structure inherited from Lab 3

---

## AST Node Types

Each syntactic construct in the language is represented by a dedicated node class. All classes implement the `Node` interface, which requires a single `toTree(String indent)` method used for pretty-printing.

### Statement Nodes

| Node | Represents | Key Fields |
|---|---|---|
| `ProgramNode` | Entire file | `List<Node> statements` |
| `ForNode` | `for i from 1 to 5` | `variable`, `start`, `end`, `body` |
| `WhileNode` | `while x > 0` | `condition`, `body` |
| `IfNode` | `if / else` | `condition`, `thenBody`, `elseBody` |
| `AssignNode` | `x = expr` | `name`, `value` |
| `CallStatementNode` | `log(...)` as statement | wraps `CallNode` |

### Expression Nodes

| Node | Represents | Key Fields |
|---|---|---|
| `BinaryOpNode` | `x - 1`, `a == 10` | `op`, `left`, `right` |
| `CallNode` | function call expression | `callee`, `List<Node> args` |
| `IdentNode` | variable reference | `name` |
| `NumberNode` | numeric literal | `value` |
| `StringNode` | string literal | `value` |
| `BoolNode` | `true` / `false` | `value` |

The separation between statement nodes and expression nodes reflects the grammar: expressions can appear inside statements (as conditions, values, arguments), but statements cannot appear inside expressions.

---

## Implementation Description

### Grammar

The parser is based on the following simplified context-free grammar:

```
program      → statement* EOF
statement    → forStmt | whileStmt | ifStmt | assignStmt | callStmt
forStmt      → FOR IDENT FROM expr TO expr NEWLINE block
whileStmt    → WHILE expr NEWLINE block
ifStmt       → IF LPAREN? expr RPAREN? NEWLINE block (ELSE NEWLINE block)?
assignStmt   → IDENT ASSIGN expr NEWLINE
callStmt     → IDENT LPAREN argList RPAREN NEWLINE
block        → INDENT statement+ DEDENT
expr         → comparison
comparison   → addition ((EQ|NEQ|LT|GT|LTE|GTE) addition)*
addition     → term ((PLUS|MINUS) term)*
term         → primary ((STAR|SLASH) primary)*
primary      → NUMBER | STRING | TRUE | FALSE
             | IDENT (LPAREN argList RPAREN)?
             | LPAREN expr RPAREN
argList      → (expr (COMMA expr)*)?
```

### Recursive-Descent Structure

Each grammar rule corresponds to one method in the `Parser` class. The parser holds a list of tokens and a position index; helper methods `peek()`, `consume()`, and `match()` handle navigation:

```java
private Token consume(TokenType type) {
    if (!check(type))
        throw new RuntimeException("Expected " + type + " but got " + peek());
    return tokens.get(pos++);
}

private boolean match(TokenType type) {
    if (check(type)) { pos++; return true; }
    return false;
}
```

The entry point `parse()` calls `parseStatement()` in a loop until `EOF` is reached, collecting top-level statements into a `ProgramNode`.

### Operator Precedence

Expression parsing is stratified into four levels so that operator precedence is handled correctly without any explicit precedence table:

```
parseExpr → parseComparison → parseAddition → parseTerm → parsePrimary
```

Each level calls the level below it for its operands, and only consumes operators at its own precedence level. This means `*` binds more tightly than `+`, which binds more tightly than `==`, entirely by the structure of the call chain.

```java
private Node parseAddition() {
    Node left = parseTerm();
    while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
        String op = peek().type == TokenType.PLUS ? "+" : "-";
        pos++;
        Node right = parseTerm();
        left = new BinaryOpNode(op, left, right);
    }
    return left;
}
```

### Block Parsing with INDENT / DEDENT

Because Lab 3's lexer already emits explicit `INDENT` and `DEDENT` tokens for indented blocks, the parser can treat blocks uniformly without ever inspecting whitespace. Parsing a block is simply:

```java
private List<Node> parseBlock() {
    consume(TokenType.INDENT);
    List<Node> stmts = new ArrayList<>();
    while (!check(TokenType.DEDENT) && !check(TokenType.EOF)) {
        stmts.add(parseStatement());
        skipNewlines();
    }
    consume(TokenType.DEDENT);
    return stmts;
}
```

This is a direct benefit of the structural tokenization done in the previous lab.

### Lookahead for Statement Disambiguation

Both assignments and call statements begin with an `IDENT` token, so one token of lookahead is used to distinguish them:

```java
if (t == TokenType.IDENT) {
    if (peek(1).type == TokenType.LPAREN)
        return parseCallStatement();   // IDENT ( → call
    return parseAssign();              // IDENT = → assign
}
```

### Handling Quirks of the Input

The sample source contains `else:`, where the `:` is not part of the language grammar and is emitted as `ILLEGAL` by the lexer. The parser tolerates this gracefully by skipping any `ILLEGAL` tokens immediately after consuming `ELSE`:

```java
consume(TokenType.ELSE);
while (check(TokenType.ILLEGAL)) pos++;  // skip stray ":"
match(TokenType.NEWLINE);
```

Similarly, the `if` condition may or may not be wrapped in parentheses — both `if(a == 10)` and `if a == 10` are accepted — by using `match()` (optional consume) rather than `consume()` for the parentheses.

---

## Results

Given the following sample input (same as Lab 3):

```
for i from 1 to 5
    log("hello")
-- What?
while x > 0
    x = x - 1
if(a == 10)
    print("a == 10 - true")
else:
    print("a == 10 - false")
done = true
```

The parser produces the following AST:

```
Program
  For(i)
    from: Number(1)
    to:   Number(5)
    body:
      Call(log) args=[String("hello")]
  While
    cond: BinOp(>, Ident(x), Number(0))
    body:
      Assign(x) = BinOp(-, Ident(x), Number(1))
  If
    cond: BinOp(==, Ident(a), Number(10))
    then:
      Call(print) args=[String("a == 10 - true")]
    else:
      Call(print) args=[String("a == 10 - false")]
  Assign(done) = Bool(true)
```

The comment `-- What?` produces no node, as it was already discarded at the lexing stage. The `else:` colon is silently absorbed. Every expression is correctly nested — `x - 1` becomes a `BinaryOpNode` whose children are `Ident(x)` and `Number(1)`, rather than a flat list of tokens.

---

## Conclusions

This laboratory work demonstrates how a flat token stream is transformed into a structured Abstract Syntax Tree through recursive-descent parsing.

The key design decisions were:

1. **One node class per construct.** Keeping statement and expression nodes separate reflects the grammar directly and makes the tree easy to traverse or extend — adding a new construct means adding one class and one parser method, nothing else.

2. **Operator precedence through call-chain depth.** Stratifying expression parsing across `parseComparison → parseAddition → parseTerm → parsePrimary` encodes precedence structurally, requiring no precedence table or special logic.

3. **Leveraging Lab 3's INDENT/DEDENT tokens.** Because the lexer already converts indentation into explicit structural tokens, the parser treats all blocks uniformly with a single `parseBlock()` method. This clean separation of concerns is a direct payoff of the Lab 3 design.

4. **Graceful handling of input quirks.** Real source code often contains constructs that don't fit the grammar cleanly — the `else:` colon is a good example. Rather than crashing, the parser skips `ILLEGAL` tokens in known positions, making it robust to minor syntactic irregularities.

Overall, this lab bridges formal language theory and practical compiler construction: the context-free grammar maps directly onto the recursive method structure, just as regular expressions in Lab 3 mapped onto the finite-automaton-based token scanner.