package com.craftingInterpreters.lox;

import java.util.List;

abstract class Expr {
 interface Visitor<R> {
    R visitAssignExpr(Assign expr);
    R visitBinaryExpr(Binary expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitUnaryExpr(Unary expr);
    R visitVarExpr(Var expr);
    R visitLogicalExpr(Logical expr);
    R visitCallExpr(Call expr);
    R visitGetExpressionExpr(GetExpression expr);
    R visitSetExpressionExpr(SetExpression expr);
    R visitSuperExpr(Super expr);
    R visitThisExpr(This expr);
  }
 static class Assign extends Expr {
     Assign(Token name, Expr value) {
        this.name = name;
        this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitAssignExpr(this);
    }

    final Token name;
    final Expr value;
   }
 static class Binary extends Expr {
     Binary(Expr left, Token operator, Expr right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
   }
 static class Grouping extends Expr {
     Grouping(Expr expression) {
        this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitGroupingExpr(this);
    }

    final Expr expression;
   }
 static class Literal extends Expr {
     Literal(Object value) {
        this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitLiteralExpr(this);
    }

    final Object value;
   }
 static class Unary extends Expr {
     Unary(Token operator, Expr right) {
        this.operator = operator;
        this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
   }
 static class Var extends Expr {
     Var(Token name) {
        this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitVarExpr(this);
    }

    final Token name;
   }
 static class Logical extends Expr {
     Logical(Expr left, Token operator, Expr right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitLogicalExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
   }
 static class Call extends Expr {
     Call(Expr callee, Token paren, List<Expr> arguments) {
        this.callee = callee;
        this.paren = paren;
        this.arguments = arguments;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitCallExpr(this);
    }

    final Expr callee;
    final Token paren;
    final List<Expr> arguments;
   }
 static class GetExpression extends Expr {
     GetExpression(Expr object, Token name) {
        this.object = object;
        this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitGetExpressionExpr(this);
    }

    final Expr object;
    final Token name;
   }
 static class SetExpression extends Expr {
     SetExpression(Expr object, Token name, Expr value) {
        this.object = object;
        this.name = name;
        this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitSetExpressionExpr(this);
    }

    final Expr object;
    final Token name;
    final Expr value;
   }
 static class Super extends Expr {
     Super(Token Keyword, Token method) {
        this.Keyword = Keyword;
        this.method = method;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitSuperExpr(this);
    }

    final Token Keyword;
    final Token method;
   }
 static class This extends Expr {
     This(Token keyword) {
        this.keyword = keyword;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
     return visitor.visitThisExpr(this);
    }

    final Token keyword;
   }

   abstract <R> R accept(Visitor<R> visitor);
}
