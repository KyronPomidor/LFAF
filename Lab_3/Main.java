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

        Lexer lexer = new Lexer(src);
        for (Token t : lexer.tokenize())
            System.out.println(t);
    }
}
