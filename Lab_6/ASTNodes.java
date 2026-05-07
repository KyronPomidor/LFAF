import java.util.List;

// ─────────────────────────────────────────────
//  Statement nodes
// ─────────────────────────────────────────────

/** Top-level program: a sequence of statements */
class ProgramNode implements Node {
    public final List<Node> statements;

    public ProgramNode(List<Node> statements) {
        this.statements = statements;
    }

    @Override
    public String toTree(String indent) {
        StringBuilder sb = new StringBuilder(indent + "Program\n");
        for (Node s : statements)
            sb.append(s.toTree(indent + "  ")).append("\n");
        return sb.toString().stripTrailing();
    }
}

/** for <var> from <start> to <end> <body> */
class ForNode implements Node {
    public final String variable;
    public final Node start;
    public final Node end;
    public final List<Node> body;

    public ForNode(String variable, Node start, Node end, List<Node> body) {
        this.variable = variable;
        this.start    = start;
        this.end      = end;
        this.body     = body;
    }

    @Override
    public String toTree(String indent) {
        StringBuilder sb = new StringBuilder(indent + "For(" + variable + ")\n");
        sb.append(indent + "  from: ").append(start.toTree("")).append("\n");
        sb.append(indent + "  to:   ").append(end.toTree("")).append("\n");
        sb.append(indent + "  body:\n");
        for (Node s : body)
            sb.append(s.toTree(indent + "    ")).append("\n");
        return sb.toString().stripTrailing();
    }
}

/** while <condition> <body> */
class WhileNode implements Node {
    public final Node condition;
    public final List<Node> body;

    public WhileNode(Node condition, List<Node> body) {
        this.condition = condition;
        this.body      = body;
    }

    @Override
    public String toTree(String indent) {
        StringBuilder sb = new StringBuilder(indent + "While\n");
        sb.append(indent + "  cond: ").append(condition.toTree("")).append("\n");
        sb.append(indent + "  body:\n");
        for (Node s : body)
            sb.append(s.toTree(indent + "    ")).append("\n");
        return sb.toString().stripTrailing();
    }
}

/** if <condition> <then> [else <else>] */
class IfNode implements Node {
    public final Node condition;
    public final List<Node> thenBody;
    public final List<Node> elseBody; // may be empty

    public IfNode(Node condition, List<Node> thenBody, List<Node> elseBody) {
        this.condition = condition;
        this.thenBody  = thenBody;
        this.elseBody  = elseBody;
    }

    @Override
    public String toTree(String indent) {
        StringBuilder sb = new StringBuilder(indent + "If\n");
        sb.append(indent + "  cond: ").append(condition.toTree("")).append("\n");
        sb.append(indent + "  then:\n");
        for (Node s : thenBody)
            sb.append(s.toTree(indent + "    ")).append("\n");
        if (!elseBody.isEmpty()) {
            sb.append(indent + "  else:\n");
            for (Node s : elseBody)
                sb.append(s.toTree(indent + "    ")).append("\n");
        }
        return sb.toString().stripTrailing();
    }
}

/** <name> = <value> */
class AssignNode implements Node {
    public final String name;
    public final Node value;

    public AssignNode(String name, Node value) {
        this.name  = name;
        this.value = value;
    }

    @Override
    public String toTree(String indent) {
        return indent + "Assign(" + name + ") = " + value.toTree("");
    }
}

/** <callee>(<args...>) used as a statement */
class CallStatementNode implements Node {
    public final CallNode call;

    public CallStatementNode(CallNode call) { this.call = call; }

    @Override
    public String toTree(String indent) {
        return call.toTree(indent);
    }
}

// ─────────────────────────────────────────────
//  Expression nodes
// ─────────────────────────────────────────────

/** Binary operation: left <op> right */
class BinaryOpNode implements Node {
    public final String op;
    public final Node left;
    public final Node right;

    public BinaryOpNode(String op, Node left, Node right) {
        this.op    = op;
        this.left  = left;
        this.right = right;
    }

    @Override
    public String toTree(String indent) {
        return indent + "BinOp(" + op + ", " + left.toTree("") + ", " + right.toTree("") + ")";
    }
}

/** Function / built-in call expression */
class CallNode implements Node {
    public final String callee;
    public final List<Node> args;

    public CallNode(String callee, List<Node> args) {
        this.callee = callee;
        this.args   = args;
    }

    @Override
    public String toTree(String indent) {
        StringBuilder sb = new StringBuilder(indent + "Call(" + callee + ")");
        if (!args.isEmpty()) {
            sb.append(" args=[");
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(args.get(i).toTree(""));
            }
            sb.append("]");
        }
        return sb.toString();
    }
}

/** Identifier reference */
class IdentNode implements Node {
    public final String name;

    public IdentNode(String name) { this.name = name; }

    @Override
    public String toTree(String indent) { return indent + "Ident(" + name + ")"; }
}

/** Numeric literal */
class NumberNode implements Node {
    public final String value;

    public NumberNode(String value) { this.value = value; }

    @Override
    public String toTree(String indent) { return indent + "Number(" + value + ")"; }
}

/** String literal */
class StringNode implements Node {
    public final String value;

    public StringNode(String value) { this.value = value; }

    @Override
    public String toTree(String indent) { return indent + "String(\"" + value + "\")"; }
}

/** Boolean literal */
class BoolNode implements Node {
    public final boolean value;

    public BoolNode(boolean value) { this.value = value; }

    @Override
    public String toTree(String indent) { return indent + "Bool(" + value + ")"; }
}
