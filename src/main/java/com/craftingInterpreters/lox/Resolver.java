package com.craftingInterpreters.lox;
// resolving variable scopes before moving on to interpretation

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>{
    final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;
    private enum FunctionType{
        NONE,
        METHOD,
        INITIALIZER,
        FUNCTION
    }
    private enum ClassType{
        CLASS,
        SUBCLASS,
        NONE
    }
    Resolver(Interpreter interpreter){
       this.interpreter = interpreter;
    }

    public void resolve(List<Stmt>stmts){
        for(Stmt stmt: stmts){
            resolve(stmt);
        }
    }

    void resolve(Stmt stmt){
        stmt.accept(this);
    }

    void resolve(Expr expr){
        expr.accept(this);
    }

    void beginScope(){
        scopes.add(new HashMap<>());
    }

    void endScope(){
        scopes.pop();
    }

    void declare(Token token){
        if(scopes.isEmpty())return;

        Map<String, Boolean> scope = scopes.peek();
        if(scope.containsKey(token.lexeme())){
            Lox.error(token, "A local variable can not be initialized twice");
        }
        scope.put(token.lexeme(), false);
    }

    void define(Token token){
        if(scopes.isEmpty())return;

        Map<String, Boolean> scope = scopes.peek();
        scope.put(token.lexeme(), true);
    }

    void resolveLocal(Expr expr, Token name){
        int i = 0;
        for(Map<String, Boolean> scope: scopes.reversed()){
            if(scope.containsKey(name.lexeme())){
                interpreter.resolve(expr, i);
                return;
            }
            i++;
        }
    }

    void resolveFunction(Stmt.Function stmt, FunctionType functionType){
       beginScope();
       FunctionType outerFunc = currentFunction;
       currentFunction = functionType;

       for(Token param: stmt.params){
           declare(param);
           define(param);
       }
       resolve(stmt.body);
       endScope();
       currentFunction = outerFunc;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr,expr.name);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVarExpr(Expr.Var expr) {
       if(!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme()) == Boolean.FALSE){
           Lox.error(expr.name, "Can't read local variable in it's own initializer.");
       }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);
        for(Expr argument: expr.arguments){
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitGetExpressionExpr(Expr.GetExpression expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSetExpressionExpr(Expr.SetExpression expr) {
        resolve(expr.object);
        resolve(expr.value);
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        if(currentClass != ClassType.SUBCLASS) Lox.error(expr.Keyword, "'super' can only be used in a subclass");
        resolveLocal(expr, expr.Keyword);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        if(currentClass == ClassType.NONE) Lox.error(expr.keyword, "'this' keyword can only be used in a class method");
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if(stmt.initializer != null)resolve(stmt.initializer);
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenStmt);
        if(stmt.elseStmt != null)resolve(stmt.elseStmt);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.loop);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitReturnStmtStmt(Stmt.ReturnStmt stmt) {
        if(currentFunction == FunctionType.NONE) Lox.error(stmt.keyword, "Can not return from top-level code");
        if(stmt.expr != null){
            if(currentFunction == FunctionType.INITIALIZER) Lox.error(stmt.keyword, "Can not return a value from initializer");
            resolve(stmt.expr);
        }
        return null;
    }

    @Override
    public Void visitClassStmtStmt(Stmt.ClassStmt stmt) {

        declare(stmt.name);
        define(stmt.name);
        ClassType surroundingClass = currentClass;
        currentClass = ClassType.CLASS;
        if(stmt.superclass != null){
            if(stmt.name.lexeme().equals(stmt.superclass.name.lexeme())) Lox.error(stmt.superclass.name, "A class can not inherit from itself");
            currentClass =  ClassType.SUBCLASS;
            resolve(stmt.superclass);
            beginScope();
            scopes.peek().put("super", true);
        }
        beginScope();
        scopes.peek().put("this", true);
        for(Stmt.Function method: stmt.methods){
            FunctionType declaration = FunctionType.METHOD;
            resolveFunction(method,declaration);
        }
        endScope();
        if(stmt.superclass !=null)endScope();
        currentClass = surroundingClass;
        return null;
    }
}