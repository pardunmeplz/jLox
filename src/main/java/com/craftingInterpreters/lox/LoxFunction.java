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
        interpreter.executeBlock(functionStatement.body, environment);
        return null;
    }

    @Override
    public int arity() {
        return this.functionStatement.params.size();
    }
}
