package com.craftingInterpreters.lox;

import java.util.List;

abstract class Stmt {
 interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);
    R visitPrintStmt(Print stmt);
    R visitVarStmt(Var stmt);
    R visitBlockStmt(Block stmt);
    R visitIfStmt(If stmt);
    R visitWhileStmt(While stmt);
  }
 static class Expression extends Stmt {
     Expression(Expr expression) {
        this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
   }
 static class Print extends Stmt {
     Print(Expr expression) {
        this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitPrintStmt(this);
    }

    final Expr expression;
   }
 static class Var extends Stmt {
     Var(Token name, Expr initializer) {
        this.name = name;
        this.initializer = initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitVarStmt(this);
    }

    final Token name;
    final Expr initializer;
   }
 static class Block extends Stmt {
     Block(List<Stmt> statements) {
        this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitBlockStmt(this);
    }

    final List<Stmt> statements;
   }
 static class If extends Stmt {
     If(Expr condition, Stmt thenStmt, Stmt elseStmt) {
        this.condition = condition;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitIfStmt(this);
    }

    final Expr condition;
    final Stmt thenStmt;
    final Stmt elseStmt;
   }
 static class While extends Stmt {
     While(Expr condition, Stmt loop) {
        this.condition = condition;
        this.loop = loop;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitWhileStmt(this);
    }

    final Expr condition;
    final Stmt loop;
   }

   abstract <R> R accept(Visitor<R> visitor);
}
