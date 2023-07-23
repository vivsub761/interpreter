abstract class Statement {

    interface StatementVisitor<R> {
        R visitPrint(Print printStatement);
        R visitExpression(Expression statement);

        R visitVariable(StatementVar variable);
    }
    abstract <R> R accept(Statement.StatementVisitor<R> visitor);
    static class Expression extends Statement {
        final Expr expr;
        Expression(Expr expr) {
            this.expr = expr;
        }
        @Override
        <R> R accept(Statement.StatementVisitor<R> visitor) {
            return visitor.visitExpression(this);
        }
    }

    static class Print extends Statement {
        final Expr expr;
        Print(Expr expr) {
            this.expr = expr;
        }
        @Override
        <R> R accept(Statement.StatementVisitor<R> visitor) {
            return visitor.visitPrint(this);
        }
    }

    static class StatementVar extends Statement {
        final Token varName;
        final Expr initialVarValue;

        StatementVar(Token varName, Expr initialVarValue) {
            this.varName = varName;
            this.initialVarValue = initialVarValue;
        }

        @Override
        <R> R accept(Statement.StatementVisitor<R> visitor) {
            return visitor.visitVariable(this);
        }
    }
}
