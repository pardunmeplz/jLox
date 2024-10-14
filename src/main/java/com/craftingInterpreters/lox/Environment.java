package com.craftingInterpreters.lox;

import java.util.HashMap;

public class Environment {
    final HashMap<String, Object> environmentMapping = new HashMap<>();
    final Environment enclosing;

    Environment(){
        this.enclosing = null;
    }
    Environment(Environment enclosing){
        this.enclosing = enclosing;
    }
    void define(String name, Object value){
        environmentMapping.put(name, value);
    }

    Object get(Token name){
        if(environmentMapping.containsKey(name.lexeme()))return environmentMapping.get(name.lexeme());
        if(enclosing == null) throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'");
        return enclosing.get(name);
    }

    void assign(Token name, Object value){
        if(environmentMapping.containsKey(name.lexeme())){
            environmentMapping.put(name.lexeme(), value);
            return;
        }
        if(enclosing == null)throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'");
        enclosing.assign(name, value);
    }
}
