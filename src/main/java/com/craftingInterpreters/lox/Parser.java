package com.craftingInterpreters.lox;

import java.util.ArrayList;
import java.util.List;
/*
    program        → declaration* EOF ;

    declaration    → varDeclaration | statement ;
    varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
    statement      → exprStmt | printStmt | block;
    block          → "{" declaration "}";
    exprStmt       → expression ";" ;
    printStmt      → "print" expression ";" ;

    expression     → assignment;
    assignment     → IDENTIFIER "=" assignment | equality;
    equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    term           → factor ( ( "-" | "+" ) factor )* ;
    factor         → unary ( ( "/" | "*" ) unary )* ;
    unary          → ( "!" | "-" ) unary | primary ;
    primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
*/



public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        try {
            while (!isAtEnd()) {
                statements.add(declaration());
            }
            return statements;
        } catch (RuntimeError error){
            return null;
        }
    }

    private Stmt declaration(){
        try {
            if (match(TokenType.VAR)) {
                return varDeclaration();
            }
            return statement();
        }catch (ParseError error){
           synchronize();
           return null;
        }
    }

    private Stmt varDeclaration(){
       if(match(TokenType.IDENTIFIER)){
           Token name = previous();
           Expr initial = null;
           if(match(TokenType.EQUAL)){
              initial = expression();
           }
           if(match(TokenType.SEMICOLON))return new Stmt.Var(name, initial);
           throw error(peek(), "Missing semicolon ';' at end of statement");
       }
        throw error(peek(),"Expected name for variable declaration");
    }

    private Stmt statement(){
        if(match(TokenType.PRINT)){
            return printStmt();
        }
        if(match(TokenType.LEFT_BRACE)){
            return new Stmt.Block(block());
        }
        return exprStmt();
    }

    private Stmt exprStmt(){
        Expr expression =  expression();
        if(match((TokenType.SEMICOLON))){
            return new Stmt.Expression(expression);
        }
        throw error(peek(), "Missing semicolon ';' at end of statement");
    }

    private Stmt printStmt(){
        Expr expression =  expression();
        if(match(TokenType.SEMICOLON)){
            return new Stmt.Print(expression);
        }
        throw error(peek(), "Missing semicolon ';' at end of statement");
    }

    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd() && !match(TokenType.RIGHT_BRACE)){
            statements.add(declaration());
        }
        if(isAtEnd()) throw error(peek(), "Expect '}' after block");
        return statements;
    }

    private Expr expression(){
        return assignment();
    }

    private Expr assignment(){
        Expr expr = equality();

        if(match(TokenType.EQUAL)){
           Token lValue = previous();
           Expr value = assignment();

           if(expr instanceof Expr.Var){
               Token name = ((Expr.Var)expr).name;
               return new Expr.Assign(name, value);
           }

           throw error(lValue, "Invalid assignment target");
        }
        return expr;
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
            case TokenType.IDENTIFIER:
                return new Expr.Var(token);
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
