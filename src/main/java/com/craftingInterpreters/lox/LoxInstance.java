package com.craftingInterpreters.lox;

import java.util.HashMap;

public class LoxInstance {
    LoxClass loxClass;
    HashMap<String, Object> fields = new HashMap<>();
    LoxInstance(LoxClass loxClass){
        this.loxClass = loxClass;
    }
    @Override
    public String toString(){
        return loxClass.name + " instance";
    }

    Object get(Token name){
        if(fields.containsKey(name.lexeme())){
           return fields.get(name.lexeme());
        }
        LoxFunction method = loxClass.findMethod(name.lexeme()).bind(this);
        if(method != null)return method;

        throw new RuntimeError(name, "Property " + name.lexeme() +" not found in instance");
    }

    void set(Token name, Object value){
       fields.put(name.lexeme(), value);
    }
}
