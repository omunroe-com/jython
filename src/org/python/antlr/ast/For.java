// Autogenerated AST node
package org.python.antlr.ast;
import org.python.antlr.PythonTree;
import org.python.antlr.ListWrapper;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import java.io.DataOutputStream;
import java.io.IOException;

public class For extends stmtType {
    private exprType target;
    public exprType getTarget() {
        return target;
    }
    public void setTarget(exprType target) {
        this.target = target;
    }

    private exprType iter;
    public exprType getIter() {
        return iter;
    }
    public void setIter(exprType iter) {
        this.iter = iter;
    }

    private ListWrapper<stmtType> body;
    public ListWrapper<stmtType> getBody() {
        return body;
    }
    public void setBody(java.util.List<stmtType> body) {
        this.body = new ListWrapper<stmtType>(body);
    }

    private ListWrapper<stmtType> orelse;
    public ListWrapper<stmtType> getOrelse() {
        return orelse;
    }
    public void setOrelse(java.util.List<stmtType> orelse) {
        this.orelse = new ListWrapper<stmtType>(orelse);
    }


    private final static String[] fields = new String[] {"target", "iter",
                                                          "body", "orelse"};
    public String[] get_fields() { return fields; }

    public For(exprType target, exprType iter, java.util.List<stmtType> body,
    java.util.List<stmtType> orelse) {
        this.target = target;
        addChild(target);
        this.iter = iter;
        addChild(iter);
        this.body = body;
        if (body != null) {
            for(PythonTree t : body) {
                addChild(t);
            }
        }
        this.orelse = orelse;
        if (orelse != null) {
            for(PythonTree t : orelse) {
                addChild(t);
            }
        }
    }

    public For(Token token, exprType target, exprType iter,
    java.util.List<stmtType> body, java.util.List<stmtType> orelse) {
        super(token);
        this.target = target;
        addChild(target);
        this.iter = iter;
        addChild(iter);
        this.body = body;
        if (body != null) {
            for(PythonTree t : body) {
                addChild(t);
            }
        }
        this.orelse = orelse;
        if (orelse != null) {
            for(PythonTree t : orelse) {
                addChild(t);
            }
        }
    }

    public For(int ttype, Token token, exprType target, exprType iter,
    java.util.List<stmtType> body, java.util.List<stmtType> orelse) {
        super(ttype, token);
        this.target = target;
        addChild(target);
        this.iter = iter;
        addChild(iter);
        this.body = body;
        if (body != null) {
            for(PythonTree t : body) {
                addChild(t);
            }
        }
        this.orelse = orelse;
        if (orelse != null) {
            for(PythonTree t : orelse) {
                addChild(t);
            }
        }
    }

    public For(PythonTree tree, exprType target, exprType iter,
    java.util.List<stmtType> body, java.util.List<stmtType> orelse) {
        super(tree);
        this.target = target;
        addChild(target);
        this.iter = iter;
        addChild(iter);
        this.body = body;
        if (body != null) {
            for(PythonTree t : body) {
                addChild(t);
            }
        }
        this.orelse = orelse;
        if (orelse != null) {
            for(PythonTree t : orelse) {
                addChild(t);
            }
        }
    }

    public String toString() {
        return "For";
    }

    public String toStringTree() {
        StringBuffer sb = new StringBuffer("For(");
        sb.append("target=");
        sb.append(dumpThis(target));
        sb.append(",");
        sb.append("iter=");
        sb.append(dumpThis(iter));
        sb.append(",");
        sb.append("body=");
        sb.append(dumpThis(body));
        sb.append(",");
        sb.append("orelse=");
        sb.append(dumpThis(orelse));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitFor(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (target != null)
            target.accept(visitor);
        if (iter != null)
            iter.accept(visitor);
        if (body != null) {
            for (PythonTree t : body) {
                if (t != null)
                    t.accept(visitor);
            }
        }
        if (orelse != null) {
            for (PythonTree t : orelse) {
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
