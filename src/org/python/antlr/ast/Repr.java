// Autogenerated AST node
package org.python.antlr.ast;
import org.python.antlr.PythonTree;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import java.io.DataOutputStream;
import java.io.IOException;

public class Repr extends exprType {
    private exprType value;
    public exprType getValue() {
        return value;
    }
    public void setValue(exprType value) {
        this.value = value;
    }


    private final static String[] fields = new String[] {"value"};
    public String[] get_fields() { return fields; }

    public Repr(exprType value) {
        this.value = value;
        addChild(value);
    }

    public Repr(Token token, exprType value) {
        super(token);
        this.value = value;
        addChild(value);
    }

    public Repr(int ttype, Token token, exprType value) {
        super(ttype, token);
        this.value = value;
        addChild(value);
    }

    public Repr(PythonTree tree, exprType value) {
        super(tree);
        this.value = value;
        addChild(value);
    }

    public String toString() {
        return "Repr";
    }

    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Repr(");
        sb.append("value=");
        sb.append(dumpThis(value));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitRepr(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
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
