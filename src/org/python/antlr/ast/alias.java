// Autogenerated AST node
package org.python.antlr.ast;

import org.antlr.runtime.Token;
import org.python.antlr.AST;
import org.python.antlr.PythonTree;
import org.python.antlr.adapter.AstAdapters;
import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.core.PyType;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedSet;
import org.python.expose.ExposedType;

@ExposedType(name = "_ast.alias", base = AST.class)
public class alias extends PythonTree {
    public static final PyType TYPE = PyType.fromClass(alias.class);
    private String name;
    public String getInternalName() {
        return name;
    }
    @ExposedGet(name = "name")
    public PyObject getName() {
        if (name == null) return Py.None;
        return new PyString(name);
    }
    @ExposedSet(name = "name")
    public void setName(PyObject name) {
        this.name = AstAdapters.py2identifier(name);
    }

    private String asname;
    public String getInternalAsname() {
        return asname;
    }
    @ExposedGet(name = "asname")
    public PyObject getAsname() {
        if (asname == null) return Py.None;
        return new PyString(asname);
    }
    @ExposedSet(name = "asname")
    public void setAsname(PyObject asname) {
        this.asname = AstAdapters.py2identifier(asname);
    }


    private final static PyString[] fields =
    new PyString[] {new PyString("name"), new PyString("asname")};
    @ExposedGet(name = "_fields")
    public PyString[] get_fields() { return fields; }

    private final static PyString[] attributes = new PyString[0];
    @ExposedGet(name = "_attributes")
    public PyString[] get_attributes() { return attributes; }

    public alias(PyType subType) {
        super(subType);
    }
    public alias() {
        this(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void alias___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("alias", args, keywords, new String[]
            {"name", "asname"}, 2, true);
        setName(ap.getPyObject(0, Py.None));
        setAsname(ap.getPyObject(1, Py.None));
    }

    public alias(PyObject name, PyObject asname) {
        setName(name);
        setAsname(asname);
    }

    public alias(Token token, String name, String asname) {
        super(token);
        this.name = name;
        this.asname = asname;
    }

    public alias(Integer ttype, Token token, String name, String asname) {
        super(ttype, token);
        this.name = name;
        this.asname = asname;
    }

    public alias(PythonTree tree, String name, String asname) {
        super(tree);
        this.name = name;
        this.asname = asname;
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "alias";
    }

    public String toStringTree() {
        StringBuffer sb = new StringBuffer("alias(");
        sb.append("name=");
        sb.append(dumpThis(name));
        sb.append(",");
        sb.append("asname=");
        sb.append(dumpThis(asname));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        traverse(visitor);
        return null;
    }

    public void traverse(VisitorIF<?> visitor) throws Exception {
    }

    public PyObject __dict__;

    @Override
    public PyObject fastGetDict() {
        ensureDict();
        return __dict__;
    }

    @ExposedGet(name = "__dict__")
    public PyObject getDict() {
        return fastGetDict();
    }

    private void ensureDict() {
        if (__dict__ == null) {
            __dict__ = new PyStringMap();
        }
    }

    // Support for indexer below

    private java.util.List<Name> nameNodes;
    public java.util.List<Name> getInternalNameNodes() {
        return nameNodes;
    }
    private Name asnameNode;
    public Name getInternalAsnameNode() {
        return asnameNode;
    }
    // [import] name [as asname]
    public alias(Name name, Name asname) {
        this(java.util.Arrays.asList(new Name[]{name}), asname);
    }

    // [import] ...foo.bar.baz [as asname]
    public alias(java.util.List<Name> nameNodes, Name asname) {
        this.nameNodes = nameNodes;
        this.name = dottedNameListToString(nameNodes);
        if (asname != null) {
            this.asnameNode = asname;
            this.asname = asname.getInternalId();
        }
    }
    // End indexer support
}
