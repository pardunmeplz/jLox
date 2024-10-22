package com.craftingInterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    Environment environment = globals;
    private final HashMap<Expr, Integer> locals = new HashMap<>();

    Interpreter(){
        globals.define("clock", new LoxCallable(){
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 10000.0;
            }

            @Override
            public int arity(){return 0;}
        });
    }
    void interpret(List<Stmt> statements){
        try{
            for(Stmt statement : statements){
                execute(statement);
            }
        }catch (RuntimeError error){
           Lox.runtimeError(error);
        }
    }

    void resolve(Expr expr, int depth){
        locals.put(expr,depth);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if(distance!= null){
            environment.assignAt(distance, expr.name.lexeme(), value);
        }else {
            globals.assign(expr.name, value);
        }
        return value;
    }

    // expression logic
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object right = evaluate(expr.right);
        Object left = evaluate(expr.left);

         switch (expr.operator.type()){
            case TokenType.PLUS :
                if (left instanceof String && right instanceof String) return (String)left+(String)right;
                if (left instanceof Double && right instanceof Double)return (double)left + (double)right;
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings" );
            case TokenType.MINUS :
                checkNumberOperands(expr.operator, right, left);
                return (double)left - (double)right;
            case TokenType.STAR :
                checkNumberOperands(expr.operator, right, left);
                return (double)left * (double)right;
            case TokenType.SLASH :
                checkNumberOperands(expr.operator, right, left);
                return (double)left / (double)right;
            case TokenType.GREATER :
                checkNumberOperands(expr.operator, right, left);
                return (double)left > (double)right;
            case TokenType.LESS :
                checkNumberOperands(expr.operator, right, left);
                return (double)left < (double)right;
            case TokenType.GREATER_EQUAL :
                checkNumberOperands(expr.operator, right, left);
                return (double)left >= (double)right;
            case TokenType.LESS_EQUAL :
                checkNumberOperands(expr.operator, right, left);
                return (double)left <= (double)right;
            case TokenType.BANG_EQUAL :
                checkNumberOperands(expr.operator, right, left);
                return !isEqual(left,right);
            case TokenType.EQUAL_EQUAL :
                checkNumberOperands(expr.operator, right, left);
                return isEqual(left,right);
            default : return null;
        }
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.type()) {
            case TokenType.BANG : return !isTruthy(right);
            case TokenType.MINUS :
                checkNumberOperand(expr.operator,right);
                return -(double) right;
            default : return null;
        }
    }

    @Override
    public Object visitVarExpr(Expr.Var expr) {
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr){
       Integer distance = locals.get(expr);
       if(distance != null) {
           return environment.getAt(distance, name.lexeme());
       }else{
           return globals.get(name);
       }
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if(expr.operator.type() == TokenType.AND && !isTruthy(left)) return left;
        if(expr.operator.type() == TokenType.OR && isTruthy(left)) return left;
        return evaluate(expr.right);
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        if(!(callee instanceof LoxCallable function))throw new RuntimeError(expr.paren, "Expression is not callable, only functions and classes are callable");

        List<Object> arguments = new ArrayList<>();
        for(Expr argument: expr.arguments){
            arguments.add(evaluate(argument));
        }
        if(arguments.size() != function.arity()){
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments, got "
           + arguments.size() + " instead" );
        }
        return function.call(this,arguments);
    }

    @Override
    public Object visitGetExpressionExpr(Expr.GetExpression expr) {
        Object object = evaluate(expr.object);
        if(object instanceof LoxInstance){
           return ((LoxInstance) object).get(expr.name);
        }
        throw new RuntimeError(expr.name, "Only instances can have properties");
    }

    @Override
    public Object visitSetExpressionExpr(Expr.SetExpression expr) {
        Object object = evaluate(expr.object);
        if(object instanceof LoxInstance){
            Object value = evaluate(expr.value);
            ((LoxInstance) object).set(expr.name, value);
            return value;
        }
        throw new RuntimeError(expr.name, "Only instances can have properties");
    }

    private Object evaluate(Expr expression){
        return expression.accept(this);
    }

    private boolean isTruthy(Object object){
        if(object == null) return false;
        if(object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b){
        if(a == null && b == null) return true;
        if(a == null) return false;
        return a.equals(b);
    }

    // error handling checks
    private void checkNumberOperand(Token operator, Object operand){
        if (operand instanceof Double)return;
        throw new RuntimeError(operator, "Operand must be a number" );
    }

    private void checkNumberOperands(Token operator, Object a, Object b){
        if (a instanceof Double && b instanceof Double)return;
        throw new RuntimeError(operator, "Operands must be numbers" );
    }

    private String stringify(Object object){
        if (object ==  null)return "nil";

        if(object instanceof  Double){
            String text = object.toString();
            if(text.endsWith(".0")){
                text = text.substring(0, text.length()-2);
            }
            return text;
        }
        return object.toString();
    }

    // statement logic
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if(stmt.initializer != null){
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme(), value);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        Object condition = evaluate(stmt.condition);
        if(isTruthy(condition)) execute(stmt.thenStmt);
        else if(stmt.elseStmt != null)execute(stmt.elseStmt);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))){
            execute(stmt.loop);
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction loxFunction = new LoxFunction(stmt, this);
        this.environment.define(stmt.name.lexeme(), loxFunction);
        return null;
    }

    @Override
    public Void visitReturnStmtStmt(Stmt.ReturnStmt stmt) {
        Object value = evaluate(stmt.expr);
        throw new Return(value);
    }

    @Override
    public Void visitClassStmtStmt(Stmt.ClassStmt stmt) {
        environment.define(stmt.name.lexeme(), null);

        HashMap<String, LoxFunction> methods = new HashMap<>();
        for(Stmt.Function method: stmt.methods){
           methods.put(method.name.lexeme(), new LoxFunction(method, this));
        }

        LoxClass loxClass = new LoxClass(stmt.name.lexeme(), methods);
        environment.assign(stmt.name, loxClass);
        return null;
    }

    private void execute(Stmt stmt){
        stmt.accept(this);
    }

    public void executeBlock(List<Stmt> statements, Environment environment){
        Environment enclosing = this.environment;
        try {
            this.environment = environment;
            for(Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = enclosing;
        }

    }

}
