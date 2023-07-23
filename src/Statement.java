import javax.swing.plaf.nimbus.State;
import java.util.List;

abstract class Statement {

    interface StatementVisitor<R> {
        R visitPrint(Print printStatement);
        R visitExpression(Expression statement);
        R visitIfStatement(ifStatement statement);
        R visitVariable(StatementVar variable);
        R visitEnvBlock(EnvBlock block);
        R visitWhileStatement(WhileStatement statement);
        R visitForStatement(ForStatement statement);
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

    static class EnvBlock extends Statement {
        List<Statement> statements;

        EnvBlock(List<Statement> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Statement.StatementVisitor<R> visitor) {
            return visitor.visitEnvBlock(this);
        }
    }

    static class ifStatement extends Statement {
        Expr condition;
        Statement ifCondTrue;
        Statement ifCondFalse;
        ifStatement(Expr condition, Statement ifCondTrue, Statement ifCondFalse) {
            this.condition = condition;
            this.ifCondFalse = ifCondFalse;
            this.ifCondTrue = ifCondTrue;
        }

        @Override
        <R> R accept(Statement.StatementVisitor<R> visitor) {
            return visitor.visitIfStatement(this);
        }
    }

    static class WhileStatement extends Statement {
        Expr condition;
        Statement whileBlock;

        WhileStatement(Expr condition, Statement whileCode) {
            this.condition = condition;
            this.whileBlock = whileCode;
        }

        @Override
        <R> R accept(Statement.StatementVisitor<R> visitor) {
            return visitor.visitWhileStatement(this);
        }
    }

    static class ForStatement extends Statement {
        Expr condition;
        Statement forBlock;
        Statement varDeclaration;
        Expr incrementation;

        ForStatement(Expr condition, Statement forBlock, Statement varDeclaration, Expr incrementation) {
            this.condition = condition;
            this.forBlock = forBlock;
            this.varDeclaration = varDeclaration;
            this.incrementation = incrementation;
        }

        @Override
        <R> R accept(Statement.StatementVisitor<R> visitor) {
            return visitor.visitForStatement(this);
        }
    }
}
