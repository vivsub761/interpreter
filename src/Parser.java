import java.text.ParseException;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int currToken;
    Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currToken = 0;
    }
    Expr parse() {
        return equality();
    }

    private Expr equality() {
        Expr left = comparison();
        if (this.currToken == this.tokens.size()) {
            return left;
        }
        Token curr = this.tokens.get(this.currToken);
        while (curr.type == TokenType.DOUBLE_EQUAL || curr.type == TokenType.EXCLAMATIONEQUALS) {
            Expr right = comparison();
            left = new Expr.Binary(left, curr, right);
            curr = this.tokens.get(++this.currToken);
        }
        return left;
    }


    private Expr comparison() {
        Expr left = term();
        if (this.currToken == this.tokens.size()) {
            return left;
        }
        Token curr = this.tokens.get(this.currToken);
        while (curr.type == TokenType.GT || curr.type == TokenType.GTE
                || curr.type == TokenType.LT || curr.type == TokenType.LTE) {
            Expr right = term();
            left = new Expr.Binary(left, curr, right);
            curr = this.tokens.get(++this.currToken);
        }
        return left;
    }

    private Expr term() {
        Expr left = factor();
        if (this.currToken == this.tokens.size()) {
            return left;
        }
        Token curr = this.tokens.get(this.currToken);
        while (curr.type == TokenType.PLUS || curr.type == TokenType.MINUS) {
            this.currToken++;
            Expr right = factor();
            left = new Expr.Binary(left, curr, right);
            if (this.currToken >= this.tokens.size()) {
                break;
            }
            curr = this.tokens.get(this.currToken);
        }
        return left;
    }

    private Expr factor() {
        Expr left = unary();
        if (this.currToken == this.tokens.size()) {
            return left;
        }
        Token curr = this.tokens.get(this.currToken);
        while (curr.type == TokenType.SLASH || curr.type == TokenType.STAR) {
            this.currToken++;
            Expr right = unary();
            left = new Expr.Binary(left, curr, right);
            if (this.currToken >= this.tokens.size()) {
                break;
            }
            curr = this.tokens.get(this.currToken);
        }
        return left;
    }
    private Expr unary() {
        Token curr = this.tokens.get(this.currToken);
        if (curr.type == TokenType.EXCLAMATION || curr.type == TokenType.MINUS) {
            this.currToken++;
            return new Expr.Unary(unary(), curr);
        }
        return primary();
    }

    private Expr primary() {
        Token curr = this.tokens.get(this.currToken);
        switch (curr.type) {
            case NULL: return new Expr.Literal(null);
            case TRUE: return new Expr.Literal(true);
            case FALSE: return new Expr.Literal(false);
            default:
                if (curr.type == TokenType.NUM || curr.type == TokenType.STRING) {
                    this.currToken++;
                    return new Expr.Literal(curr.literal);
                } else if (curr.type == TokenType.LEFT_P) {
                    this.currToken++;
                    Expr expr = equality();
                    if (this.currToken == this.tokens.size()) {
                        return expr;
                    }
                    if (this.tokens.get(this.currToken).type == TokenType.RIGHT_P) {
                        this.currToken++;
                    } else {
                        Interpreter.error(1, "no matching right parenthesis");
                    }
                    return new Expr.Grouping(expr);
                }

        }
        return null;
    }
}
