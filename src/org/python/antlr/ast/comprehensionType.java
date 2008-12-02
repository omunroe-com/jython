// Autogenerated AST node
package org.python.antlr.ast;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.python.antlr.PythonTree;
import org.python.antlr.adapter.AstAdapters;
import org.python.core.AstList;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyType;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedSet;
import org.python.expose.ExposedType;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class comprehensionType extends PythonTree {
    public static final PyType TYPE = PyType.fromClass(comprehensionType.class);
    private exprType target;
    public exprType getInternalTarget() {
        return target;
    }
    @ExposedGet(name = "target")
    public PyObject getTarget() {
        return target;
    }
    @ExposedSet(name = "target")
    public void setTarget(PyObject target) {
        this.target = AstAdapters.to_expr(target);
    }

    private exprType iter;
    public exprType getInternalIter() {
        return iter;
    }
    @ExposedGet(name = "iter")
    public PyObject getIter() {
        return iter;
    }
    @ExposedSet(name = "iter")
    public void setIter(PyObject iter) {
        this.iter = AstAdapters.to_expr(iter);
    }

    private java.util.List<exprType> ifs;
    public java.util.List<exprType> getInternalIfs() {
        return ifs;
    }
    @ExposedGet(name = "ifs")
    public PyObject getIfs() {
        return new AstList(ifs, AstAdapters.exprAdapter);
    }
    @ExposedSet(name = "ifs")
    public void setIfs(PyObject ifs) {
        //FJW this.ifs = AstAdapters.to_exprList(ifs);
    }


    private final static String[] fields = new String[] {"target", "iter",
                                                          "ifs"};
    public String[] get_fields() { return fields; }

    public comprehensionType() {
        this(TYPE);
    }
    public comprehensionType(PyType subType) {
        super(subType);
    }
    @ExposedNew
    @ExposedMethod
    public void Module___init__(PyObject[] args, String[] keywords) {}
    public comprehensionType(PyObject target, PyObject iter, PyObject ifs) {
        setTarget(target);
        setIter(iter);
        setIfs(ifs);
    }

    public comprehensionType(Token token, exprType target, exprType iter,
    java.util.List<exprType> ifs) {
        super(token);
        this.target = target;
        addChild(target);
        this.iter = iter;
        addChild(iter);
        this.ifs = ifs;
        if (ifs == null) {
            this.ifs = new ArrayList<exprType>();
        }
        for(PythonTree t : this.ifs) {
            addChild(t);
        }
    }

    public comprehensionType(Integer ttype, Token token, exprType target,
    exprType iter, java.util.List<exprType> ifs) {
        super(ttype, token);
        this.target = target;
        addChild(target);
        this.iter = iter;
        addChild(iter);
        this.ifs = ifs;
        if (ifs == null) {
            this.ifs = new ArrayList<exprType>();
        }
        for(PythonTree t : this.ifs) {
            addChild(t);
        }
    }

    public comprehensionType(PythonTree tree, exprType target, exprType iter,
    java.util.List<exprType> ifs) {
        super(tree);
        this.target = target;
        addChild(target);
        this.iter = iter;
        addChild(iter);
        this.ifs = ifs;
        if (ifs == null) {
            this.ifs = new ArrayList<exprType>();
        }
        for(PythonTree t : this.ifs) {
            addChild(t);
        }
    }

    public String toString() {
        return "comprehension";
    }

    public String toStringTree() {
        StringBuffer sb = new StringBuffer("comprehension(");
        sb.append("target=");
        sb.append(dumpThis(target));
        sb.append(",");
        sb.append("iter=");
        sb.append(dumpThis(iter));
        sb.append(",");
        sb.append("ifs=");
        sb.append(dumpThis(ifs));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        traverse(visitor);
        return null;
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (target != null)
            target.accept(visitor);
        if (iter != null)
            iter.accept(visitor);
        if (ifs != null) {
            for (PythonTree t : ifs) {
                if (t != null)
                    t.accept(visitor);
            }
        }
    }

}
