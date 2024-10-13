package com.craftingInterpreters.lox;

import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    Expr parse(){
        try{
            return expression();
        }catch (ParseError error){
            return null;
        }
    }
    private Expr expression(){
        return equality();
    }

    private Expr equality(){
        Expr expr = comparison();

        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right );
        }
        return expr;
    }

    private Expr comparison(){
        Expr expr = term();
        while(match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)){
            Token operator =  previous();
            Expr right = term();
            expr =  new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term(){
        Expr expr = factor();
        while(match(TokenType.PLUS, TokenType.MINUS)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor(){
        Expr expr = unary();
        while(match(TokenType.SLASH, TokenType.STAR)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary(){
        if(match(TokenType.BANG, TokenType.MINUS)){
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary(){
        Token token = advance();
        switch (token.type()){
            case TokenType.NUMBER:
            case TokenType.STRING:
                return new Expr.Literal(token.literal());
            case TokenType.TRUE:
                return new Expr.Literal(true);
            case TokenType.FALSE:
                return new Expr.Literal(false);
            case TokenType.NIL:
                return new Expr.Literal(null);
            case TokenType.LEFT_PAR:
                Expr groupExpression = expression();
                if(match(TokenType.RIGHT_PAR)){
                   return new Expr.Grouping(groupExpression);
                }
                throw error(peek(), " Expected ')' after expression.");
            default:
                throw error(peek(), "Expect expression.");
        }
    }

    private Token previous(){
        return tokens.get(current-1);
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token advance(){
        if(isAtEnd()) return previous();
        return tokens.get(current++);
    }

    private boolean isAtEnd(){
        return peek().type() == TokenType.EOF;
    }

    private boolean check(TokenType type){
        if(isAtEnd())return false;
        return peek().type() == type;
    }

    private boolean match(TokenType ...types){
       for(TokenType type: types){
          if( check(type) ){
              advance();
              return true;
          }
       }
       return false;
    }

    private ParseError error(Token token, String message){
        Lox.error(token, message);
        return new ParseError();
    }

    // At what point is it okay to start checking for errors again
    // (Panic mode end)
    private void synchronize(){
        advance();
        while(!isAtEnd()){
            if(previous().type() == TokenType.SEMICOLON){
                    return;
            }

            switch (peek().type()){
                case TokenType.CLASS:
                case TokenType.FUN:
                case TokenType.VAR:
                case TokenType.FOR:
                case TokenType.IF:
                case TokenType.WHILE:
                case TokenType.RETURN:
                case TokenType.PRINT:
                    return;
            }
        }
    }
}
