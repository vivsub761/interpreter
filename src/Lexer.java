import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String fileData;
    private final List<Token> tokens;
    private int currTokenStart = 0;
    private int curr = 0;

    private int line;

    private static final Map<String, TokenType> keywordMap;
    static {
        keywordMap = new HashMap<>();
        keywordMap.put("else",   TokenType.ELSE);
        keywordMap.put("false",  TokenType.FALSE);
        keywordMap.put("for",    TokenType.FOR);
        keywordMap.put("def",    TokenType.DEF);
        keywordMap.put("if",     TokenType.IF);
        keywordMap.put("nil",    TokenType.NULL);
        keywordMap.put("print",  TokenType.PRINT);
        keywordMap.put("return", TokenType.RETURN);
        keywordMap.put("true",   TokenType.TRUE);
        keywordMap.put("var",    TokenType.VAR);
        keywordMap.put("while",  TokenType.WHILE);
        keywordMap.put("elif", TokenType.ELIF);
    }

    Lexer(String fileData) {
        this.fileData = fileData;
        this.tokens = new ArrayList<>();
        tokenize();
    }


    public void tokenize() {
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
            case '.': addToken(TokenType.DOT, ".", null); break;
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
                break;
            case '&':
                if (checkNext('&')) {
                    addToken(TokenType.AND, "&&", null);
                } else {
                    addToken(TokenType.LOGICAL_AND, "&", null);
                }
                break;
            case '|':
                if (checkNext('|')) {
                    addToken(TokenType.OR, "||", null);
                } else {
                    addToken(TokenType.LOGICAL_OR, "|", null);
                }
                break;
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
        this.tokens.add(new Token(type, lexeme, literal, line));
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
        if (this.curr >= this.fileData.length()) {
            Interpreter.error(this.line, "String not terminated");
        }
        this.curr++;
        addToken(TokenType.STRING, this.fileData.substring(this.currTokenStart, this.curr), strBuilder.toString());
    }

    private void numberLiteral() {
        while (this.curr < this.fileData.length() && Character.isDigit(this.fileData.charAt(this.curr))) {
            this.curr++;
        }
        if (checkNext('.')) {
            this.curr++;
        }
        while (this.curr < this.fileData.length() && Character.isDigit(this.fileData.charAt(this.curr))) {
            this.curr++;
        }
        String capturedNum = this.fileData.substring(this.currTokenStart, this.curr);
        addToken(TokenType.NUM, capturedNum, Float.parseFloat(capturedNum));

    }

    private void keyword() {
        char c = this.fileData.charAt(this.curr);
        StringBuilder strBuilder = new StringBuilder();
        while (Character.isLetter(c) || Character.isDigit(c) || c == '_') {
            strBuilder.append(c);
            c = this.fileData.charAt(++this.curr);
        }
        String s = strBuilder.toString();
        if (keywordMap.containsKey(s)) {
            addToken(keywordMap.get(s), s, null);
        } else {
            addToken(TokenType.IDENTIFIER, s, null);
        }

    }
}
