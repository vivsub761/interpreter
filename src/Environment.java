import java.util.HashMap;
import java.util.Map;

public class Environment {
    private Map<String, Object> variables;
    private Environment parentEnv;

    Environment(Environment parent) {
        this.variables = new HashMap<>();
        this.parentEnv = parent;
    }
    void setVariable(String varName, Object value) {
        this.variables.put(varName, value);
    }

    void reassign(String varName, Object value) {
        if (this.variables.containsKey(varName)) {
            setVariable(varName, value);
        } else if (this.parentEnv != null){
            this.parentEnv.reassign(varName, value);
        } else {
            Interpreter.error(1, "Invalid assignment");
        }
    }

    Object getVal(String varName) {
        if (this.variables.containsKey(varName)) {
            return this.variables.get(varName);
        }
        if (this.parentEnv != null) {
            return this.parentEnv.getVal(varName);
        }
        Interpreter.error(1, "Variable is undefined");
        return null;
    }
}
