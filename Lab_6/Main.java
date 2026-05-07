public class Main {
    public static void main(String[] args) {
        String src = """
                for i from 1 to 5
                \tlog("hello")
                -- What?
                while x > 0
                \tx = x - 1
                if(a == 10)
                \t print("a == 10 - true")
                else:
                \t print("a == 10 - false")
                done = true
                """;

        // Lab 3: Lexer
        Lexer lexer = new Lexer(src);
        var tokens = lexer.tokenize();

        System.out.println("=== TOKENS ===");
        for (Token t : tokens)
            System.out.println(t);

        // Lab 4: Parser into AST
        System.out.println("\n=== AST ===");
        Parser parser = new Parser(tokens);
        ProgramNode ast = parser.parse();
        System.out.println(ast.toTree(""));
    }
}