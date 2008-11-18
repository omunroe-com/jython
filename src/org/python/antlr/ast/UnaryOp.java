// Autogenerated AST node
package org.python.antlr.ast;
import org.python.antlr.PythonTree;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import java.io.DataOutputStream;
import java.io.IOException;

public class UnaryOp extends exprType {
    private unaryopType op;
    public unaryopType getOp() {
        return op;
    }
    public void setOp(unaryopType op) {
        this.op = op;
    }

    private exprType operand;
    public exprType getOperand() {
        return operand;
    }
    public void setOperand(exprType operand) {
        this.operand = operand;
    }


    private final static String[] fields = new String[] {"op", "operand"};
    public String[] get_fields() { return fields; }

    public UnaryOp(unaryopType op, exprType operand) {
        this.op = op;
        this.operand = operand;
        addChild(operand);
    }

    public UnaryOp(Token token, unaryopType op, exprType operand) {
        super(token);
        this.op = op;
        this.operand = operand;
        addChild(operand);
    }

    public UnaryOp(int ttype, Token token, unaryopType op, exprType operand) {
        super(ttype, token);
        this.op = op;
        this.operand = operand;
        addChild(operand);
    }

    public UnaryOp(PythonTree tree, unaryopType op, exprType operand) {
        super(tree);
        this.op = op;
        this.operand = operand;
        addChild(operand);
    }

    public String toString() {
        return "UnaryOp";
    }

    public String toStringTree() {
        StringBuffer sb = new StringBuffer("UnaryOp(");
        sb.append("op=");
        sb.append(dumpThis(op));
        sb.append(",");
        sb.append("operand=");
        sb.append(dumpThis(operand));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitUnaryOp(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (operand != null)
            operand.accept(visitor);
    }

    private int lineno = -1;
    public int getLineno() {
        if (lineno != -1) {
            return lineno;
        }
        return getLine();
    }

    public void setLineno(int num) {
        lineno = num;
    }

    private int col_offset = -1;
    public int getCol_offset() {
        if (col_offset != -1) {
            return col_offset;
        }
        return getCharPositionInLine();
    }

    public void setCol_offset(int num) {
        col_offset = num;
    }

}
