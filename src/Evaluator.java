public class Evaluator {
    public Object visitLiteral(Expr.Literal expr) {
        return expr.value;
    }

    public Object visitUnary(Expr.Unary expr) {
        Object right = eval(expr.right);

        if (expr.operator.type == TokenType.MINUS) {
            return -(float) right;
        } else if (expr.operator.type == TokenType.EXCLAMATION) {
            if (right == null) {
                return true;
            }
            return (right instanceof Boolean) ? !(Boolean) right : false;
        }
        return null;
    }

    public Object visitBinary(Expr.Binary expr) {
        Object left = eval(expr.left);
        Object right = eval(expr.right);

        switch (expr.operator.type) {
            case DOUBLE_EQUAL:
                return left.equals(right);
            case EXCLAMATIONEQUALS:
                return !left.equals(right);

        }
        if (expr.operator.type == TokenType.PLUS) {
            if (validateStrings(left, right)) {
                return (String) left + (String) right;
            } else if (validate(left, right)) {
                return (float) left + (float) right;
            } else {
                Interpreter.error(1, "Operand mismatch");
            }

        }
        if (validateStrings(left, right)) {
            Interpreter.error(1, "Invalid operator on two strings");
        }
        switch (expr.operator.type) {
            case MINUS:
                return (float) left - (float) right;
            case SLASH:
                return (float) left / (float) right;
            case STAR:
                return (float) left * (float) right;
            case GTE:
                return (float) left >= (float) right;
            case GT:
                return (float) left > (float) right;
            case LT:
                return (float) left < (float) right;
            case LTE:
                return (float) left <= (float) right;
        }
        return null;
    }

    public Object visitGrouping(Expr.Grouping expr) {
        return eval(expr.expression);
    }

    private Object eval(Expr expr) {
        if (expr instanceof Expr.Unary) {
            return visitUnary((Expr.Unary) expr);
        } else if (expr instanceof Expr.Binary) {
            return visitBinary((Expr.Binary) expr);
        } else if (expr instanceof Expr.Literal) {
            return visitLiteral((Expr.Literal) expr);
        } else if (expr instanceof Expr.Grouping) {
            return visitGrouping((Expr.Grouping) expr);
        }
        return "";
    }

    void evaluate(Expr expr) {
        Object value = eval(expr);
        System.out.println(value.toString());
    }

    private boolean validate(Object left, Object right) {
        return ((left instanceof Float) && (right instanceof Float));
    }

    private boolean validateStrings(Object left, Object right) {
        return ((left instanceof String) && (right instanceof String));
    }
}

