package com.craftingInterpreters.lox;

import java.util.List;

public class LoxFunction implements LoxCallable{
    private final Stmt.Function functionStatement;
    private final Environment closure;
    private final Boolean isInitializer;

    LoxFunction(Stmt.Function functionStatement, Interpreter interpreter, Boolean isInitializer){
        this.functionStatement = functionStatement;
        this.closure = interpreter.environment;
        this.isInitializer = isInitializer;
    }

    LoxFunction(Stmt.Function functionStatement, Environment closure, Boolean isInitializer ){
        this.functionStatement = functionStatement;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        int i = 0;
        for(Token param: this.functionStatement.params){
            environment.define(param.lexeme(), arguments.get(i));
            i++;
        }
        // it's slightly insane to me that the error handling is being used as a control flow mechanism
        // to run the return statement
        try {
            interpreter.executeBlock(functionStatement.body, environment);
        } catch (Return retValue){
            if(isInitializer) return closure.getAt(0,"this");
            return retValue.value;
        }
        if(isInitializer) return closure.getAt(0,"this");
        return null;
    }

    @Override
    public int arity() {
        return this.functionStatement.params.size();
    }

    LoxFunction bind(LoxInstance instance){
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(functionStatement, environment, this.isInitializer);
    }
}
