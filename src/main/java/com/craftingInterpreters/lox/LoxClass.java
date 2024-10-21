package com.craftingInterpreters.lox;

import java.util.List;

public class LoxClass implements LoxCallable {
    final String name;
    LoxClass(String name){
        this.name = name;
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
