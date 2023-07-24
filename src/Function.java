import java.util.List;

public class Function implements Callable{
    private final Statement.functionDef function;

    Function(Statement.functionDef function) {
        this.function = function;
    }
    @Override
    public Object call(Evaluator evaluator, List<Object> args) {
        Environment environment = new Environment(evaluator.globalEnv);
        if (args.size() != this.function.args.size()) {
            Interpreter.error(1, "Incorrect amount of arguments");
        }
        for (int i = 0; i < args.size(); i++) {
            String key = this.function.args.get(i).lexeme;
            Object val = args.get(i);
            environment.setVariable(key, val);
        }
        try {
            evaluator.executeBlock(function.funcBody, environment);
        } catch (Return returnVal) {
            return returnVal.returnVal;
        }
        return null;
    }
}
