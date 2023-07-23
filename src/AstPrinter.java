class AstPrinter implements Expr.ExprVisitor<String>{
    String print(Expr expr) {
        return expr.accept(this);
    }
    @Override
    public String visitBinary(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme,
                expr.left, expr.right);
    }
    @Override
    public String visitVariable(Expr.Variable expr) {
        return expr.varName.lexeme;
    }
    @Override
    public String visitGrouping(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }
    @Override
    public String visitLiteral(Expr.Literal expr) {
        if (expr.value == null) return "null";
        return expr.value.toString();
    }
    @Override
    public String visitUnary(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }
    @Override
    public String visitAssignment(Expr.Assignment expr) {
        return parenthesize(expr.variable.lexeme, expr.value);
    }
    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
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