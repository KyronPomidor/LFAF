public enum TokenType {

    // Special
    EOF, ILLEGAL,

    // Literals & Identifiers
    IDENT, NUMBER, STRING,

    // Keywords
    FOR, FROM, TO, TIMES,
    WHILE, IF, ELSE, STOP,
    NOT, TRUE, FALSE,

    // Arithmetic
    PLUS, MINUS, STAR, SLASH,

    // Comparison
    EQ, NEQ, LT, GT, LTE, GTE,

    // Assignment
    ASSIGN,

    // Delimiters
    LPAREN, RPAREN, COMMA, DOT,

    // Structure
    NEWLINE, INDENT, DEDENT
}

//IDENT        # any variable/function name
//NUMBER       # any integer
//STRING       # anything between " "
//
//PLUS         # +
//MINUS        # -
//STAR         # *
//SLASH        # /
//
//EQ           # ==
//NEQ          # !=
//LT           #
//GT           # >
//LTE          # <=
//GTE          # >=
//
//ASSIGN       # =
//
//EOF          # End of the file
//ILLEGAL      # anything unrecognized
//
//INDENT       # Code is indented by one tab
//DEDENT       # Code is dedented by one tab
//NEWLINE      # A newline...