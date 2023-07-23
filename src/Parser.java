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
            Expr expr = expression();
//            if (!(expr instanceof Expr.Grouping)) {
//                Interpreter.error(6, "Missing parentheses");
//            }
            semicolonCheck();
            return new Statement.Print(expr);
        } else if (curr.type == TokenType.VAR){
            this.currToken++;
            Token next = getCurrToken();
            if (next.type != TokenType.IDENTIFIER) {
                Interpreter.error(1, "Expect variable name after var keyword");
            }
            Token name = getCurrToken();
            this.currToken++;
            next = getCurrToken();
            if (next.type != TokenType.EQUAL) {
                Interpreter.error(1,"Initial value needed");
            }
            this.currToken++;
            Expr initialVal = expression();
            semicolonCheck();
            return new Statement.StatementVar(name, initialVal);

        } else {
            Expr expr = assignment();
            semicolonCheck();
            return new Statement.Expression(expr);

        }
    }

    private void semicolonCheck() {
        if (getCurrToken().type != TokenType.SEMICOLON) {
            Interpreter.error(6, "missing semicolon");
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
            Expr right = comparison();
            left = new Expr.Binary(left, curr, right);
            this.currToken++;
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
            Expr right = term();
            left = new Expr.Binary(left, curr, right);
            this.currToken++;
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
        return primary();
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
                        Interpreter.error(1, "no matching right parenthesis");
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
        Expr left = equality();
        if (getCurrToken().type == TokenType.EQUAL) {
            Token curr = getCurrToken();
            this.currToken++;
            Expr value = assignment();
            if (left instanceof Expr.Variable) {
                return new Expr.Assignment(((Expr.Variable) left).varName, value);
            } else {
                Interpreter.error(1, "Invalid assignment");
            }
        }
        return left;
    }
}
