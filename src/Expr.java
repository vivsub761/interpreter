import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

abstract class Expr {
    interface ExprVisitor<R> {
        R visitBinary(Binary binary);
        R visitUnary(Unary unary);
        R visitGrouping(Grouping grouping);
        R visitLiteral(Literal literal);
        R visitVariable(Variable variable);
        R visitAssignment(Assignment assignment);
        R visitLogical(Logical logical);
        R visitCall(Call call);
        R visitArray(Array array);
    }
    abstract <R> R accept(ExprVisitor<R> visitor);
    static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;
        Binary (Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitBinary(this);
        }
    }

    static class Unary extends Expr {
        final Expr right;
        final Token operator;

        Unary(Expr right, Token operator) {
            this.right = right;
            this.operator = operator;
        }
        @Override
        <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitUnary(this);
        }
    }

    static class Literal extends  Expr {
        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitLiteral(this);
        }
    }

    static class Grouping extends Expr {
        final Expr expression;

        Grouping(Expr expression) {
            this.expression = expression;
        }
        @Override
        <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitGrouping(this);
        }
    }

    static class Variable extends Expr {
        final Token varName;
        Variable(Token name) {
            this.varName = name;
        }
        @Override
        <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitVariable(this);
        }
    }

    static class Assignment extends Expr {
        final Token variable;
        final Expr value;

        Assignment(Token variable, Expr value) {
            this.variable = variable;
            this.value = value;
        }
        @Override
        <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitAssignment(this);
        }
    }

    static class Logical extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;
        Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitLogical(this);
        }
    }

    static class Call extends Expr {
        final Expr functionToCall;
        final int lineNumber;
        final List<Expr> args;

        Call(Expr functionToCall, List<Expr> args, int lineNumber) {
            this.lineNumber = lineNumber;
            this.functionToCall = functionToCall;
            this.args = args;
        }

        @Override
        <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitCall(this);
        }
    }

    static class Array extends Expr {
        final ArrayList<Object> arrayContents;

        Array(ArrayList<Object> arrayContents) {
            this.arrayContents = arrayContents;
        }
        @Override
        <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitArray(this);
        }
    }

}
