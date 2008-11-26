// Autogenerated AST node
package org.python.antlr.ast;
import java.util.ArrayList;
import org.python.antlr.PythonTree;
import org.python.antlr.adapter.AstAdapters;
import org.python.antlr.adapter.ListWrapper;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import java.io.DataOutputStream;
import java.io.IOException;

public class Assign extends stmtType {
    private java.util.List<exprType> targets;
    public java.util.List<exprType> getInternalTargets() {
        return targets;
    }
    public Object getTargets() {
        return new ListWrapper(targets, AstAdapters.exprAdapter);
    }
    public void setTargets(Object targets) {
        this.targets = AstAdapters.to_exprList(targets);
    }

    private exprType value;
    public exprType getInternalValue() {
        return value;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = AstAdapters.to_expr(value);
    }


    private final static String[] fields = new String[] {"targets", "value"};
    public String[] get_fields() { return fields; }

    public Assign() {}
    public Assign(Object targets, Object value) {
        setTargets(targets);
        setValue(value);
    }

    public Assign(Token token, java.util.List<exprType> targets, exprType
    value) {
        super(token);
        this.targets = targets;
        if (targets == null) {
            this.targets = new ArrayList<exprType>();
        }
        for(PythonTree t : this.targets) {
            addChild(t);
        }
        this.value = value;
        addChild(value);
    }

    public Assign(Integer ttype, Token token, java.util.List<exprType> targets,
    exprType value) {
        super(ttype, token);
        this.targets = targets;
        if (targets == null) {
            this.targets = new ArrayList<exprType>();
        }
        for(PythonTree t : this.targets) {
            addChild(t);
        }
        this.value = value;
        addChild(value);
    }

    public Assign(PythonTree tree, java.util.List<exprType> targets, exprType
    value) {
        super(tree);
        this.targets = targets;
        if (targets == null) {
            this.targets = new ArrayList<exprType>();
        }
        for(PythonTree t : this.targets) {
            addChild(t);
        }
        this.value = value;
        addChild(value);
    }

    public String toString() {
        return "Assign";
    }

    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Assign(");
        sb.append("targets=");
        sb.append(dumpThis(targets));
        sb.append(",");
        sb.append("value=");
        sb.append(dumpThis(value));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitAssign(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (targets != null) {
            for (PythonTree t : targets) {
                if (t != null)
                    t.accept(visitor);
            }
        }
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
