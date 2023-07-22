class AstPrinter {
    String print(Expr expr) {
        if (expr instanceof Expr.Unary) {
            return visitUnary((Expr.Unary) expr);
        } else if (expr instanceof  Expr.Binary) {
            return visitBinary((Expr.Binary) expr);
        } else if (expr instanceof Expr.Literal) {
            return visitLiteral((Expr.Literal) expr);
        } else if (expr instanceof Expr.Grouping) {
            return visitGrouping((Expr.Grouping) expr);
        }
        return "";
    }
    public String visitBinary(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme,
                expr.left, expr.right);
    }

    public String visitGrouping(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    public String visitLiteral(Expr.Literal expr) {
        if (expr.value == null) return "null";
        return expr.value.toString();
    }

    public String visitUnary(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }
    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            if (expr instanceof Expr.Unary) {
                builder.append(visitUnary((Expr.Unary) expr));
            } else if (expr instanceof  Expr.Binary) {
                builder.append(visitBinary((Expr.Binary) expr));
            } else if (expr instanceof Expr.Literal) {
                builder.append(visitLiteral((Expr.Literal) expr));
            } else if (expr instanceof Expr.Grouping) {
                builder.append(visitGrouping((Expr.Grouping) expr));
            }
//            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Expr.Literal(423984),
                        new Token(TokenType.MINUS, "-", null)),
                new Token(TokenType.SLASH, "/", null),
                new Expr.Grouping(
                        new Expr.Literal(3234.42587)));

        System.out.println(new AstPrinter().print(expression));
    }
}