package com.craftingInterpreters.lox;

public record Token(int line, Object literal, String lexeme, TokenType type){
    public String toString(){
        return this.type + " " + this.lexeme + " " + this.literal;
    }
};