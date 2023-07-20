class Token {
    final TokenType type;
    final String lexeme;

    final Object literal;
    Token(TokenType tokenType, String lexeme, Object literal) {
        this.type = tokenType;
        this.lexeme = lexeme;
        this.literal = literal;
    }

}
