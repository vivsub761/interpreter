import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int currToken;
    List<Statement> statements = new ArrayList<>();
    Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currToken = 0;
    }
    List<Statement> parse() {

        while (this.currToken < this.tokens.size()) {
            this.statements.add(getNextStatement());
        }
        return this.statements;
    }
    Statement getNextStatement() {
        Token curr = getCurrToken();
        if (curr.type == TokenType.PRINT) {
            this.currToken++;
            checkType(TokenType.LEFT_P, "(");
            Expr expr = expression();
            checkType(TokenType.RIGHT_P, ")");
            semicolonCheck();
            return new Statement.Print(expr);
        } else if (curr.type == TokenType.VAR){
            this.currToken++;
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
            Expr initialVal = expression();
            semicolonCheck();
            return new Statement.StatementVar(name, initialVal);

        } else if (curr.type == TokenType.LEFT_B){
            this.currToken++;
            return new Statement.EnvBlock(block());
        } else if (curr.type == TokenType.IF) {
            this.currToken++;
            checkType(TokenType.LEFT_P, "Missing '(' after if");
            Expr condition = expression();
            checkType(TokenType.RIGHT_P, "Missing ')' after condition");
            Statement ifCondTrue = getNextStatement();
            if (this.tokens.get(this.currToken).type == TokenType.ELSE) {
                this.currToken++;
                Statement ifCondFalse = getNextStatement();
                return new Statement.ifStatement(condition, ifCondTrue, ifCondFalse);
            }
            return new Statement.ifStatement(condition, ifCondTrue, null);

        } else if (curr.type == TokenType.WHILE) {
            this.currToken++;
            checkType(TokenType.LEFT_P, "Missing ( after 'while'");
            Expr condition = expression();
            checkType(TokenType.RIGHT_P, "Missing ')' after while loop condition");
            Statement whileCode = getNextStatement();
            return new Statement.WhileStatement(condition, whileCode);

        } else if (curr.type == TokenType.FOR) {
            this.currToken++;
            checkType(TokenType.LEFT_P, "Missing '(' after for loop declaration");
            // this is a var declaration
            Statement initialize = getNextStatement();
            Expr condition = expression();
            semicolonCheck();
            Expr incrementation = expression();
            checkType(TokenType.RIGHT_P, "Missing ')' after specifying for loop incrementation");
            Statement forBlock = getNextStatement();
            return new Statement.ForStatement(condition, forBlock, initialize, incrementation);
        } else if (curr.type == TokenType.DEF) {
            this.currToken++;
            checkType(TokenType.IDENTIFIER, "Please add a function name after 'def' keyword");
            Token name = this.tokens.get(this.currToken -1 );
            checkType(TokenType.LEFT_P, "Missing '(' after function name");
            List<Token> args = new ArrayList<>();
            if (getCurrToken().type != TokenType.RIGHT_P) {
                checkType(TokenType.IDENTIFIER, "Missing argument identifier");
                args.add(this.tokens.get(this.currToken - 1));
                while (getCurrToken().type == TokenType.COMMA) {
                    checkType(TokenType.IDENTIFIER, "Missing argument identifier");
                    args.add(this.tokens.get(this.currToken - 1));
                }
            }
            checkType(TokenType.RIGHT_P, "Missing ')' after listing arguments");
            checkType(TokenType.LEFT_B, "Missing '{' after function is declared");
            List<Statement> funcBody = block();
            return new Statement.functionDef(name, args, funcBody);
        } else if (curr.type == TokenType.RETURN) {
            int lineNum = getCurrToken().lineNumber;
            this.currToken++;
            Expr returnVal = null;
            if (getCurrToken().type != TokenType.SEMICOLON) {
                returnVal = expression();
            }
            semicolonCheck();
            return new Statement.Return(lineNum, returnVal);
        } else {
            Expr expr = assignment();
            semicolonCheck();
            return new Statement.Expression(expr);
        }
    }

    private void checkType(TokenType type, String message) {
        if (getCurrToken().type != type) {
            Interpreter.error(getCurrToken().lineNumber, message);
        }
        this.currToken++;
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();
        while (this.currToken < this.tokens.size() && getCurrToken().type != TokenType.RIGHT_B) {
            statements.add(getNextStatement());
        }
        if (this.currToken >= this.tokens.size() || getCurrToken().type != TokenType.RIGHT_B) {
            Interpreter.error(getCurrToken().lineNumber, "Missing right bracket in line " + Integer.toString(this.getCurrToken().lineNumber));
        }
        this.currToken++;
        return statements;
    }

    private void semicolonCheck() {
        if (getCurrToken().type != TokenType.SEMICOLON) {
            Interpreter.error(getCurrToken().lineNumber, "missing semicolon in line " + Integer.toString(this.getCurrToken().lineNumber));
        }
        this.currToken++;
    }

    private Expr equality() {
        Expr left = comparison();
        if (this.currToken == this.tokens.size()) {
            return left;
        }
        Token curr = getCurrToken();
        while (curr.type == TokenType.DOUBLE_EQUAL || curr.type == TokenType.EXCLAMATIONEQUALS) {
            this.currToken++;
            Expr right = comparison();
            left = new Expr.Binary(left, curr, right);
            curr = getCurrToken();
        }
        return left;
    }


    private Expr comparison() {
        Expr left = term();
        if (this.currToken == this.tokens.size()) {
            return left;
        }
        Token curr = getCurrToken();
        while (curr.type == TokenType.GT || curr.type == TokenType.GTE
                || curr.type == TokenType.LT || curr.type == TokenType.LTE) {
            this.currToken++;
            Expr right = term();
            left = new Expr.Binary(left, curr, right);
            curr = getCurrToken();
        }
        return left;
    }

    private Expr term() {
        Expr left = factor();
        if (this.currToken == this.tokens.size()) {
            return left;
        }
        Token curr = getCurrToken();
        while (curr.type == TokenType.PLUS || curr.type == TokenType.MINUS) {
            this.currToken++;
            Expr right = factor();
            left = new Expr.Binary(left, curr, right);
            if (this.currToken >= this.tokens.size()) {
                break;
            }
            curr = getCurrToken();
        }
        return left;
    }

    private Expr factor() {
        Expr left = unary();
        if (this.currToken == this.tokens.size()) {
            return left;
        }
        Token curr = getCurrToken();
        while (curr.type == TokenType.SLASH || curr.type == TokenType.STAR) {
            this.currToken++;
            Expr right = unary();
            left = new Expr.Binary(left, curr, right);
            if (this.currToken >= this.tokens.size()) {
                break;
            }
            curr = getCurrToken();
        }
        return left;
    }
    private Expr unary() {
        Token curr = getCurrToken();
        if (curr.type == TokenType.EXCLAMATION || curr.type == TokenType.MINUS) {
            this.currToken++;
            return new Expr.Unary(unary(), curr);
        }
        return call();
    }

    private Expr call() {
        Expr left = primary();
        List<Expr> args = null;
        while (getCurrToken().type == TokenType.LEFT_P) {
            this.currToken++;
            args = new ArrayList<>();
            if (getCurrToken().type != TokenType.RIGHT_P) {
                args.add(expression());
                while (getCurrToken().type == TokenType.COMMA) {
                    this.currToken++;
                    args.add(expression());
                }
            }
            int lineNum = getCurrToken().lineNumber;
            checkType(TokenType.RIGHT_P, ")");
            left = new Expr.Call(left, args, lineNum);
        }
        return left;

    }

    private Expr primary() {
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
                    Expr expr = expression();
                    if (this.currToken++ >= this.tokens.size()) {
                        return expr;
                    }
                    if (getCurrToken().type == TokenType.RIGHT_P) {
                        this.currToken++;
                    } else {
                        Interpreter.error(getCurrToken().lineNumber, "no matching right parenthesis");
                    }
                    return new Expr.Grouping(expr);
                } else if (curr.type == TokenType.IDENTIFIER) {
                    return new Expr.Variable(this.tokens.get(this.currToken++));
                }
        }
        return null;
    }
    Token getCurrToken() {
        return this.tokens.get(this.currToken);
    }

    private Expr expression() {
        return assignment();
    }
    private Expr assignment() {
        Expr left = or();
        if (getCurrToken().type == TokenType.EQUAL) {
            Token curr = getCurrToken();
            this.currToken++;
            Expr value = assignment();
            if (left instanceof Expr.Variable) {
                return new Expr.Assignment(((Expr.Variable) left).varName, value);
            } else {
                Interpreter.error(getCurrToken().lineNumber, "Invalid assignment");
            }
        }
        return left;
    }

    private Expr or() {
        Expr left = and();
        while (getCurrToken().type == TokenType.OR) {
            Token operator = getCurrToken();
            this.currToken++;
            Expr right = equality();
            left = new Expr.Logical(left, operator, right);
        }
        return left;
    }

    private Expr and() {
        Expr left = equality();
        while (getCurrToken().type == TokenType.AND) {
            Token operator = getCurrToken();
            this.currToken++;
            Expr right = equality();
            left = new Expr.Logical(left, operator, right);
        }
        return left;
    }
}
