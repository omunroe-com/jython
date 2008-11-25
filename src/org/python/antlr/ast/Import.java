// Autogenerated AST node
package org.python.antlr.ast;
import java.util.ArrayList;
import org.python.antlr.AstAdapters;
import org.python.antlr.PythonTree;
import org.python.antlr.ListWrapper;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import java.io.DataOutputStream;
import java.io.IOException;

public class Import extends stmtType {
    private java.util.List<aliasType> names;
    public java.util.List<aliasType> getInternalNames() {
        return names;
    }
    public Object getNames() {
        return new ListWrapper(names);
    }
    public void setNames(Object names) {
        this.names = AstAdapters.to_aliasList(names);
    }


    private final static String[] fields = new String[] {"names"};
    public String[] get_fields() { return fields; }

    public Import() {}
    public Import(Object names) {
        setNames(names);
    }

    public Import(Token token, java.util.List<aliasType> names) {
        super(token);
        this.names = names;
        if (names == null) {
            this.names = new ArrayList<aliasType>();
        }
        for(PythonTree t : this.names) {
            addChild(t);
        }
    }

    public Import(Integer ttype, Token token, java.util.List<aliasType> names) {
        super(ttype, token);
        this.names = names;
        if (names == null) {
            this.names = new ArrayList<aliasType>();
        }
        for(PythonTree t : this.names) {
            addChild(t);
        }
    }

    public Import(PythonTree tree, java.util.List<aliasType> names) {
        super(tree);
        this.names = names;
        if (names == null) {
            this.names = new ArrayList<aliasType>();
        }
        for(PythonTree t : this.names) {
            addChild(t);
        }
    }

    public String toString() {
        return "Import";
    }

    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Import(");
        sb.append("names=");
        sb.append(dumpThis(names));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitImport(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (names != null) {
            for (PythonTree t : names) {
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
