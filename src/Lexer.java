import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String fileData;
    private final List<Token> tokens;
    private int currTokenStart = 0;
    private int curr = 0;

    private int line;
    Lexer(String fileData) {
        this.fileData = fileData;
        this.tokens = new ArrayList<>();
        tokenize();
    }


    public void tokenize() {
        int currTokenStart = 0;
        while (this.curr < this.fileData.length()) {
            this.currTokenStart = this.curr;
            addNextToken();
        }
    }
    public List<Token> getTokens() {
        return this.tokens;
    }

    private void addNextToken() {
        switch(this.fileData.charAt(this.curr++)) {
            //Single character tokens: ( ) , . { } + - * ;
            case '(': addToken(TokenType.LEFT_P, "(", null); break;
            case ')': addToken(TokenType.RIGHT_P, ")", null); break;
            case ',': addToken(TokenType.COMMA, ",", null); break;
            case '.': addToken(TokenType.COMMA, ".", null); break;
            case '{': addToken(TokenType.LEFT_B, "{", null); break;
            case '}': addToken(TokenType.RIGHT_B, "}", null); break;
            case '+': addToken(TokenType.PLUS, "+", null); break;
            case '-': addToken(TokenType.MINUS, "-", null); break;
            case ';': addToken(TokenType.SEMICOLON, ";", null); break;
            case '*': addToken(TokenType.STAR, "*", null); break;

            //Multi-char tokens

            case '!':
                if (checkNext('=')) {
                    addToken(TokenType.EXCLAMATIONEQUALS, "!=", null);
                } else {
                    addToken(TokenType.EXCLAMATION, "=", null);
                }
                break;
            case '=':
                if (checkNext('=')) {
                    addToken(TokenType.DOUBLE_EQUAL, "==", null);
                } else {
                    addToken(TokenType.EQUAL, "=", null);
                }
                break;
            case '>':
                if (checkNext('=')) {
                    addToken(TokenType.GTE, ">=", null);
                } else {
                    addToken(TokenType.GT, ">", null);
                }
                break;
            case '<':
                if (checkNext('=')) {
                    addToken(TokenType.LTE, "<=", null);
                } else {
                    addToken(TokenType.LT, "<", null);
                }
                break;
            case '/':
                if (checkNext('/')) {
                    addToken(TokenType.DOUBLE_SLASH, "//", null);
                } else {
                    addToken(TokenType.SLASH, "/", null);
                }
            case ' ':
            case '\t':
            case '\r':
                break;
            case '\n':
                this.line++;
                break;

            //literals
            case '"':
                stringLiteral();
                break;
            default:
                char c = this.fileData.charAt(this.curr - 1);
                if (Character.isDigit(c)) {
                    this.curr--;
                    numberLiteral();
                } else if (Character.isLetter(c) | c == '_') {
                    this.curr--;
                    keyword();
                } else {
                    Interpreter.error(this.line, "Invalid Character");
                }
        }
    }

    private boolean checkNext(char comparator) {
        if (this.curr >= this.fileData.length()) {
            return false;
        } else if (this.fileData.charAt(this.curr) != comparator) {
            return false;
        }
        this.curr++;
        return true;
    }
    private void addToken(TokenType type, String lexeme, Object literal) {
        this.tokens.add(new Token(type, lexeme, literal));
    }

    private void stringLiteral() {
        StringBuilder strBuilder = new StringBuilder();
        while (this.curr < this.fileData.length() && this.fileData.charAt(this.curr) != '"') {
            if (this.fileData.charAt(this.curr) == '\n') {
                this.line++;
            }
            strBuilder.append(this.fileData.charAt(this.curr));
            this.curr++;
        }
        this.curr++;
        if (this.curr >= this.fileData.length()) {
            Interpreter.error(this.line, "String not terminated");
        }
        addToken(TokenType.STRING, this.fileData.substring(this.currTokenStart, this.curr), strBuilder.toString());
    }

    private void numberLiteral() {
        while (Character.isDigit(this.fileData.charAt(this.curr))) {
            this.curr++;
        }
        if (checkNext('.')) {
            this.curr++;
        }
        while (Character.isDigit(this.fileData.charAt(this.curr))) {
            this.curr++;
        }
        String capturedNum = this.fileData.substring(this.currTokenStart, this.curr);
        addToken(TokenType.NUM, capturedNum, Double.parseDouble(capturedNum));

    }

    private void keyword() {

    }
}
