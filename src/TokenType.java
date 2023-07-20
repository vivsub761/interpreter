enum TokenType {
    //keywords
    FOR, WHILE, IF, ELIF, NULL, TRUE, FALSE, DEF, ELSE,

    //symbols
    OR, AND, EXCLAMATION, EXCLAMATIONEQUALS, LT, LTE, GT, GTE, EQUAL, DOUBLE_EQUAL,
    LEFT_P, RIGHT_P, LEFT_B, RIGHT_B, SEMICOLON, COMMA, DOT,

    //Math
    PLUS, MINUS, SLASH, DOUBLE_SLASH, STAR,

    IDENTIFIER, STRING, NUM
}