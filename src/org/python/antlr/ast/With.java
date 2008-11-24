// Autogenerated AST node
package org.python.antlr.ast;
import java.util.ArrayList;
import org.python.antlr.AstAdapter;
import org.python.antlr.PythonTree;
import org.python.antlr.ListWrapper;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import java.io.DataOutputStream;
import java.io.IOException;

public class With extends stmtType {
    private exprType context_expr;
    public exprType getInternalContext_expr() {
        return context_expr;
    }
    public Object getContext_expr() {
        return context_expr;
    }
    public void setContext_expr(Object context_expr) {
        this.context_expr = AstAdapter.to_expr(context_expr);
    }

    private exprType optional_vars;
    public exprType getInternalOptional_vars() {
        return optional_vars;
    }
    public Object getOptional_vars() {
        return optional_vars;
    }
    public void setOptional_vars(Object optional_vars) {
        this.optional_vars = AstAdapter.to_expr(optional_vars);
    }

    private java.util.List<stmtType> body;
    public java.util.List<stmtType> getInternalBody() {
        return body;
    }
    public Object getBody() {
        return new ListWrapper(body);
    }
    public void setBody(Object body) {
        this.body = AstAdapter.to_stmtList(body);
    }


    private final static String[] fields = new String[] {"context_expr",
                                                          "optional_vars",
                                                          "body"};
    public String[] get_fields() { return fields; }

    public With() {}
    public With(Object context_expr, Object optional_vars, Object body) {
        setContext_expr(context_expr);
        setOptional_vars(optional_vars);
        setBody(body);
    }

    public With(Token token, exprType context_expr, exprType optional_vars,
    java.util.List<stmtType> body) {
        super(token);
        this.context_expr = context_expr;
        addChild(context_expr);
        this.optional_vars = optional_vars;
        addChild(optional_vars);
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<stmtType>();
        }
        for(PythonTree t : this.body) {
            addChild(t);
        }
    }

    public With(Integer ttype, Token token, exprType context_expr, exprType
    optional_vars, java.util.List<stmtType> body) {
        super(ttype, token);
        this.context_expr = context_expr;
        addChild(context_expr);
        this.optional_vars = optional_vars;
        addChild(optional_vars);
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<stmtType>();
        }
        for(PythonTree t : this.body) {
            addChild(t);
        }
    }

    public With(PythonTree tree, exprType context_expr, exprType optional_vars,
    java.util.List<stmtType> body) {
        super(tree);
        this.context_expr = context_expr;
        addChild(context_expr);
        this.optional_vars = optional_vars;
        addChild(optional_vars);
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<stmtType>();
        }
        for(PythonTree t : this.body) {
            addChild(t);
        }
    }

    public String toString() {
        return "With";
    }

    public String toStringTree() {
        StringBuffer sb = new StringBuffer("With(");
        sb.append("context_expr=");
        sb.append(dumpThis(context_expr));
        sb.append(",");
        sb.append("optional_vars=");
        sb.append(dumpThis(optional_vars));
        sb.append(",");
        sb.append("body=");
        sb.append(dumpThis(body));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitWith(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (context_expr != null)
            context_expr.accept(visitor);
        if (optional_vars != null)
            optional_vars.accept(visitor);
        if (body != null) {
            for (PythonTree t : body) {
                if (t != null)
                    t.accept(visitor);
            }
        }
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
