package com.craftingInterpreters.lox;

import java.util.HashMap;

public class Environment {
    final HashMap<String, Object> environmentMapping = new HashMap<>();

    void define(String name, Object value){
        environmentMapping.put(name, value);
    }

    Object get(Token name){
        if(environmentMapping.containsKey(name.lexeme()))return environmentMapping.get(name.lexeme());
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'");
    }

    void assign(Token name, Object value){
        if(environmentMapping.containsKey(name.lexeme())){
            environmentMapping.put(name.lexeme(), value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'");
    }
}
