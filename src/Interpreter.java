import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Interpreter {

    public static void main(String[] args) throws IOException{
        if (args.length == 1) {
            run(args[0]);
        } else {
            System.out.println("Invalid number of arguments");
            System.exit(64);
        }
    }
    private static void run(String filePath) throws IOException {
        byte[] fileData = Files.readAllBytes(Paths.get(filePath));
        Lexer lexer = new Lexer(new String(fileData, Charset.defaultCharset()));
        List<Token> tokens = lexer.getTokens();
        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();
        Evaluator evaluator = new Evaluator();
        evaluator.evaluate(statements);
    }
    public static void error(int lineNumber, String errorMessage) {
        System.out.println("Error in line " + Integer.toString(lineNumber) + ": " + errorMessage);
        System.exit(64);

    }
}
