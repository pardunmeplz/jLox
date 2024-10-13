package com.craftingInterpreters.lox;

public class Interpreter implements Expr.Visitor<Object> {
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
                return (double)left - (double)right;
            case TokenType.STAR : return (double)left * (double)right;
            case TokenType.SLASH : return (double)left / (double)right;
            case TokenType.GREATER : return (double)left > (double)right;
            case TokenType.LESS : return (double)left < (double)right;
            case TokenType.GREATER_EQUAL : return (double)left >= (double)right;
            case TokenType.LESS_EQUAL : return (double)left <= (double)right;
            case TokenType.BANG_EQUAL : return !isEqual(left,right);
            case TokenType.EQUAL_EQUAL : return isEqual(left,right);
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
        if(a ==null) return false;
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
}
