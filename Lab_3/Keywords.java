import java.util.Map;

public class Keywords {

    private static final Map<String, TokenType> keywords = Map.ofEntries(
            Map.entry("for",   TokenType.FOR),
            Map.entry("from",  TokenType.FROM),
            Map.entry("to",    TokenType.TO),
            Map.entry("times", TokenType.TIMES),
            Map.entry("while", TokenType.WHILE),
            Map.entry("if",    TokenType.IF),
            Map.entry("else",  TokenType.ELSE),
            Map.entry("stop",  TokenType.STOP),
            Map.entry("not",   TokenType.NOT),
            Map.entry("true",  TokenType.TRUE),
            Map.entry("false", TokenType.FALSE)
    );

    public static TokenType lookup(String literal) {
        return keywords.getOrDefault(literal, TokenType.IDENT);
    }
}
