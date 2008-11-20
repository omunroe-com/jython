// Autogenerated AST node
package org.python.antlr.ast;
import org.python.antlr.PythonTree;
import org.python.antlr.ListWrapper;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import java.io.DataOutputStream;
import java.io.IOException;

public class AugAssign extends stmtType {
    private exprType target;
    public exprType getTarget() {
        return target;
    }
    public void setTarget(exprType target) {
        this.target = target;
    }

    private operatorType op;
    public operatorType getOp() {
        return op;
    }
    public void setOp(operatorType op) {
        this.op = op;
    }

    private exprType value;
    public exprType getValue() {
        return value;
    }
    public void setValue(exprType value) {
        this.value = value;
    }


    private final static String[] fields = new String[] {"target", "op",
                                                          "value"};
    public String[] get_fields() { return fields; }

    public AugAssign(exprType target, operatorType op, exprType value) {
        this.target = target;
        addChild(target);
        this.op = op;
        this.value = value;
        addChild(value);
    }

    public AugAssign(Token token, exprType target, operatorType op, exprType
    value) {
        super(token);
        this.target = target;
        addChild(target);
        this.op = op;
        this.value = value;
        addChild(value);
    }

    public AugAssign(int ttype, Token token, exprType target, operatorType op,
    exprType value) {
        super(ttype, token);
        this.target = target;
        addChild(target);
        this.op = op;
        this.value = value;
        addChild(value);
    }

    public AugAssign(PythonTree tree, exprType target, operatorType op,
    exprType value) {
        super(tree);
        this.target = target;
        addChild(target);
        this.op = op;
        this.value = value;
        addChild(value);
    }

    public String toString() {
        return "AugAssign";
    }

    public String toStringTree() {
        StringBuffer sb = new StringBuffer("AugAssign(");
        sb.append("target=");
        sb.append(dumpThis(target));
        sb.append(",");
        sb.append("op=");
        sb.append(dumpThis(op));
        sb.append(",");
        sb.append("value=");
        sb.append(dumpThis(value));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitAugAssign(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (target != null)
            target.accept(visitor);
        if (value != null)
            value.accept(visitor);
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
