package com.craftingInterpreters.lox;

public enum TokenType {
   // Single char
   LEFT_PAR, RIGHT_PAR, LEFT_BRACE, RIGHT_BRACE,
   COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

   // one or two char
    BANG, BANG_EQUAL, EQUAL,
    EQUAL_EQUAL, GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // literals
    IDENTIFIER, STRING, NUMBER,

    // keywords
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT,RETURN, SUPER, THIS,TRUE, VAR, WHILE,

    EOF
}