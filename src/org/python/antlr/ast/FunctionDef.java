// Autogenerated AST node
package org.python.antlr.ast;
import org.python.antlr.PythonTree;
import org.python.antlr.ListWrapper;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import java.io.DataOutputStream;
import java.io.IOException;

public class FunctionDef extends stmtType {
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    private argumentsType args;
    public argumentsType getArgs() {
        return args;
    }
    public void setArgs(argumentsType args) {
        this.args = args;
    }

    private ListWrapper<stmtType> body;
    public ListWrapper<stmtType> getBody() {
        return body;
    }
    public void setBody(java.util.List<stmtType> body) {
        this.body = new ListWrapper<stmtType>(body);
    }

    private ListWrapper<exprType> decorators;
    public ListWrapper<exprType> getDecorators() {
        return decorators;
    }
    public void setDecorators(java.util.List<exprType> decorators) {
        this.decorators = new ListWrapper<exprType>(decorators);
    }


    private final static String[] fields = new String[] {"name", "args",
                                                          "body", "decorators"};
    public String[] get_fields() { return fields; }

    public FunctionDef(String name, argumentsType args,
    java.util.List<stmtType> body, java.util.List<exprType> decorators) {
        this.name = name;
        this.args = args;
        this.body = body;
        if (body != null) {
            for(PythonTree t : body) {
                addChild(t);
            }
        }
        this.decorators = decorators;
        if (decorators != null) {
            for(PythonTree t : decorators) {
                addChild(t);
            }
        }
    }

    public FunctionDef(Token token, String name, argumentsType args,
    java.util.List<stmtType> body, java.util.List<exprType> decorators) {
        super(token);
        this.name = name;
        this.args = args;
        this.body = body;
        if (body != null) {
            for(PythonTree t : body) {
                addChild(t);
            }
        }
        this.decorators = decorators;
        if (decorators != null) {
            for(PythonTree t : decorators) {
                addChild(t);
            }
        }
    }

    public FunctionDef(int ttype, Token token, String name, argumentsType args,
    java.util.List<stmtType> body, java.util.List<exprType> decorators) {
        super(ttype, token);
        this.name = name;
        this.args = args;
        this.body = body;
        if (body != null) {
            for(PythonTree t : body) {
                addChild(t);
            }
        }
        this.decorators = decorators;
        if (decorators != null) {
            for(PythonTree t : decorators) {
                addChild(t);
            }
        }
    }

    public FunctionDef(PythonTree tree, String name, argumentsType args,
    java.util.List<stmtType> body, java.util.List<exprType> decorators) {
        super(tree);
        this.name = name;
        this.args = args;
        this.body = body;
        if (body != null) {
            for(PythonTree t : body) {
                addChild(t);
            }
        }
        this.decorators = decorators;
        if (decorators != null) {
            for(PythonTree t : decorators) {
                addChild(t);
            }
        }
    }

    public String toString() {
        return "FunctionDef";
    }

    public String toStringTree() {
        StringBuffer sb = new StringBuffer("FunctionDef(");
        sb.append("name=");
        sb.append(dumpThis(name));
        sb.append(",");
        sb.append("args=");
        sb.append(dumpThis(args));
        sb.append(",");
        sb.append("body=");
        sb.append(dumpThis(body));
        sb.append(",");
        sb.append("decorators=");
        sb.append(dumpThis(decorators));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitFunctionDef(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (args != null)
            args.accept(visitor);
        if (body != null) {
            for (PythonTree t : body) {
                if (t != null)
                    t.accept(visitor);
            }
        }
        if (decorators != null) {
            for (PythonTree t : decorators) {
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
