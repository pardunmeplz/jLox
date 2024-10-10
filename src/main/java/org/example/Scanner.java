package org.example;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    // location info
    private int start = 0, current=0, line=0;
    Scanner(String source){
        this.source = source;
    }

    public List<Token> scanTokens(){
        while(!isAtEnd()){
            // start of new lexeme
            start = current;
            scanToken();
        }
        // end of file token
        tokens.add(new Token(line, null, "", TokenType.EOF));
        return tokens;
    }

    private Boolean isAtEnd(){
        return current >= source.length();
    }

    private void scanToken(){
        // todo: implement keyword and identifier scanning
        char c = advance();
        switch (c){
            case '(':
                addToken(TokenType.LEFT_PAR);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAR);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            case '=':
                if(match('=')) addToken(TokenType.EQUAL_EQUAL);
                else addToken(TokenType.EQUAL);
                break;
            case '!':
                if(match('=')) addToken(TokenType.BANG_EQUAL);
                else addToken(TokenType.BANG);
                break;
            case '<':
                if(match('=')) addToken(TokenType.LESS_EQUAL);
                else addToken(TokenType.LESS);
                break;
            case '>':
                if(match('=')) addToken(TokenType.GREATER_EQUAL);
                else addToken(TokenType.GREATER);
                break;
            case '/':
                if(match('/'))while(peek() != '\n' && !isAtEnd()) advance();
                else addToken(TokenType.SLASH);
                break;
            case '"':
                while(peek() != '"' && !isAtEnd()){
                    if(advance() == '\n')line++;
                };
                if(isAtEnd()){
                    Lox.error(line, "Missing \", unterminated string");
                    break;
                }
                advance();
                addToken( TokenType.STRING, source.substring(start+1, current-1));
            case ' ':
            case '\t':
            case '\r':
                break;
            case '\n':
                line ++;
                break;
            default:
                if(isDigit(c)){
                    while(isDigit(peek()) && ! isAtEnd())advance();
                    if(peek() == '.' && isDigit(peekNext())){
                        do advance();
                        while (isDigit(peek()) && !isAtEnd());
                    }
                    addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start,current)));
                    break;
                }
                Lox.error(line,"Unexpected character "+c);
                break;
        }
    }
    private void addToken(TokenType type){
        addToken(type, null);
    }
    private void addToken(TokenType type, Object literal){
        String lexeme = source.substring(start, current);
        tokens.add(new Token(line, literal, lexeme, type));
    }

    private char advance(){
        return source.charAt(current++);
    }

    private char peek(){
        if (current >= source.length()) return '\0';
        return source.charAt(current);
    }

    private char peekNext(){
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }

    private boolean match(char c){
        if(isAtEnd()) return false;
        if(peek() == c){
            advance();
            return true;
        }
        return false;
    }

    private boolean isDigit(char c){
        return '0' <= c && c <='9';
    }
}
