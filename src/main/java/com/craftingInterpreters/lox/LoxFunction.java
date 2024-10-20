package com.craftingInterpreters.lox;

import java.util.List;

public class LoxFunction implements LoxCallable{
    private final Stmt.Function functionStatement;
    LoxFunction(Stmt.Function functionStatement){
        this.functionStatement = functionStatement;
    }
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.globals);
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
            return retValue.value;
        }
        return null;
    }

    @Override
    public int arity() {
        return this.functionStatement.params.size();
    }
}
