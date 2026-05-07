public class Token {
    public final TokenType type;
    public final String literal;

    public Token(TokenType type, String literal) {
        this.type = type;
        this.literal = literal;
    }

    public Token(TokenType type) {
        this.type = type;
        this.literal = null;
    }

    @Override
    public String toString() {
        if (literal != null)
            return "Token(" + type + ", " + literal + ")";
        return "Token(" + type + ")";
    }
}