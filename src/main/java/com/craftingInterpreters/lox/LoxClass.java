package com.craftingInterpreters.lox;

import java.util.HashMap;
import java.util.List;

public class LoxClass implements LoxCallable {
    final String name;
    final HashMap<String, LoxFunction> methods;
    LoxClass(String name, HashMap<String, LoxFunction> methods){
        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString(){
        return name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance loxInstance = new LoxInstance(this);
        return loxInstance;
    }

    @Override
    public int arity() {
        return 0;
    }
}
