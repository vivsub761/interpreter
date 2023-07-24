class Token {
    final TokenType type;
    final String lexeme;
    final int lineNumber;
    final Object literal;
    Token(TokenType tokenType, String lexeme, Object literal, int lineNumber) {
        this.type = tokenType;
        this.lexeme = lexeme;
        this.literal = literal;
        this.lineNumber = lineNumber;
    }

}
