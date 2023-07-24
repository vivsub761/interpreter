import java.util.List;

interface Callable {
    Object call(Evaluator evaluator, List<Object> args);
}