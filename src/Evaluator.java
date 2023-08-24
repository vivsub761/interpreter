import java.util.ArrayList;
import java.util.List;

public class Evaluator implements Statement.StatementVisitor<Void>, Expr.ExprVisitor<Object>{

    final Environment globalEnv = new Environment(null);
    private Environment environment = globalEnv;
    @Override
    public Object visitLiteral(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitArray(Expr.Array array) {
        for (int i = 0; i < array.arrayContents.size(); i++) {
            array.arrayContents.set(i, eval((Expr) array.arrayContents.get(i)));
        }
        return array.arrayContents;
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
    @Override
    public Object visitLogical(Expr.Logical logical) {
        Object left = eval(logical.left);
        Boolean leftTruth = isStatementTrue(left);
        if (logical.operator.type == TokenType.AND) {
            return leftTruth ? eval(logical.right) : left;
        } else {
            return leftTruth ? left : eval(logical.right);
        }
    }
    @Override
    public Object visitCall(Expr.Call call) {
        Object functionToCall = eval(call.functionToCall);

        List<Object> args = new ArrayList<>();
        for (Expr arg : call.args) {
            args.add(eval(arg));
        }
        if (!(functionToCall instanceof Callable)) {
            Interpreter.error(call.lineNumber, "Cannot call this object");
        }
        Callable function = (Callable) functionToCall;
        return function.call(this, args);
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
        executeBlock(block.statements, blockEnv);
        return null;
    }
    @Override
    public Void visitIfStatement(Statement.ifStatement statement) {
        Object condResult = eval(statement.condition);
        if (isStatementTrue(condResult)) {
            execute(statement.ifCondTrue);
        } else if (statement.ifCondFalse != null){
            execute(statement.ifCondFalse);
        }
        return null;
    }
    @Override
    public Void visitWhileStatement(Statement.WhileStatement statement) {
        while (isStatementTrue(eval(statement.condition))) {
            execute(statement.whileBlock);
        }
        return null;
    }

    @Override
    public Void visitForStatement(Statement.ForStatement statement) {
        Statement.StatementVar var = (Statement.StatementVar) statement.varDeclaration;
        this.environment.setVariable(var.varName.lexeme, ((Expr.Literal) var.initialVarValue).value);
        while (isStatementTrue(eval(statement.condition))) {
            execute(statement.forBlock);
            eval(statement.incrementation);
        }
        return null;
    }
    @Override
    public Void visitFunction(Statement.functionDef function) {
        Function func = new Function(function);
        environment.setVariable(function.name.lexeme, func);
        return null;
    }

    @Override
    public Void visitReturn(Statement.Return returnStatement) {
        Object value = eval(returnStatement.returnVal);
        throw new Return(value);
    }

    private Object eval(Expr expr) {
        if (expr == null) {
            return null;
        }
        return expr.accept(this);
    }
    private void execute(Statement statement) {
        statement.accept(this);
    }
    void evaluate(List<Statement> statements) {
        for (Statement statement: statements) {
            execute(statement);
        }
    }

    void executeBlock(List<Statement> block, Environment environment) {
        Environment prev = this.environment;
        this.environment = environment;
        for (Statement statement : block) {
            execute(statement);
        }
        this.environment = prev;
    }

    private boolean validate(Object left, Object right) {
        return ((left instanceof Float) && (right instanceof Float));
    }


    private boolean validateStrings(Object left, Object right) {
        return ((left instanceof String) && (right instanceof String));
    }

    private Boolean isStatementTrue(Object res) {
        if (res == null) {
            return false;
        } else if (res instanceof Boolean) {
            return (Boolean) res;
        } else {
            return true;
        }
    }
}

