import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent parser.
 *
 * Grammar (simplified):
 *
 *   program      → statement* EOF
 *   statement    → forStmt | whileStmt | ifStmt | assignStmt | callStmt
 *   forStmt      → FOR IDENT FROM expr TO expr NEWLINE block
 *   whileStmt    → WHILE expr NEWLINE block
 *   ifStmt       → IF LPAREN? expr RPAREN? NEWLINE block (ELSE NEWLINE block)?
 *   assignStmt   → IDENT ASSIGN expr NEWLINE
 *   callStmt     → IDENT LPAREN argList RPAREN NEWLINE
 *   block        → INDENT statement+ DEDENT
 *   expr         → comparison
 *   comparison   → addition ((EQ|NEQ|LT|GT|LTE|GTE) addition)*
 *   addition     → term ((PLUS|MINUS) term)*
 *   term         → primary ((STAR|SLASH) primary)*
 *   primary      → NUMBER | STRING | TRUE | FALSE | IDENT (LPAREN argList RPAREN)? | LPAREN expr RPAREN
 *   argList      → (expr (COMMA expr)*)?
 */
public class Parser {

    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private Token peek() { return tokens.get(pos); }

    private Token peek(int offset) {
        int idx = pos + offset;
        return idx < tokens.size() ? tokens.get(idx) : tokens.get(tokens.size() - 1);
    }

    private boolean check(TokenType type) { return peek().type == type; }

    private Token consume(TokenType type) {
        if (!check(type))
            throw new RuntimeException(
                    "Expected " + type + " but got " + peek() + " at position " + pos);
        return tokens.get(pos++);
    }

    private boolean match(TokenType type) {
        if (check(type)) { pos++; return true; }
        return false;
    }

    /** Skip any number of NEWLINEs */
    private void skipNewlines() {
        while (check(TokenType.NEWLINE)) pos++;
    }

    // ── entry point ──────────────────────────────────────────────────────────

    public ProgramNode parse() {
        List<Node> stmts = new ArrayList<>();
        skipNewlines();
        while (!check(TokenType.EOF)) {
            stmts.add(parseStatement());
            skipNewlines();
        }
        return new ProgramNode(stmts);
    }

    // ── statements ───────────────────────────────────────────────────────────

    private Node parseStatement() {
        TokenType t = peek().type;

        if (t == TokenType.FOR)   return parseFor();
        if (t == TokenType.WHILE) return parseWhile();
        if (t == TokenType.IF)    return parseIf();

        // assign or call: both start with IDENT
        if (t == TokenType.IDENT) {
            // lookahead: IDENT LPAREN → call statement
            if (peek(1).type == TokenType.LPAREN)
                return parseCallStatement();
            // otherwise assignment
            return parseAssign();
        }

        throw new RuntimeException("Unexpected token in statement: " + peek());
    }

    /** for IDENT from expr to expr NEWLINE block */
    private ForNode parseFor() {
        consume(TokenType.FOR);
        String var = consume(TokenType.IDENT).literal;
        consume(TokenType.FROM);
        Node start = parseExpr();
        consume(TokenType.TO);
        Node end = parseExpr();
        consume(TokenType.NEWLINE);
        List<Node> body = parseBlock();
        return new ForNode(var, start, end, body);
    }

    /** while expr NEWLINE block */
    private WhileNode parseWhile() {
        consume(TokenType.WHILE);
        Node cond = parseExpr();
        consume(TokenType.NEWLINE);
        List<Node> body = parseBlock();
        return new WhileNode(cond, body);
    }

    /** if (expr) NEWLINE block [else NEWLINE block] */
    private IfNode parseIf() {
        consume(TokenType.IF);
        match(TokenType.LPAREN);          // optional paren
        Node cond = parseExpr();
        match(TokenType.RPAREN);          // optional paren
        consume(TokenType.NEWLINE);
        List<Node> thenBody = parseBlock();

        List<Node> elseBody = new ArrayList<>();
        if (check(TokenType.ELSE)) {
            consume(TokenType.ELSE);
            // skip optional colon / ILLEGAL tokens (the sample uses "else:")
            while (check(TokenType.ILLEGAL)) pos++;
            match(TokenType.NEWLINE);
            elseBody = parseBlock();
        }
        return new IfNode(cond, thenBody, elseBody);
    }

    /** IDENT = expr NEWLINE */
    private AssignNode parseAssign() {
        String name = consume(TokenType.IDENT).literal;
        consume(TokenType.ASSIGN);
        Node value = parseExpr();
        consume(TokenType.NEWLINE);
        return new AssignNode(name, value);
    }

    /** IDENT ( argList ) NEWLINE */
    private CallStatementNode parseCallStatement() {
        CallNode call = parseCall();
        consume(TokenType.NEWLINE);
        return new CallStatementNode(call);
    }

    // ── block ────────────────────────────────────────────────────────────────

    /** INDENT statement+ DEDENT */
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

    // ── expressions ──────────────────────────────────────────────────────────

    private Node parseExpr() { return parseComparison(); }

    private Node parseComparison() {
        Node left = parseAddition();
        while (true) {
            String op = comparisonOp();
            if (op == null) break;
            Node right = parseAddition();
            left = new BinaryOpNode(op, left, right);
        }
        return left;
    }

    private String comparisonOp() {
        switch (peek().type) {
            case EQ:  pos++; return "==";
            case NEQ: pos++; return "!=";
            case LT:  pos++; return "<";
            case GT:  pos++; return ">";
            case LTE: pos++; return "<=";
            case GTE: pos++; return ">=";
            default:  return null;
        }
    }

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

    private Node parseTerm() {
        Node left = parsePrimary();
        while (check(TokenType.STAR) || check(TokenType.SLASH)) {
            String op = peek().type == TokenType.STAR ? "*" : "/";
            pos++;
            Node right = parsePrimary();
            left = new BinaryOpNode(op, left, right);
        }
        return left;
    }

    private Node parsePrimary() {
        Token t = peek();

        if (t.type == TokenType.NUMBER) { pos++; return new NumberNode(t.literal); }
        if (t.type == TokenType.STRING) { pos++; return new StringNode(t.literal); }
        if (t.type == TokenType.TRUE)   { pos++; return new BoolNode(true);  }
        if (t.type == TokenType.FALSE)  { pos++; return new BoolNode(false); }

        if (t.type == TokenType.IDENT) {
            // function call used as expression
            if (peek(1).type == TokenType.LPAREN)
                return parseCall();
            pos++;
            return new IdentNode(t.literal);
        }

        if (t.type == TokenType.LPAREN) {
            pos++;
            Node inner = parseExpr();
            consume(TokenType.RPAREN);
            return inner;
        }

        throw new RuntimeException("Unexpected token in expression: " + t);
    }

    /** IDENT ( argList ) */
    private CallNode parseCall() {
        String callee = consume(TokenType.IDENT).literal;
        consume(TokenType.LPAREN);
        List<Node> args = parseArgList();
        consume(TokenType.RPAREN);
        return new CallNode(callee, args);
    }

    private List<Node> parseArgList() {
        List<Node> args = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            args.add(parseExpr());
            while (match(TokenType.COMMA))
                args.add(parseExpr());
        }
        return args;
    }
}
