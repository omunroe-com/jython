// Autogenerated AST node
package org.python.antlr.ast;
import org.python.antlr.PythonTree;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import java.io.DataOutputStream;
import java.io.IOException;

public class Module extends modType {
    public stmtType[] body;

    public static final String[] _fields = new String[] {"body"};

    public Module(Token token, stmtType[] body) {
        super(token);
        this.body = body;
        if (body != null) {
            for(int ibody=0;ibody<body.length;ibody++) {
                addChild(body[ibody]);
            }
        }
    }

    public Module(int ttype, Token token, stmtType[] body) {
        super(ttype, token);
        this.body = body;
        if (body != null) {
            for(int ibody=0;ibody<body.length;ibody++) {
                addChild(body[ibody]);
            }
        }
    }

    public Module(PythonTree tree, stmtType[] body) {
        super(tree);
        this.body = body;
        if (body != null) {
            for(int ibody=0;ibody<body.length;ibody++) {
                addChild(body[ibody]);
            }
        }
    }

    public String toString() {
        return "Module";
    }

    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Module[");
        sb.append("body=");
        sb.append(this.body);
        sb.append("]");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitModule(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (body != null) {
            for (int i = 0; i < body.length; i++) {
                if (body[i] != null)
                    body[i].accept(visitor);
            }
        }
    }

}
