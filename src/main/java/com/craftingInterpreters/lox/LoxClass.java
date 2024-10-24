package com.craftingInterpreters.lox;

import java.util.HashMap;
import java.util.List;

public class LoxClass implements LoxCallable {
    final String name;
    final HashMap<String, LoxFunction> methods;
    final LoxClass superClass;

    LoxClass(String name, HashMap<String, LoxFunction> methods, LoxClass superClass){
        this.name = name;
        this.methods = methods;
        this.superClass = superClass;
    }

    @Override
    public String toString(){
        return name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance loxInstance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if(initializer != null){
            initializer.bind(loxInstance).call(interpreter, arguments);
        }
        return loxInstance;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if(initializer == null)return 0;
        return initializer.arity();
    }

    LoxFunction findMethod(String name){
        if(this.methods.containsKey(name)){
            return this.methods.get(name);
        }
        if(this.superClass != null){
            return this.superClass.findMethod(name);
        }
        return null;
    }
}