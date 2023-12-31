import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int currToken;

//    This hashmap has the following format Map<key = Token funcName, val = Statement.functionDef>
//    When the parser finds a function, it will store them here. When the parser finds a function call to the functions,
//    it will use this hashmap to resolve default arguments and send the full argument list to the evaluator
    HashMap<String, Statement.functionDef> functions = new HashMap<>();
    List<Pair> addToBlock = new ArrayList<>();
    List<Pair> addToStatements = new ArrayList<>();
    List<Statement> statements = new ArrayList<>();
    Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currToken = 0;
    }
    List<Statement> parse() {

        while (this.currToken < this.tokens.size()) {
            this.statements.add(getNextStatement(null));
            for (Pair pair : addToStatements) {
                this.statements.add((Integer) pair.getFirst(), (Statement.Expression)pair.getSecond());
            }
            this.addToStatements.clear();
        }
        return this.statements;
    }
    Statement getNextStatement(List<Statement> block) {
        Token curr = getCurrToken();
        this.currToken++;
        if (curr.type == TokenType.PRINT) {
            checkType(TokenType.LEFT_P, "Missing '(' after print");
            Expr expr = expression(block);
            checkType(TokenType.RIGHT_P, "Missing ')' after print");
            semicolonCheck();
            return new Statement.Print(expr);
        } else if (curr.type == TokenType.VAR){
            Token next = getCurrToken();
            if (next.type != TokenType.IDENTIFIER) {
                Interpreter.error(next.lineNumber, "Expect variable name after var keyword");
            }
            Token name = getCurrToken();
            this.currToken++;
            next = getCurrToken();
            if (next.type != TokenType.EQUAL) {
                Interpreter.error(next.lineNumber,"Initial value needed");
            }
            this.currToken++;
            Expr initialVal = expression(block);
            semicolonCheck();
            return new Statement.StatementVar(name, initialVal);
        } else if (curr.type == TokenType.LEFT_B){
            return new Statement.EnvBlock(block());
        } else if (curr.type == TokenType.IF) {
            checkType(TokenType.LEFT_P, "Missing '(' after if");
            Expr condition = expression(block);
            checkType(TokenType.RIGHT_P, "Missing ')' after condition");
            Statement ifCondTrue = getNextStatement(null);
            if (this.tokens.get(this.currToken).type == TokenType.ELSE) {
                this.currToken++;
                Statement ifCondFalse = getNextStatement(null);
                return new Statement.ifStatement(condition, ifCondTrue, ifCondFalse);
            }
            return new Statement.ifStatement(condition, ifCondTrue, null);

        } else if (curr.type == TokenType.WHILE) {
            checkType(TokenType.LEFT_P, "Missing ( after 'while'");
            Expr condition = expression(block);
            checkType(TokenType.RIGHT_P, "Missing ')' after while loop condition");
            Statement whileCode = getNextStatement(null);
            return new Statement.WhileStatement(condition, whileCode);

        } else if (curr.type == TokenType.FOR) {
            checkType(TokenType.LEFT_P, "Missing '(' after for loop declaration");
            // this is a var declaration
            Statement initialize = getNextStatement(null);
            Expr condition = expression(block);
            semicolonCheck();

            Expr incrementation = expression(block);
            checkType(TokenType.RIGHT_P, "Missing ')' after specifying for loop incrementation");
            Statement forBlock = getNextStatement(null);
            return new Statement.ForStatement(condition, forBlock, initialize, incrementation);
        } else if (curr.type == TokenType.DEF) {
            checkType(TokenType.IDENTIFIER, "Please add a function name after 'def' keyword");
            Token name = this.tokens.get(this.currToken - 1);
            checkType(TokenType.LEFT_P, "Missing '(' after function name");
            HashMap<String, Integer> nameToArgIndex = new HashMap<>();
            HashMap<Integer, Object> argIndexToDefault = new HashMap<>();
            int argNum = 0;
            if (getCurrToken().type != TokenType.RIGHT_P) {
                checkType(TokenType.IDENTIFIER, "Missing argument identifier");
                Token variable = this.tokens.get(this.currToken - 1);
                Expr defaultArg =  null;
                if (getCurrToken().type == TokenType.EQUAL) {
                    this.currToken++;
                    if (!this.getCurrToken().isLiteral()) {
                        Interpreter.error(this.getCurrToken().lineNumber, "Default argument for function must be a literal");
                    }
                    if (!this.getCurrToken().isLiteral()) {
                        Interpreter.error(this.getCurrToken().lineNumber, "Default argument for function must be a literal");
                    }
                    defaultArg = primary(block);
                }
                nameToArgIndex.put(variable.lexeme, argNum);
                argIndexToDefault.put(argNum++, defaultArg);
                while (getCurrToken().type == TokenType.COMMA) {
                    this.currToken++;
                    checkType(TokenType.IDENTIFIER, "Missing argument identifier");
                    Token var2 = this.tokens.get(this.currToken - 1);
                    Expr defaultArg2 =  null;
                    if (getCurrToken().type == TokenType.EQUAL) {
                        this.currToken++;
                        if (!this.getCurrToken().isLiteral()) {
                            Interpreter.error(this.getCurrToken().lineNumber, "Default argument for function must be a literal");
                        }
                        defaultArg2 = primary(block);
                    }
                    nameToArgIndex.put(var2.lexeme, argNum);
                    argIndexToDefault.put(argNum++, defaultArg2);
                }
            }
            checkType(TokenType.RIGHT_P, "Missing ')' after listing arguments");
            checkType(TokenType.LEFT_B, "Missing '{' after function is declared");
            List<Statement> funcBody = block();
            Statement funcDef = new Statement.functionDef(name, nameToArgIndex, argIndexToDefault, funcBody);
            this.functions.put(name.lexeme, (Statement.functionDef) funcDef);
            return funcDef;
        } else if (curr.type == TokenType.RETURN) {
            int lineNum = curr.lineNumber;
            Expr returnVal = null;
            if (getCurrToken().type != TokenType.SEMICOLON) {
                returnVal = expression(block);
            }
            semicolonCheck();
            return new Statement.Return(lineNum, returnVal);
        } else if (curr.type == TokenType.BREAK) {
            semicolonCheck();
            return new Statement.Break();
        } else if (curr.type == TokenType.CONTINUE) {
            semicolonCheck();
            return new Statement.Continue();
        }else {
            this.currToken--;
            Expr expr = assignment(block);
            semicolonCheck();
            return new Statement.Expression(expr);
        }
    }

    private boolean checkType(TokenType type, String message) {
        if (getCurrToken().type != type) {
            Interpreter.error(getCurrToken().lineNumber, message);
        }
        this.currToken++;
        return true;
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();
        while (this.currToken < this.tokens.size() && getCurrToken().type != TokenType.RIGHT_B) {
            statements.add(getNextStatement(statements));
            for (Pair pair : this.addToBlock) {
                statements.add((Integer) pair.getFirst(), (Statement.Expression) pair.getSecond());
            }
            this.addToBlock.clear();
        }
        if (this.currToken >= this.tokens.size() || getCurrToken().type != TokenType.RIGHT_B) {
            Interpreter.error(getCurrToken().lineNumber, "Missing right bracket");
        }
        this.currToken++;
        return statements;
    }

    private void semicolonCheck() {
        if (getCurrToken().type != TokenType.SEMICOLON) {
            Interpreter.error(getCurrToken().lineNumber, "missing semicolon");
        }
        this.currToken++;
    }

    private Expr equality(List<Statement> block) {
        Expr left = comparison(block);
        if (this.currToken == this.tokens.size()) {
            return left;
        }
        Token curr = getCurrToken();
        while (curr.type == TokenType.DOUBLE_EQUAL || curr.type == TokenType.EXCLAMATIONEQUALS) {
            this.currToken++;
            Expr right = comparison(block);
            left = new Expr.Binary(left, curr, right);
            curr = getCurrToken();
        }
        return left;
    }


    private Expr comparison(List<Statement> block) {
        Expr left = term(block);
        if (this.currToken == this.tokens.size()) {
            return left;
        }
        Token curr = getCurrToken();
        while (curr.type == TokenType.GT || curr.type == TokenType.GTE
                || curr.type == TokenType.LT || curr.type == TokenType.LTE) {
            this.currToken++;
            Expr right = term(block);
            left = new Expr.Binary(left, curr, right);
            curr = getCurrToken();
        }
        return left;
    }

    private Expr term(List<Statement> block) {
        Expr left = factor(block);
        if (this.currToken == this.tokens.size()) {
            return left;
        }
        Token curr = getCurrToken();
        while (curr.type == TokenType.PLUS || curr.type == TokenType.MINUS) {
            this.currToken++;
            Expr right = factor(block);
            left = new Expr.Binary(left, curr, right);
            if (this.currToken >= this.tokens.size()) {
                break;
            }
            curr = getCurrToken();
        }
        return left;
    }

    private Expr factor(List<Statement> block) {
        Expr left = unary(block);
        if (this.currToken == this.tokens.size()) {
            return left;
        }
        Token curr = getCurrToken();
        while (curr.type == TokenType.SLASH || curr.type == TokenType.STAR) {
            this.currToken++;
            Expr right = unary(block);
            left = new Expr.Binary(left, curr, right);
            if (this.currToken >= this.tokens.size()) {
                break;
            }
            curr = getCurrToken();
        }
        return left;
    }
    private Expr unary(List<Statement> block) {
        Token curr = getCurrToken();
        if (curr.type == TokenType.EXCLAMATION || curr.type == TokenType.MINUS) {
            this.currToken++;
            return new Expr.Unary(unary(block), curr);
        }
        return call(block);
    }
    private int countArgs() {
        int rewind = this.currToken;
        if (getCurrToken().type == TokenType.RIGHT_P) {
            return 0;
        }
        this.currToken++;
        int commaCount = 0;
        while (getCurrToken().type != TokenType.RIGHT_P) {
            if (this.getCurrToken().type == TokenType.COMMA) {
                commaCount++;
            }
            this.currToken++;
        }
        this.currToken = rewind;
        return commaCount + 1;
    }
    private Expr call(List<Statement> block) {
        Expr left = primary(block);

        List<Expr> args = null;
        while (this.currToken < this.tokens.size() && getCurrToken().type == TokenType.LEFT_P) {
            this.currToken++;
            int numArgs = countArgs();

            Token funcName = ((Expr.Variable) left).varName;
            Statement.functionDef targetFunction = this.functions.get(funcName.lexeme);
            args = new ArrayList<>();
            if (numArgs > targetFunction.argIndexToDefault.size()) {
                Interpreter.error(funcName.lineNumber, "Too many arguments");
            } else {
                handleArgs(args, targetFunction, block);
            }

            int lineNum = getCurrToken().lineNumber;
            checkType(TokenType.RIGHT_P, ")");
            left = new Expr.Call(left, args, lineNum);
        }
        return left;

    }

    private void handleArgs(List<Expr> args, Statement.functionDef targetFunction, List<Statement> block) {
        Token curr = this.getCurrToken();
        int argNum = 0;
        HashMap<String, Integer> nameToIndex = targetFunction.nameToArgIndex;
        HashMap<Integer, Object> indexToDefault = targetFunction.argIndexToDefault;
        for (int i = 0; i < indexToDefault.size(); i++) {
            args.add(null);
        }
        do {
            Expr arg = expression(block);
            if (arg instanceof Expr.Assignment) {
                Token argName = ((Expr.Assignment) arg).variable;
                int argIndex = nameToIndex.get(argName.lexeme);
                args.set(argIndex, ((Expr.Assignment) arg).value);
            } else {
                args.set(argNum++, arg);
            }
        } while (this.tokens.get(this.currToken++).type == TokenType.COMMA);
        this.currToken--;
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i) == null) {
                args.set(i, (Expr) indexToDefault.get(i));
            }
        }

    }

    private Expr primary(List<Statement> block) {
        Token curr = getCurrToken();
        switch (curr.type) {
            case NULL: this.currToken++; return new Expr.Literal(null);
            case TRUE: this.currToken++; return new Expr.Literal(true);
            case FALSE: this.currToken++; return new Expr.Literal(false);
            default:
                if (curr.type == TokenType.NUM || curr.type == TokenType.STRING) {
                    this.currToken++;
                    return new Expr.Literal(curr.literal);
                } else if (curr.type == TokenType.LEFT_P) {
                    this.currToken++;
                    Expr expr = expression(block);
                    checkType(TokenType.RIGHT_P, "no matching right parenthesis");
                    return new Expr.Grouping(expr);
                } else if (curr.type == TokenType.IDENTIFIER) {
                    Expr var = new Expr.Variable(this.tokens.get(this.currToken++));
                    Token next = this.getCurrToken();
                    if (next.type == TokenType.DOUBLEPLUS || next.type == TokenType.DOUBLEMINUS) {
                        Expr assignTo = new Expr.Binary(new Expr.Variable(curr),
                                new Token(next.type == TokenType.DOUBLEMINUS ? TokenType.MINUS : TokenType.PLUS,
                                        next.type == TokenType.DOUBLEMINUS ? "-" : "+", null,
                                        this.getCurrToken().lineNumber), new Expr.Literal((float) 1));
                        Expr assignment = new Expr.Assignment(curr, assignTo);
                        Statement desugared = new Statement.Expression(assignment);
                        Pair thisPair;
                        if (block == null) {
                            thisPair = new Pair(this.statements.size() + 1, desugared);
                            this.addToStatements.add(thisPair);
                        } else {
                            thisPair = new Pair(block.size() + 1, desugared);
                            this.addToBlock.add(thisPair);
                        }
                        this.currToken++;
                    }
                    return var;
                } else if (curr.type == TokenType.DOUBLEMINUS || curr.type == TokenType.DOUBLEPLUS) {
                    Token variable = this.tokens.get(++this.currToken);
                    Expr assignTo = new Expr.Binary(new Expr.Variable(variable),
                            new Token(curr.type == TokenType.DOUBLEMINUS ? TokenType.MINUS : TokenType.PLUS,
                                    curr.type == TokenType.DOUBLEMINUS ? "-" : "+",
                                    null, this.getCurrToken().lineNumber), new Expr.Literal((float) 1));
                    Expr assignment = new Expr.Assignment(variable, assignTo);
                    Statement desugaredStatement = new Statement.Expression(assignment);
                    if (block == null) {
                        this.statements.add(desugaredStatement);
                    } else {
                        block.add(desugaredStatement);
                    }
                    return new Expr.Variable(this.tokens.get(this.currToken++));
                } else if (curr.type == TokenType.LEFT_S) {
                    this.currToken++;
                    ArrayList<Object> contents = new ArrayList<>();
                    do {
                        contents.add(assignment(block));
                    } while (this.tokens.get(this.currToken++).type == TokenType.COMMA);
                    this.currToken--;
                    checkType(TokenType.RIGHT_S, "Array never terminated");
                    return new Expr.Array(contents);
                }
        }
        return null;
    }
    Token getCurrToken() {
        return this.tokens.get(this.currToken);
    }

    private Expr expression(List<Statement> block) {
        return assignment(block);
    }
    private Expr assignment(List<Statement> block) {
        Expr left = or(block);
        TokenType currType= this.getCurrToken().type;
        if (currType == TokenType.PLUSEQUALS || currType == TokenType.MINUSEQUALS) {
            if (!(left instanceof Expr.Variable)) {
                Interpreter.error(this.getCurrToken().lineNumber, "Invalid Assignment");
            }
            this.currToken++;
            Token operator = new Token(currType == TokenType.PLUSEQUALS ? TokenType.PLUS : TokenType.MINUS, currType == TokenType.PLUSEQUALS ? "+" :"-", null, this.getCurrToken().lineNumber);
            Expr value = new Expr.Binary(left, operator, assignment(block));
            return new Expr.Assignment(((Expr.Variable) left).varName, value);
        } else if (currType == TokenType.EQUAL) {
            Token curr = getCurrToken();
            this.currToken++;
            Expr value = assignment(block);
            if (left instanceof Expr.Variable) {
                return new Expr.Assignment(((Expr.Variable) left).varName, value);
            } else {
                Interpreter.error(getCurrToken().lineNumber, "Invalid assignment");
            }
        }
        return left;
    }

    private Expr or(List<Statement> block) {
        Expr left = and(block);
        while (this.currToken < tokens.size() && getCurrToken().type == TokenType.OR) {
            Token operator = getCurrToken();
            this.currToken++;
            Expr right = equality(block);
            left = new Expr.Logical(left, operator, right);
        }
        return left;
    }

    private Expr and(List<Statement> block) {
        Expr left = equality(block);
        while (getCurrToken().type == TokenType.AND) {
            Token operator = getCurrToken();
            this.currToken++;
            Expr right = equality(block);
            left = new Expr.Logical(left, operator, right);
        }
        return left;
    }

}
