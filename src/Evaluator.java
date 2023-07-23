import java.util.List;

public class Evaluator implements Statement.StatementVisitor<Void>, Expr.ExprVisitor<Object>{

    private Environment environment;
    Evaluator() {
        this.environment = new Environment(null);
    }
    @Override
    public Object visitLiteral(Expr.Literal expr) {
        return expr.value;
    }
    @Override
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
    @Override
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
    @Override
    public Object visitGrouping(Expr.Grouping expr) {
        return eval(expr.expression);
    }

    @Override
    public Object visitAssignment(Expr.Assignment assignment) {
        Object value = eval(assignment.value);
        environment.reassign(assignment.variable.lexeme, value);
        return value;
    }
    @Override
    public Object visitVariable(Expr.Variable var) {
        return this.environment.getVal(var.varName.lexeme);
    }

//    Statements
    @Override
    public Void visitExpression(Statement.Expression statement) {
        eval(statement.expr);
        return null;
    }
    @Override
    public Void visitPrint(Statement.Print printStatement) {
        Object value = eval(printStatement.expr);
        System.out.println(value.toString());
        return null;
    }
    @Override
    public Void visitVariable(Statement.StatementVar var) {
        Object value = eval(var.initialVarValue);
        this.environment.setVariable(var.varName.lexeme, value);
        return null;
    }
    @Override
    public Void visitEnvBlock(Statement.EnvBlock block) {
        Environment blockEnv = new Environment(this.environment);
        Environment prev = this.environment;
        this.environment = blockEnv;
        for (Statement statement : block.statements) {
            execute(statement);
        }
        this.environment = prev;
        return null;
    }




    private Object eval(Expr expr) {
        return expr.accept(this);
    }
    private void execute(Statement statement) {
        statement.accept(this);
    }
    void evaluate(List<Statement> statements) {
        for (Statement statement: statements) {
            execute(statement);
        }
//        System.out.println(value.toString());
    }

    private boolean validate(Object left, Object right) {
        return ((left instanceof Float) && (right instanceof Float));
    }


    private boolean validateStrings(Object left, Object right) {
        return ((left instanceof String) && (right instanceof String));
    }
}

