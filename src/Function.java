import java.util.List;
import java.util.Map;

public class Function implements Callable{
    private final Statement.functionDef function;

    Function(Statement.functionDef function) {
        this.function = function;
    }
    @Override
    public Object call(Evaluator evaluator, List<Object> args) {
        Environment environment = new Environment(evaluator.globalEnv);
        if (args.size() != this.function.argIndexToDefault.size()) {
            Interpreter.error(1, "Incorrect amount of arguments");
        }
        for (int i = 0; i < args.size(); i++) {
            String key = this.keyFromVal(i);
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

    private String keyFromVal(int index) {
        for (String key : this.function.nameToArgIndex.keySet()) {
            if (this.function.nameToArgIndex.get(key) == index) {
                return key;
            }
        }
        Interpreter.error(1, "Issue with function arguments");
        return "";
    }
}
