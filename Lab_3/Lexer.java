import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Lexer {

    // Token spec: order matters - longer/more specific patterns first
    private static final List<TokenSpec> TOKEN_SPECS = List.of(
            new TokenSpec(TokenType.STRING,  "\"[^\"]*\""),
            new TokenSpec(TokenType.NUMBER,  "\\d+"),
            new TokenSpec(null,              "--[^\n]*"),          // comment -> skip
            new TokenSpec(TokenType.NEQ,     "!="),
            new TokenSpec(TokenType.EQ,      "=="),
            new TokenSpec(TokenType.LTE,     "<="),
            new TokenSpec(TokenType.GTE,     ">="),
            new TokenSpec(TokenType.LT,      "<"),
            new TokenSpec(TokenType.GT,      ">"),
            new TokenSpec(TokenType.ASSIGN,  "="),
            new TokenSpec(TokenType.PLUS,    "\\+"),
            new TokenSpec(TokenType.MINUS,   "-"),
            new TokenSpec(TokenType.STAR,    "\\*"),
            new TokenSpec(TokenType.SLASH,   "/"),
            new TokenSpec(TokenType.LPAREN,  "\\("),
            new TokenSpec(TokenType.RPAREN,  "\\)"),
            new TokenSpec(TokenType.COMMA,   ","),
            new TokenSpec(TokenType.DOT,     "\\."),
            new TokenSpec(TokenType.IDENT,   "[A-Za-z][A-Za-z0-9_]*")
    );

    private static final Pattern MASTER = buildMaster();

    private static Pattern buildMaster() {
        List<String> parts = new ArrayList<>();
        for (TokenSpec spec : TOKEN_SPECS)
            parts.add("(" + spec.pattern + ")");
        return Pattern.compile(String.join("|", parts));
    }

    // -----------------------------------------------------------------------

    private final String[] lines;

    public Lexer(String source) {
        // Normalise line endings, keep blank lines so indent tracking works
        this.lines = source.split("\n");
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        Deque<Integer> indentStack = new ArrayDeque<>();
        indentStack.push(0);

        for (String rawLine : lines) {
            // ---- measure leading spaces / tabs (1 tab = 1 indent level) ----
            int indent = 0;
            int i = 0;
            while (i < rawLine.length() && (rawLine.charAt(i) == ' ' || rawLine.charAt(i) == '\t')) {
                indent++;
                i++;
            }
            String content = rawLine.substring(i);

            // blank / whitespace-only line -> skip (no structural meaning)
            if (content.isEmpty()) continue;

            // emit INDENT / DEDENT
            int current = indentStack.peek();
            if (indent > current) {
                indentStack.push(indent);
                tokens.add(new Token(TokenType.INDENT));
            } else {
                while (indent < indentStack.peek()) {
                    indentStack.pop();
                    tokens.add(new Token(TokenType.DEDENT));
                }
            }

            // lex the content of the line
            Matcher m = MASTER.matcher(content);
            int pos = 0;
            while (pos < content.length()) {
                // skip inline whitespace
                if (content.charAt(pos) == ' ' || content.charAt(pos) == '\t') {
                    pos++;
                    continue;
                }

                m.region(pos, content.length());
                if (m.lookingAt()) {
                    String matched = m.group();
                    TokenSpec spec = specForMatch(m);

                    if (spec == null) {
                        // if the result is unexpected
                        break;
                    }

                    if (spec.type == null) {
                        // comment -> swallow rest of line
                        break;
                    }

                    TokenType type = spec.type;

                    // keyword check for identifiers
                    if (type == TokenType.IDENT) {
                        type = Keywords.lookup(matched);
                        tokens.add(type == TokenType.IDENT
                                ? new Token(TokenType.IDENT, matched)
                                : new Token(type));
                    } else if (type == TokenType.STRING) {
                        // strip surrounding quotes
                        tokens.add(new Token(TokenType.STRING, matched.substring(1, matched.length() - 1)));
                    } else if (type == TokenType.NUMBER) {
                        tokens.add(new Token(TokenType.NUMBER, matched));
                    } else {
                        tokens.add(new Token(type));
                    }

                    pos = m.end();
                } else {

                    tokens.add(new Token(TokenType.ILLEGAL, String.valueOf(content.charAt(pos))));
                    pos++;
                }
            }

            tokens.add(new Token(TokenType.NEWLINE));
        }

        while (indentStack.size() > 1) {
            indentStack.pop();
            tokens.add(new Token(TokenType.DEDENT));
        }

        tokens.add(new Token(TokenType.EOF));
        return tokens;
    }

    // Find which capturing group matched and return its TokenSpec
    private TokenSpec specForMatch(Matcher m) {
        for (int g = 1; g <= TOKEN_SPECS.size(); g++) {
            if (m.group(g) != null)
                return TOKEN_SPECS.get(g - 1);
        }
        return null;
    }

    // -----------------------------------------------------------------------

    private static class TokenSpec {
        final TokenType type;
        final String pattern;

        TokenSpec(TokenType type, String pattern) {
            this.type = type;
            this.pattern = pattern;
        }
    }
}