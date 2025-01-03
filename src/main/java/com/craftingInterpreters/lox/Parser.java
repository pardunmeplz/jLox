package com.craftingInterpreters.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/*
    program        → declaration* EOF ;

    declaration    → varDeclaration | statement | funcDecl | classDecl ;

    funcDecl       → "fun" function;
    function       → IDENTIFIER "(" parameters? ")" block;
    parameters     → IDENTIFIER ( "," IDENTIFIER )*;

    classDecl      → "class" IDENTIFIER ( "<" IDENTIFIER )? "{" function* "}" ;

    varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;

    statement      → exprStmt | ifStmt | whileStmt | forStmt | returnStmt |  printStmt | block;
    ifStmt         → "if" "(" expression ")" statement
                       (else statement)?;

    returnStmt     → "return" expression? ";" ;

    whileStmt      → "while" "(" expression ")" statement;
    forStmt        → "for" "(" varDecl | exprStmt | ";" expression? ";" expression? ")" statement;

    block          → "{" declaration "}";
    exprStmt       → expression ";" ;
    printStmt      → "print" expression ";" ;

    expression     → assignment;
    assignment     → (call ".")? IDENTIFIER "=" assignment | logicalOr;
    logicalOr      → logicalAnd ( "or" logicalAnd)*;
    logicalAnd     → equality ( "and" equality)*;
    equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    term           → factor ( ( "-" | "+" ) factor )* ;
    factor         → unary ( ( "/" | "*" ) unary )* ;
    unary          → ( "!" | "-" ) unary | primary ;
    call           → primary ( "(" arguments? ")" )* | getExpression;
    getExpression  → primary ( "." IDENTIFIER )*;
    arguments      → expression ( "," expression )*;
    primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | "super" "." IDENTIFIER ;
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
            if(match(TokenType.FUN)){
                return funcDeclaration("function");
            }
            if(match(TokenType.CLASS)){
                return classDeclaration();
            }
            return statement();
        }catch (ParseError error){
           synchronize();
           return null;
        }
    }

    private Stmt classDeclaration(){
        if(!match(TokenType.IDENTIFIER)) throw error(peek(),"Expected name for class declaration");
        Token name = previous();
        Expr.Var superClass = null;
        if(match(TokenType.LESS)){
            if(!match(TokenType.IDENTIFIER)) throw error(peek(),"Expected name for class declaration");
            superClass = new Expr.Var(previous());
        }
        if(!match(TokenType.LEFT_BRACE))throw error(peek(), "Expected '{' after class name");
        List<Stmt.Function> methods = new ArrayList<>();
        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd()){
            methods.add(funcDeclaration("method"));
        }
        if(!match(TokenType.RIGHT_BRACE))throw error(peek(), "Expected '}' at end of class declaration");
        return new Stmt.ClassStmt(name, superClass, methods);
    }

    private Stmt.Function funcDeclaration(String type){
       if(!match(TokenType.IDENTIFIER)) throw error(peek(),"Expected name for " + type + " declaration");
       Token name = previous();
       if(!match(TokenType.LEFT_PAR)) throw error(peek(),"Expected '(' after " + type + " name");
       List<Token> params = new ArrayList<>();
       if(!match(TokenType.RIGHT_PAR)){
           do{
               if(params.size() >= 255){
                   error(peek(), "Can't have more than 255 parameters");
               }
               params.add(advance());
           }while (match(TokenType.COMMA));
           if(!match(TokenType.RIGHT_PAR)) throw error(peek(),"Expected ')' after parameters");
       }
       if(!match(TokenType.LEFT_BRACE)) throw error(peek(),"Expected '{' before body");
       List<Stmt> body = block();
       return new Stmt.Function(name,params,body);
    }

    private Stmt varDeclaration(){
       if(match(TokenType.IDENTIFIER)){
           Token name = previous();
           Expr initial = null;
           if(match(TokenType.EQUAL)){
              initial = expression();
           }
           if(match(TokenType.SEMICOLON))return new Stmt.Var(name, initial);
           throw error(previous(), "Missing semicolon ';' at end of statement");
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
        if (match(TokenType.IF)) {
            return ifStmt();
        }
        if(match(TokenType.WHILE)){
            return whileStmt();
        }
        if(match(TokenType.FOR)){
            return forStmt();
        }
        if(match(TokenType.RETURN)){
            return returnStmt();
        }

        return exprStmt();
    }

    private Stmt returnStmt(){
        Token token = previous();
        Expr expr = new Expr.Literal(null);
        if(!check(TokenType.SEMICOLON)) expr = expression();
        if(!match(TokenType.SEMICOLON)) throw error(peek(), "Expected ';' at end of statement");
        return new Stmt.ReturnStmt(token, expr);
    }

    private Stmt whileStmt(){
        if(!match(TokenType.LEFT_PAR)) throw error(peek(), "Expected '(' after If statement");
        Expr condition = expression();
        if(!match(TokenType.RIGHT_PAR)) throw error(peek(), "Expected ')' after expression");

        Stmt loop = statement();
        return new Stmt.While(condition, loop);
    }

    private Stmt forStmt(){
        // todo: for loop not working, pls fix
        if(!match(TokenType.LEFT_PAR)) throw error(peek(), "Expected '(' after If statement");
        Stmt initialization = null;
        if(!match(TokenType.SEMICOLON)){
           if(match(TokenType.VAR)){
               initialization = varDeclaration();
           }else{
               initialization = exprStmt();
           }
        }
        Expr condition = new Expr.Literal(true);
        if(!match(TokenType.SEMICOLON)){
            condition = expression();
            if(!match(TokenType.SEMICOLON)) throw error(peek(), "Expected ';' after statement");
        }

        Expr increment = null;
        if(!match(TokenType.RIGHT_PAR)){
            increment = expression();
            if(!match(TokenType.RIGHT_PAR)) throw error(peek(), "Expected ')' after expression");
        }
        Stmt body = statement();
        if(increment != null){
            body = new Stmt.Block(Arrays.asList(
                    body,
                    new Stmt.Expression(increment)
            ));
        }
        body =  new Stmt.While(condition,body);
        if(initialization != null){
            body = new Stmt.Block(Arrays.asList(
                    initialization,
                    body
            ));
        }
        return body;
    }

    private Stmt ifStmt(){
        if(!match(TokenType.LEFT_PAR)) throw error(peek(), "Expected '(' after If statement");
        Expr condition = expression();
        if(!match(TokenType.RIGHT_PAR)) throw error(peek(), "Expected ')' after expression");

        Stmt then = statement();
        Stmt elseStmt = null;
        if(match(TokenType.ELSE)){
            elseStmt = statement();
        }
        return new Stmt.If(condition, then, elseStmt);
    }

    private Stmt exprStmt(){
        Expr expression =  expression();
        if(match((TokenType.SEMICOLON))){
            return new Stmt.Expression(expression);
        }
        throw error(previous(), "Missing semicolon ';' at end of statement");
    }

    private Stmt printStmt(){
        Expr expression =  expression();
        if(match(TokenType.SEMICOLON)){
            return new Stmt.Print(expression);
        }
        throw error(previous(), "Missing semicolon ';' at end of statement");
    }

    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd() && !match(TokenType.RIGHT_BRACE)){
            statements.add(declaration());
        }
        if(previous().type() != TokenType.RIGHT_BRACE) throw error(peek(), "Expect '}' after block");
        return statements;
    }

    private Expr expression(){
        return assignment();
    }

    private Expr assignment(){
        Expr expr = logicalOr();

        if(match(TokenType.EQUAL)){
           Token lValue = previous();
           Expr value = assignment();

           if(expr instanceof Expr.Var){
               Token name = ((Expr.Var)expr).name;
               return new Expr.Assign(name, value);
           } else if(expr instanceof Expr.GetExpression getExpr){
               return new Expr.SetExpression(getExpr.object, getExpr.name, value);
           }

           throw error(lValue, "Invalid assignment target");
        }
        return expr;
    }

    private Expr logicalOr(){
        Expr expr = logicalAnd();
        while(match(TokenType.OR)){
            Token operator = previous();
            Expr right = logicalAnd();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;

    }

    private Expr logicalAnd(){
        Expr expr =  equality();
        while(match(TokenType.AND)){
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator,right);
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
        return call();
    }

    private Expr call(){
        Expr expr = primary();
        while(match(TokenType.LEFT_PAR) || match(TokenType.DOT)){
           expr = previous().type() == TokenType.LEFT_PAR? finishCall(expr):getExpression(expr) ;
        }
        return expr;
    }

    private Expr getExpression(Expr object){
        if(!match(TokenType.IDENTIFIER))throw error(peek(), "Expected property name after '.'");
        return new Expr.GetExpression(object, previous());
    }

    private Expr finishCall(Expr callee){
        List<Expr> arguments = new ArrayList<>();
       if(match(TokenType.RIGHT_PAR)) return new Expr.Call(callee, previous(),arguments);
       do{
           if(arguments.size() >= 255){
               error(peek(),"Can't have more than 255 arguments");
           }
           arguments.add(expression());
       }while (match(TokenType.COMMA));
       if(!match(TokenType.RIGHT_PAR)) throw error(peek(), "Expected ')' after arguments");
       return new Expr.Call(callee, previous(),arguments);
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
            case TokenType.THIS:
                return new Expr.This(token);
            case TokenType.SUPER:
                if(!match(TokenType.DOT)) throw error(peek(),"Expected '.' after super");
                if(!match(TokenType.IDENTIFIER)) throw error(peek(),"Expected method name for superClass");
                return new Expr.Super(token, previous());
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
                default:advance();
            }
        }
    }
}
