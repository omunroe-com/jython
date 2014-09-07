package org.python.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.python.core.stringlib.FieldNameIterator;
import org.python.core.stringlib.MarkupIterator;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedType;
import org.python.expose.MethodType;
import org.python.modules._codecs;
import org.python.util.Generic;

/**
 * a builtin python unicode string.
 */
@ExposedType(name = "unicode", base = PyBaseString.class, doc = BuiltinDocs.unicode_doc)
public class PyUnicode extends PyString implements Iterable {

    /**
     * Nearly every significant method comes in two versions: one applicable when the string
     * contains only basic plane characters, and one that is correct when supplementary characters
     * are also present. Set this constant <code>true</code> to treat all strings as containing
     * supplementary characters, so that these versions will be exercised in tests.
     */
    private static final boolean DEBUG_NON_BMP_METHODS = false;

    public static final PyType TYPE = PyType.fromClass(PyUnicode.class);

    // for PyJavaClass.init()
    public PyUnicode() {
        this(TYPE, "", true);
    }

    /**
     * Construct a PyUnicode interpreting the Java String argument as UTF-16.
     *
     * @param string UTF-16 string encoding the characters (as Java).
     */
    public PyUnicode(String string) {
        this(TYPE, string, false);
    }

    /**
     * Construct a PyUnicode interpreting the Java String argument as UTF-16. If it is known that
     * the string contains no supplementary characters, argument isBasic may be set true by the
     * caller. If it is false, the PyUnicode will scan the string to find out.
     *
     * @param string UTF-16 string encoding the characters (as Java).
     * @param isBasic true if it is known that only BMP characters are present.
     */
    public PyUnicode(String string, boolean isBasic) {
        this(TYPE, string, isBasic);
    }

    public PyUnicode(PyType subtype, String string) {
        this(subtype, string, false);
    }

    public PyUnicode(PyString pystring) {
        this(TYPE, pystring);
    }

    public PyUnicode(PyType subtype, PyString pystring) {
        this(subtype, //
                pystring instanceof PyUnicode ? pystring.string : pystring.decode().toString(), //
                pystring.isBasicPlane());
    }

    public PyUnicode(char c) {
        this(TYPE, String.valueOf(c), true);
    }

    public PyUnicode(int codepoint) {
        this(TYPE, new String(new int[] {codepoint}, 0, 1));
    }

    public PyUnicode(int[] codepoints) {
        this(new String(codepoints, 0, codepoints.length));
    }

    PyUnicode(StringBuilder buffer) {
        this(TYPE, new String(buffer));
    }

    private static StringBuilder fromCodePoints(Iterator<Integer> iter) {
        StringBuilder buffer = new StringBuilder();
        while (iter.hasNext()) {
            buffer.appendCodePoint(iter.next());
        }
        return buffer;
    }

    public PyUnicode(Iterator<Integer> iter) {
        this(fromCodePoints(iter));
    }

    public PyUnicode(Collection<Integer> ucs4) {
        this(ucs4.iterator());
    }

    /**
     * Fundamental all-features constructor on which the others depend. If it is known that the
     * string contains no supplementary characters, argument isBasic may be set true by the caller.
     * If it is false, the PyUnicode will scan the string to find out.
     *
     * @param subtype actual type to create.
     * @param string UTF-16 string encoding the characters (as Java).
     * @param isBasic true if it is known that only BMP characters are present.
     */
    private PyUnicode(PyType subtype, String string, boolean isBasic) {
        super(subtype, string);
        translator = isBasic ? BASIC : this.chooseIndexTranslator();
    }

    @Override
    public int[] toCodePoints() {
        int n = getCodePointCount();
        int[] codePoints = new int[n];
        int i = 0;
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext(); i++) {
            codePoints[i] = iter.next();
        }
        return codePoints;
    }

    // ------------------------------------------------------------------------------------------
    // Index translation for Unicode beyond the BMP
    // ------------------------------------------------------------------------------------------

    /**
     * Index translation between code point index (as seen by Python) and UTF-16 index (as used in
     * the Java String.
     */
    private interface IndexTranslator {

        /** Number of supplementary characters (hence point code length may be found). */
        public int suppCount();

        /** Translate a UTF-16 code unit index to its equivalent code point index. */
        public int codePointIndex(int utf16Index);

        /** Translate a code point index to its equivalent UTF-16 code unit index. */
        public int utf16Index(int codePointIndex);
    }

    /**
     * The instance of index translation in use in this string. It will be set to either
     * {@link #BASIC} or and instance of {@link #Supplementary}.
     */
    private final IndexTranslator translator;

    /**
     * A singleton provides the translation service (which is a pass-through) for all BMP strings.
     */
    static final IndexTranslator BASIC = new IndexTranslator() {

        @Override
        public int suppCount() {
            return 0;
        }

        @Override
        public int codePointIndex(int u) {
            return u;
        }

        @Override
        public int utf16Index(int i) {
            return i;
        }
    };

    /**
     * A class of index translation that uses the features provided by the Java String.
     */
    private class Supplementary implements IndexTranslator {

        /** The number of supplementary character in this string. */
        private final int supp;

        Supplementary(int supp) {
            this.supp = supp;
        }

        @Override
        public int codePointIndex(int u) {
            return string.codePointCount(0, u);
        }

        @Override
        public int utf16Index(int i) {
            return string.offsetByCodePoints(0, i);
        }

        @Override
        public int suppCount() {
            return supp;
        }
    }

    /**
     * Choose an {@link IndexTranslator} implementation for efficient working, according to the
     * contents of the {@link PyString#string}.
     *
     * @return chosen <code>IndexTranslator</code>
     */
    private IndexTranslator chooseIndexTranslator() {
        int n = string.length();
        int s = n - string.codePointCount(0, n);
        if (DEBUG_NON_BMP_METHODS) {
            return new Supplementary(s);
        } else {
            return s == 0 ? BASIC : new Supplementary(s);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * In the <code>PyUnicode</code> version, the arguments are code point indices, such as are
     * received from the Python caller, while the first two elements of the returned array have been
     * translated to UTF-16 indices in the implementation string.
     */
    @Override
    protected int[] translateIndices(PyObject start, PyObject end) {
        int[] indices = super.translateIndices(start, end);
        indices[0] = translator.utf16Index(indices[0]);
        indices[1] = translator.utf16Index(indices[1]);
        // indices[2] and [3] remain Unicode indices (and may be out of bounds) relative to len()
        return indices;
    }

    // ------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * The indices  are code point indices, not UTF-16 (<code>char</code>) indices. For example:
     *
     * <pre>
     * PyUnicode u = new PyUnicode("..\ud800\udc02\ud800\udc03...");
     * // (Python) u = u'..\U00010002\U00010003...'
     *
     * String s = u.substring(2, 4);  // = "\ud800\udc02\ud800\udc03" (Java)
     * </pre>
     */
    @Override
    public String substring(int start, int end) {
        return super.substring(translator.utf16Index(start), translator.utf16Index(end));
    }

    /**
     * Creates a PyUnicode from an already interned String. Just means it won't be reinterned if
     * used in a place that requires interned Strings.
     */
    public static PyUnicode fromInterned(String interned) {
        PyUnicode uni = new PyUnicode(TYPE, interned);
        uni.interned = true;
        return uni;
    }

    /**
     * {@inheritDoc}
     *
     * @return true if the string consists only of BMP characters
     */
    @Override
    public boolean isBasicPlane() {
        if (DEBUG_NON_BMP_METHODS) {
            return false;
        } else {
            return translator.suppCount() == 0;
        }
    }

    public int getCodePointCount() {
        return string.length() - translator.suppCount();
    }

    @ExposedNew
    final static PyObject unicode_new(PyNewWrapper new_, boolean init, PyType subtype,
            PyObject[] args, String[] keywords) {
        ArgParser ap =
                new ArgParser("unicode", args, keywords, new String[] {"string", "encoding",
                        "errors"}, 0);
        PyObject S = ap.getPyObject(0, null);
        String encoding = ap.getString(1, null);
        String errors = ap.getString(2, null);
        if (new_.for_type == subtype) {
            if (S == null) {
                return new PyUnicode("");
            }
            if (S instanceof PyUnicode) {
                return new PyUnicode(((PyUnicode)S).getString());
            }
            if (S instanceof PyString) {
                if (S.getType() != PyString.TYPE && encoding == null && errors == null) {
                    return S.__unicode__();
                }
                PyObject decoded = codecs.decode((PyString)S, encoding, errors);
                if (decoded instanceof PyUnicode) {
                    return new PyUnicode((PyUnicode)decoded);
                } else {
                    throw Py.TypeError("decoder did not return an unicode object (type="
                            + decoded.getType().fastGetName() + ")");
                }
            }
            return S.__unicode__();
        } else {
            if (S == null) {
                return new PyUnicodeDerived(subtype, Py.EmptyString);
            }
            if (S instanceof PyUnicode) {
                return new PyUnicodeDerived(subtype, (PyUnicode)S);
            } else {
                return new PyUnicodeDerived(subtype, S.__str__());
            }
        }
    }

    @Override
    public PyString createInstance(String str) {
        return new PyUnicode(str);
    }

    /**
     * @param string UTF-16 string encoding the characters (as Java).
     * @param isBasic true if it is known that only BMP characters are present.
     */
    @Override
    protected PyString createInstance(String string, boolean isBasic) {
        return new PyUnicode(string, isBasic);
    }

    @Override
    public PyObject __mod__(PyObject other) {
        return unicode___mod__(other);
    }

    @ExposedMethod(doc = BuiltinDocs.unicode___mod___doc)
    final PyObject unicode___mod__(PyObject other) {
        StringFormatter fmt = new StringFormatter(getString(), true);
        return fmt.format(other);
    }

    @Override
    public PyUnicode __unicode__() {
        return this;
    }

    @Override
    public PyString __str__() {
        return unicode___str__();
    }

    @ExposedMethod(doc = BuiltinDocs.unicode___str___doc)
    final PyString unicode___str__() {
        return new PyString(encode());
    }

    @Override
    public int __len__() {
        return unicode___len__();
    }

    @ExposedMethod(doc = BuiltinDocs.unicode___len___doc)
    final int unicode___len__() {
        return getCodePointCount();
    }

    @Override
    public PyString __repr__() {
        return unicode___repr__();
    }

    @ExposedMethod(doc = BuiltinDocs.unicode___repr___doc)
    final PyString unicode___repr__() {
        return new PyString("u" + encode_UnicodeEscape(getString(), true));
    }

    @ExposedMethod(doc = BuiltinDocs.unicode___getitem___doc)
    final PyObject unicode___getitem__(PyObject index) {
        return str___getitem__(index);
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.unicode___getslice___doc)
    final PyObject unicode___getslice__(PyObject start, PyObject stop, PyObject step) {
        return seq___getslice__(start, stop, step);
    }

    @Override
    protected PyObject getslice(int start, int stop, int step) {
        if (isBasicPlane()) {
            return super.getslice(start, stop, step);
        }
        if (step > 0 && stop < start) {
            stop = start;
        }

        StringBuilder buffer = new StringBuilder(sliceLength(start, stop, step));
        for (Iterator<Integer> iter = newSubsequenceIterator(start, stop, step); iter.hasNext();) {
            buffer.appendCodePoint(iter.next());
        }
        return createInstance(new String(buffer));
    }

    @ExposedMethod(type = MethodType.CMP, doc = BuiltinDocs.unicode___getslice___doc)
    final int unicode___cmp__(PyObject other) {
        return str___cmp__(other);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.unicode___getslice___doc)
    final PyObject unicode___eq__(PyObject other) {
        return str___eq__(other);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.unicode___getslice___doc)
    final PyObject unicode___ne__(PyObject other) {
        return str___ne__(other);
    }

    @ExposedMethod(doc = BuiltinDocs.unicode___hash___doc)
    final int unicode___hash__() {
        return str___hash__();
    }

    @Override
    protected PyObject pyget(int i) {
        if (isBasicPlane()) {
            return Py.makeCharacter(getString().charAt(i), true);
        }

        int k = 0;
        while (i > 0) {
            int W1 = getString().charAt(k);
            if (W1 >= 0xD800 && W1 < 0xDC00) {
                k += 2;
            } else {
                k += 1;
            }
            i--;
        }
        int codepoint = getString().codePointAt(k);
        return Py.makeCharacter(codepoint, true);
    }

    private class SubsequenceIteratorImpl implements Iterator {

        private int current, k, start, stop, step;

        SubsequenceIteratorImpl(int start, int stop, int step) {
            k = 0;
            current = start;
            this.start = start;
            this.stop = stop;
            this.step = step;
            for (int i = 0; i < start; i++) {
                nextCodePoint();
            }
        }

        SubsequenceIteratorImpl() {
            this(0, getCodePointCount(), 1);
        }

        @Override
        public boolean hasNext() {
            return current < stop;
        }

        @Override
        public Object next() {
            int codePoint = nextCodePoint();
            current += 1;
            for (int j = 1; j < step && hasNext(); j++) {
                nextCodePoint();
                current += 1;
            }
            return codePoint;
        }

        private int nextCodePoint() {
            int U;
            int W1 = getString().charAt(k);
            if (W1 >= 0xD800 && W1 < 0xDC00) {
                int W2 = getString().charAt(k + 1);
                U = (((W1 & 0x3FF) << 10) | (W2 & 0x3FF)) + 0x10000;
                k += 2;
            } else {
                U = W1;
                k += 1;
            }
            return U;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(
                    "Not supported on PyUnicode objects (immutable)");
        }
    }

    private static class SteppedIterator<T> implements Iterator {

        private final Iterator<T> iter;
        private final int step;
        private T lookahead = null;

        public SteppedIterator(int step, Iterator<T> iter) {
            this.iter = iter;
            this.step = step;
            lookahead = advance();
        }

        private T advance() {
            if (iter.hasNext()) {
                T elem = iter.next();
                for (int i = 1; i < step && iter.hasNext(); i++) {
                    iter.next();
                }
                return elem;
            } else {
                return null;
            }
        }

        @Override
        public boolean hasNext() {
            return lookahead != null;
        }

        @Override
        public T next() {
            T old = lookahead;
            if (iter.hasNext()) {
                lookahead = iter.next();
                for (int i = 1; i < step && iter.hasNext(); i++) {
                    iter.next();
                }
            } else {
                lookahead = null;
            }
            return old;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // XXX: Parameterize SubsequenceIteratorImpl and friends (and make them Iterable)
    public Iterator<Integer> newSubsequenceIterator() {
        return new SubsequenceIteratorImpl();
    }

    public Iterator<Integer> newSubsequenceIterator(int start, int stop, int step) {
        if (step < 0) {
            return new SteppedIterator(step * -1, new ReversedIterator(new SubsequenceIteratorImpl(
                    stop + 1, start + 1, 1)));
        } else {
            return new SubsequenceIteratorImpl(start, stop, step);
        }
    }

    /**
     * Helper used many times to "coerce" a method argument into a <code>PyUnicode</code> (which it
     * may already be). A <code>null</code> or incoercible argument will raise a
     * <code>TypeError</code>.
     *
     * @param o the object to coerce
     * @return an equivalent <code>PyUnicode</code> (or o itself)
     */
    private PyUnicode coerceToUnicode(PyObject o) {
        if (o instanceof PyUnicode) {
            return (PyUnicode)o;
        } else if (o instanceof PyString) {
            return new PyUnicode(((PyString)o).getString(), true);
        } else if (o instanceof BufferProtocol) {
            // PyByteArray, PyMemoryView, Py2kBuffer ...
            try (PyBuffer buf = ((BufferProtocol)o).getBuffer(PyBUF.FULL_RO)) {
                return new PyUnicode(buf.toString(), true);
            }
        } else {
            // o is some type not allowed:
            if (o == null) {
                // Do something safe and approximately right
                o = Py.None;
            }
            throw Py.TypeError("coercing to Unicode: need string or buffer, "
                    + o.getType().fastGetName() + " found");
        }
    }

    /**
     * Helper used many times to "coerce" a method argument into a <code>PyUnicode</code> (which it
     * may already be). A <code>null</code> argument or a <code>PyNone</code> causes
     * <code>null</code> to be returned.
     *
     * @param o the object to coerce
     * @return an equivalent <code>PyUnicode</code> (or o itself, or <code>null</code>)
     */
    private PyUnicode coerceToUnicodeOrNull(PyObject o) {
        if (o == null || o == Py.None) {
            return null;
        } else {
            return coerceToUnicode(o);
        }
    }

    @ExposedMethod(doc = BuiltinDocs.unicode___contains___doc)
    final boolean unicode___contains__(PyObject o) {
        return str___contains__(o);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.unicode___getslice___doc)
    final PyObject unicode___mul__(PyObject o) {
        return str___mul__(o);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.unicode___getslice___doc)
    final PyObject unicode___rmul__(PyObject o) {
        return str___rmul__(o);
    }

    @Override
    public PyObject __add__(PyObject other) {
        return unicode___add__(other);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.unicode___getslice___doc)
    final PyObject unicode___add__(PyObject other) {
        PyUnicode otherUnicode;
        if (other instanceof PyUnicode) {
            otherUnicode = (PyUnicode)other;
        } else if (other instanceof PyString) {
            otherUnicode = (PyUnicode)((PyString)other).decode();
        } else {
            return null;
        }
        return new PyUnicode(getString().concat(otherUnicode.getString()));
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_lower_doc)
    final PyObject unicode_lower() {
        return new PyUnicode(str_lower());
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_upper_doc)
    final PyObject unicode_upper() {
        return new PyUnicode(str_upper());
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_title_doc)
    final PyObject unicode_title() {
        if (isBasicPlane()) {
            return new PyUnicode(str_title());
        }
        StringBuilder buffer = new StringBuilder(getString().length());
        boolean previous_is_cased = false;
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext();) {
            int codePoint = iter.next();
            if (previous_is_cased) {
                buffer.appendCodePoint(Character.toLowerCase(codePoint));
            } else {
                buffer.appendCodePoint(Character.toTitleCase(codePoint));
            }

            if (Character.isLowerCase(codePoint) || Character.isUpperCase(codePoint)
                    || Character.isTitleCase(codePoint)) {
                previous_is_cased = true;
            } else {
                previous_is_cased = false;
            }
        }
        return new PyUnicode(buffer);
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_swapcase_doc)
    final PyObject unicode_swapcase() {
        if (isBasicPlane()) {
            return new PyUnicode(str_swapcase());
        }
        StringBuilder buffer = new StringBuilder(getString().length());
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext();) {
            int codePoint = iter.next();
            if (Character.isUpperCase(codePoint)) {
                buffer.appendCodePoint(Character.toLowerCase(codePoint));
            } else if (Character.isLowerCase(codePoint)) {
                buffer.appendCodePoint(Character.toUpperCase(codePoint));
            } else {
                buffer.appendCodePoint(codePoint);
            }
        }
        return new PyUnicode(buffer);
    }

    private static class StripIterator implements Iterator {

        private final Iterator<Integer> iter;
        private int lookahead = -1;

        public StripIterator(PyUnicode sep, Iterator<Integer> iter) {
            this.iter = iter;
            if (sep != null) {
                Set<Integer> sepSet = Generic.set();
                for (Iterator<Integer> sepIter = sep.newSubsequenceIterator(); sepIter.hasNext();) {
                    sepSet.add(sepIter.next());
                }
                while (iter.hasNext()) {
                    int codePoint = iter.next();
                    if (!sepSet.contains(codePoint)) {
                        lookahead = codePoint;
                        return;
                    }
                }
            } else {
                while (iter.hasNext()) {
                    int codePoint = iter.next();
                    if (!Character.isWhitespace(codePoint)) {
                        lookahead = codePoint;
                        return;
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            return lookahead != -1;
        }

        @Override
        public Object next() {
            int old = lookahead;
            if (iter.hasNext()) {
                lookahead = iter.next();
            } else {
                lookahead = -1;
            }
            return old;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // compliance requires that we need to support a bit of inconsistency
    // compared to other coercion used
    /**
     * Helper used in <code>.strip()</code> to "coerce" a method argument into a
     * <code>PyUnicode</code> (which it may already be). A <code>null</code> argument or a
     * <code>PyNone</code> causes <code>null</code> to be returned. A buffer type is not acceptable
     * to (Unicode) <code>.strip()</code>. This is the difference from
     * {@link #coerceToUnicodeOrNull(PyObject)}.
     *
     * @param o the object to coerce
     * @return an equivalent <code>PyUnicode</code> (or o itself, or <code>null</code>)
     */
    private PyUnicode coerceStripSepToUnicode(PyObject o) {
        if (o == null) {
            return null;
        } else if (o instanceof PyUnicode) {
            return (PyUnicode)o;
        } else if (o instanceof PyString) {
            return new PyUnicode(((PyString)o).decode().toString());
        } else if (o == Py.None) {
            return null;
        } else {
            throw Py.TypeError("strip arg must be None, unicode or str");
        }
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.unicode_strip_doc)
    final PyObject unicode_strip(PyObject sepObj) {

        PyUnicode sep = coerceStripSepToUnicode(sepObj);

        if (isBasicPlane()) {
            // this contains only basic plane characters
            if (sep == null) {
                // And we're stripping whitespace, so use the PyString implementation
                return new PyUnicode(_strip());
            } else if (sep.isBasicPlane()) {
                // And the strip characters are basic plane too, so use the PyString implementation
                return new PyUnicode(_strip(sep.getString()));
            }
        }

        // Not basic plane: have to do real Unicode
        return new PyUnicode(new ReversedIterator(new StripIterator(sep, new ReversedIterator(
                new StripIterator(sep, newSubsequenceIterator())))));
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.unicode_lstrip_doc)
    final PyObject unicode_lstrip(PyObject sepObj) {

        PyUnicode sep = coerceStripSepToUnicode(sepObj);

        if (isBasicPlane()) {
            // this contains only basic plane characters
            if (sep == null) {
                // And we're stripping whitespace, so use the PyString implementation
                return new PyUnicode(_lstrip());
            } else if (sep.isBasicPlane()) {
                // And the strip characters are basic plane too, so use the PyString implementation
                return new PyUnicode(_lstrip(sep.getString()));
            }
        }

        // Not basic plane: have to do real Unicode
        return new PyUnicode(new StripIterator(sep, newSubsequenceIterator()));
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.unicode_rstrip_doc)
    final PyObject unicode_rstrip(PyObject sepObj) {

        PyUnicode sep = coerceStripSepToUnicode(sepObj);

        if (isBasicPlane()) {
            // this contains only basic plane characters
            if (sep == null) {
                // And we're stripping whitespace, so use the PyString implementation
                return new PyUnicode(_rstrip());
            } else if (sep.isBasicPlane()) {
                // And the strip characters are basic plane too, so use the PyString implementation
                return new PyUnicode(_rstrip(sep.getString()));
            }
        }

        // Not basic plane: have to do real Unicode
        return new PyUnicode(new ReversedIterator(new StripIterator(sep, new ReversedIterator(
                newSubsequenceIterator()))));
    }

    @Override
    public PyTuple partition(PyObject sep) {
        return unicode_partition(sep);
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_partition_doc)
    final PyTuple unicode_partition(PyObject sep) {
        return unicodePartition(coerceToUnicode(sep));
    }

    private abstract class SplitIterator implements Iterator {

        protected final int maxsplit;
        protected final Iterator<Integer> iter = newSubsequenceIterator();
        protected final LinkedList<Integer> lookahead = new LinkedList<Integer>();
        protected int numSplits = 0;
        protected boolean completeSeparator = false;

        SplitIterator(int maxsplit) {
            this.maxsplit = maxsplit;
        }

        @Override
        public boolean hasNext() {
            return lookahead.peek() != null
                    || (iter.hasNext() && (maxsplit == -1 || numSplits <= maxsplit));
        }

        protected void addLookahead(StringBuilder buffer) {
            for (int codepoint : lookahead) {
                buffer.appendCodePoint(codepoint);
            }
            lookahead.clear();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean getEndsWithSeparator() {
            return completeSeparator && !hasNext();
        }
    }

    private class WhitespaceSplitIterator extends SplitIterator {

        WhitespaceSplitIterator(int maxsplit) {
            super(maxsplit);
        }

        @Override
        public PyUnicode next() {
            StringBuilder buffer = new StringBuilder();

            addLookahead(buffer);
            if (numSplits == maxsplit) {
                while (iter.hasNext()) {
                    buffer.appendCodePoint(iter.next());
                }
                return new PyUnicode(buffer);
            }

            boolean inSeparator = false;
            boolean atBeginning = numSplits == 0;

            while (iter.hasNext()) {
                int codepoint = iter.next();
                if (Character.isWhitespace(codepoint)) {
                    completeSeparator = true;
                    if (!atBeginning) {
                        inSeparator = true;
                    }
                } else if (!inSeparator) {
                    completeSeparator = false;
                    buffer.appendCodePoint(codepoint);
                } else {
                    completeSeparator = false;
                    lookahead.add(codepoint);
                    break;
                }
                atBeginning = false;
            }
            numSplits++;
            return new PyUnicode(buffer);
        }
    }

    private static class PeekIterator<T> implements Iterator {

        private T lookahead = null;
        private final Iterator<T> iter;

        public PeekIterator(Iterator<T> iter) {
            this.iter = iter;
            next();
        }

        public T peek() {
            return lookahead;
        }

        @Override
        public boolean hasNext() {
            return lookahead != null;
        }

        @Override
        public T next() {
            T peeked = lookahead;
            lookahead = iter.hasNext() ? iter.next() : null;
            return peeked;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class ReversedIterator<T> implements Iterator {

        private final List<T> reversed = Generic.list();
        private final Iterator<T> iter;

        ReversedIterator(Iterator<T> iter) {
            while (iter.hasNext()) {
                reversed.add(iter.next());
            }
            Collections.reverse(reversed);
            this.iter = reversed.iterator();
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public T next() {
            return iter.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class LineSplitIterator implements Iterator {

        private final PeekIterator<Integer> iter = new PeekIterator(newSubsequenceIterator());
        private final boolean keepends;

        LineSplitIterator(boolean keepends) {
            this.keepends = keepends;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public Object next() {
            StringBuilder buffer = new StringBuilder();
            while (iter.hasNext()) {
                int codepoint = iter.next();
                if (codepoint == '\r' && iter.peek() != null && iter.peek() == '\n') {
                    if (keepends) {
                        buffer.appendCodePoint(codepoint);
                        buffer.appendCodePoint(iter.next());
                    } else {
                        iter.next();
                    }
                    break;
                } else if (codepoint == '\n' || codepoint == '\r'
                        || Character.getType(codepoint) == Character.LINE_SEPARATOR) {
                    if (keepends) {
                        buffer.appendCodePoint(codepoint);
                    }
                    break;
                } else {
                    buffer.appendCodePoint(codepoint);
                }
            }
            return new PyUnicode(buffer);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class SepSplitIterator extends SplitIterator {

        private final PyUnicode sep;

        SepSplitIterator(PyUnicode sep, int maxsplit) {
            super(maxsplit);
            this.sep = sep;
        }

        @Override
        public PyUnicode next() {
            StringBuilder buffer = new StringBuilder();

            addLookahead(buffer);
            if (numSplits == maxsplit) {
                while (iter.hasNext()) {
                    buffer.appendCodePoint(iter.next());
                }
                return new PyUnicode(buffer);
            }

            boolean inSeparator = true;
            while (iter.hasNext()) {
                // TODO: should cache the first codepoint
                inSeparator = true;
                for (Iterator<Integer> sepIter = sep.newSubsequenceIterator(); sepIter.hasNext();) {
                    int codepoint = iter.next();
                    if (codepoint != sepIter.next()) {
                        addLookahead(buffer);
                        buffer.appendCodePoint(codepoint);
                        inSeparator = false;
                        break;
                    } else {
                        lookahead.add(codepoint);
                    }
                }

                if (inSeparator) {
                    lookahead.clear();
                    break;
                }
            }

            numSplits++;
            completeSeparator = inSeparator;
            return new PyUnicode(buffer);
        }
    }

    private SplitIterator newSplitIterator(PyUnicode sep, int maxsplit) {
        if (sep == null) {
            return new WhitespaceSplitIterator(maxsplit);
        } else if (sep.getCodePointCount() == 0) {
            throw Py.ValueError("empty separator");
        } else {
            return new SepSplitIterator(sep, maxsplit);
        }
    }

    @Override
    public PyTuple rpartition(PyObject sep) {
        return unicode_rpartition(sep);
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_rpartition_doc)
    final PyTuple unicode_rpartition(PyObject sep) {
        return unicodeRpartition(coerceToUnicode(sep));
    }

    @ExposedMethod(defaults = {"null", "-1"}, doc = BuiltinDocs.unicode_split_doc)
    final PyList unicode_split(PyObject sepObj, int maxsplit) {
        PyUnicode sep = coerceToUnicodeOrNull(sepObj);
        if (sep != null) {
            return _split(sep.getString(), maxsplit);
        } else {
            return _split(null, maxsplit);
        }
    }

    @ExposedMethod(defaults = {"null", "-1"}, doc = BuiltinDocs.unicode_rsplit_doc)
    final PyList unicode_rsplit(PyObject sepObj, int maxsplit) {
        PyUnicode sep = coerceToUnicodeOrNull(sepObj);
        if (sep != null) {
            return _rsplit(sep.getString(), maxsplit);
        } else {
            return _rsplit(null, maxsplit);
        }
    }

    @ExposedMethod(defaults = "false", doc = BuiltinDocs.unicode___getslice___doc)
    final PyList unicode_splitlines(boolean keepends) {
        if (isBasicPlane()) {
            return str_splitlines(keepends);
        }
        return new PyList(new LineSplitIterator(keepends));

    }

    @Override
    protected PyString fromSubstring(int begin, int end) {
        assert (isBasicPlane()); // can only be used on a codepath from str_ equivalents
        return new PyUnicode(getString().substring(begin, end), true);
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.unicode_index_doc)
    final int unicode_index(PyObject subObj, PyObject start, PyObject end) {
        final PyUnicode sub = coerceToUnicode(subObj);
        // Now use the mechanics of the PyString on the UTF-16 of the PyUnicode.
        return checkIndex(_find(sub.getString(), start, end));
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.unicode_index_doc)
    final int unicode_rindex(PyObject subObj, PyObject start, PyObject end) {
        final PyUnicode sub = coerceToUnicode(subObj);
        // Now use the mechanics of the PyString on the UTF-16 of the PyUnicode.
        return checkIndex(_rfind(sub.getString(), start, end));
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.unicode_count_doc)
    final int unicode_count(PyObject subObj, PyObject start, PyObject end) {
        final PyUnicode sub = coerceToUnicode(subObj);
        if (isBasicPlane()) {
            return _count(sub.getString(), start, end);
        }
        int[] indices = super.translateIndices(start, end); // do not convert to utf-16 indices.
        int count = 0;
        for (Iterator<Integer> mainIter = newSubsequenceIterator(indices[0], indices[1], 1); mainIter
                .hasNext();) {
            int matched = sub.getCodePointCount();
            for (Iterator<Integer> subIter = sub.newSubsequenceIterator(); mainIter.hasNext()
                    && subIter.hasNext();) {
                if (mainIter.next() != subIter.next()) {
                    break;
                }
                matched--;

            }
            if (matched == 0) {
                count++;
            }
        }
        return count;
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.unicode_find_doc)
    final int unicode_find(PyObject subObj, PyObject start, PyObject end) {
        int found = _find(coerceToUnicode(subObj).getString(), start, end);
        return found < 0 ? -1 : translator.codePointIndex(found);
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.unicode_rfind_doc)
    final int unicode_rfind(PyObject subObj, PyObject start, PyObject end) {
        int found = _rfind(coerceToUnicode(subObj).getString(), start, end);
        return found < 0 ? -1 : translator.codePointIndex(found);
    }

    private static String padding(int n, int pad) {
        StringBuilder buffer = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            buffer.appendCodePoint(pad);
        }
        return buffer.toString();
    }

    private static int parse_fillchar(String function, String fillchar) {
        if (fillchar == null) {
            return ' ';
        }
        if (fillchar.codePointCount(0, fillchar.length()) != 1) {
            throw Py.TypeError(function + "() argument 2 must be char, not str");
        }
        return fillchar.codePointAt(0);
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.unicode___getslice___doc)
    final PyObject unicode_ljust(int width, String padding) {
        int n = width - getCodePointCount();
        if (n <= 0) {
            return new PyUnicode(getString());
        } else {
            return new PyUnicode(getString() + padding(n, parse_fillchar("ljust", padding)));
        }
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.unicode___getslice___doc)
    final PyObject unicode_rjust(int width, String padding) {
        int n = width - getCodePointCount();
        if (n <= 0) {
            return new PyUnicode(getString());
        } else {
            return new PyUnicode(padding(n, parse_fillchar("ljust", padding)) + getString());
        }
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.unicode___getslice___doc)
    final PyObject unicode_center(int width, String padding) {
        int n = width - getCodePointCount();
        if (n <= 0) {
            return new PyUnicode(getString());
        }
        int half = n / 2;
        if (n % 2 > 0 && width % 2 > 0) {
            half += 1;
        }
        int pad = parse_fillchar("center", padding);
        return new PyUnicode(padding(half, pad) + getString() + padding(n - half, pad));
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_zfill_doc)
    final PyObject unicode_zfill(int width) {
        int n = getCodePointCount();
        if (n >= width) {
            return new PyUnicode(getString());
        }
        if (isBasicPlane()) {
            return new PyUnicode(str_zfill(width));
        }
        StringBuilder buffer = new StringBuilder(width);
        int nzeros = width - n;
        boolean first = true;
        boolean leadingSign = false;
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext();) {
            int codePoint = iter.next();
            if (first) {
                first = false;
                if (codePoint == '+' || codePoint == '-') {
                    buffer.appendCodePoint(codePoint);
                    leadingSign = true;
                }
                for (int i = 0; i < nzeros; i++) {
                    buffer.appendCodePoint('0');
                }
                if (!leadingSign) {
                    buffer.appendCodePoint(codePoint);
                }
            } else {
                buffer.appendCodePoint(codePoint);
            }
        }
        if (first) {
            for (int i = 0; i < nzeros; i++) {
                buffer.appendCodePoint('0');
            }
        }
        return new PyUnicode(buffer);
    }

    @ExposedMethod(defaults = "8", doc = BuiltinDocs.unicode___getslice___doc)
    final PyObject unicode_expandtabs(int tabsize) {
        return new PyUnicode(str_expandtabs(tabsize));
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_capitalize_doc)
    final PyObject unicode_capitalize() {
        if (getString().length() == 0) {
            return this;
        }
        if (isBasicPlane()) {
            return new PyUnicode(str_capitalize());
        }
        StringBuilder buffer = new StringBuilder(getString().length());
        boolean first = true;
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext();) {
            if (first) {
                buffer.appendCodePoint(Character.toUpperCase(iter.next()));
                first = false;
            } else {
                buffer.appendCodePoint(Character.toLowerCase(iter.next()));
            }
        }
        return new PyUnicode(buffer);
    }

    @ExposedMethod(defaults = "-1", doc = BuiltinDocs.unicode_replace_doc)
    final PyString unicode_replace(PyObject oldPieceObj, PyObject newPieceObj, int count) {

        // Convert other argument types to PyUnicode (or error)
        PyUnicode newPiece = coerceToUnicode(newPieceObj);
        PyUnicode oldPiece = coerceToUnicode(oldPieceObj);

        if (isBasicPlane() && newPiece.isBasicPlane() && oldPiece.isBasicPlane()) {
            // Use the mechanics of PyString, since all is basic plane
            return _replace(oldPiece.getString(), newPiece.getString(), count);

        } else {
            // A Unicode-specific implementation is needed working in code points
            StringBuilder buffer = new StringBuilder();

            if (oldPiece.getCodePointCount() == 0) {
                Iterator<Integer> iter = newSubsequenceIterator();
                for (int i = 1; (count == -1 || i < count) && iter.hasNext(); i++) {
                    if (i == 1) {
                        buffer.append(newPiece.getString());
                    }
                    buffer.appendCodePoint(iter.next());
                    buffer.append(newPiece.getString());
                }
                while (iter.hasNext()) {
                    buffer.appendCodePoint(iter.next());
                }
                return new PyUnicode(buffer);

            } else {
                SplitIterator iter = newSplitIterator(oldPiece, count);
                int numSplits = 0;
                while (iter.hasNext()) {
                    buffer.append(((PyUnicode)iter.next()).getString());
                    if (iter.hasNext()) {
                        buffer.append(newPiece.getString());
                    }
                    numSplits++;
                }
                if (iter.getEndsWithSeparator() && (count == -1 || numSplits <= count)) {
                    buffer.append(newPiece.getString());
                }
                return new PyUnicode(buffer);
            }
        }
    }

    // end utf-16 aware
    @Override
    public PyString join(PyObject seq) {
        return unicode_join(seq);
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_join_doc)
    final PyUnicode unicode_join(PyObject seq) {
        return unicodeJoin(seq);
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.unicode_startswith_doc)
    final boolean unicode_startswith(PyObject prefix, PyObject start, PyObject end) {
        // FIXME: slice indexing logic incorrect when this is ASTRAL
        return str_startswith(prefix, start, end);
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.unicode_endswith_doc)
    final boolean unicode_endswith(PyObject suffix, PyObject start, PyObject end) {
        // FIXME: slice indexing logic incorrect when this is ASTRAL
        return str_endswith(suffix, start, end);
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_translate_doc)
    final PyObject unicode_translate(PyObject table) {
        return _codecs.translateCharmap(this, "ignore", table);
    }

    // these tests need to be UTF-16 aware because they are character-by-character tests,
    // so we can only use equivalent str_XXX tests if we are in basic plane
    @ExposedMethod(doc = BuiltinDocs.unicode_islower_doc)
    final boolean unicode_islower() {
        if (isBasicPlane()) {
            return str_islower();
        }
        boolean cased = false;
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext();) {
            int codepoint = iter.next();
            if (Character.isUpperCase(codepoint) || Character.isTitleCase(codepoint)) {
                return false;
            } else if (!cased && Character.isLowerCase(codepoint)) {
                cased = true;
            }
        }
        return cased;
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_isupper_doc)
    final boolean unicode_isupper() {
        if (isBasicPlane()) {
            return str_isupper();
        }
        boolean cased = false;
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext();) {
            int codepoint = iter.next();
            if (Character.isLowerCase(codepoint) || Character.isTitleCase(codepoint)) {
                return false;
            } else if (!cased && Character.isUpperCase(codepoint)) {
                cased = true;
            }
        }
        return cased;
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_isalpha_doc)
    final boolean unicode_isalpha() {
        if (isBasicPlane()) {
            return str_isalpha();
        }
        if (getCodePointCount() == 0) {
            return false;
        }
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext();) {
            if (!Character.isLetter(iter.next())) {
                return false;
            }
        }
        return true;
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_isalnum_doc)
    final boolean unicode_isalnum() {
        if (isBasicPlane()) {
            return str_isalnum();
        }
        if (getCodePointCount() == 0) {
            return false;
        }
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext();) {
            int codePoint = iter.next();
            if (!(Character.isLetterOrDigit(codePoint) || Character.getType(codePoint) == Character.LETTER_NUMBER)) {
                return false;
            }
        }
        return true;
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_isdecimal_doc)
    final boolean unicode_isdecimal() {
        if (isBasicPlane()) {
            return str_isdecimal();
        }
        if (getCodePointCount() == 0) {
            return false;
        }
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext();) {
            if (Character.getType(iter.next()) != Character.DECIMAL_DIGIT_NUMBER) {
                return false;
            }
        }
        return true;
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_isdigit_doc)
    final boolean unicode_isdigit() {
        if (isBasicPlane()) {
            return str_isdigit();
        }
        if (getCodePointCount() == 0) {
            return false;
        }
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext();) {
            if (!Character.isDigit(iter.next())) {
                return false;
            }
        }
        return true;
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_isnumeric_doc)
    final boolean unicode_isnumeric() {
        if (isBasicPlane()) {
            return str_isnumeric();
        }
        if (getCodePointCount() == 0) {
            return false;
        }
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext();) {
            int type = Character.getType(iter.next());
            if (type != Character.DECIMAL_DIGIT_NUMBER && type != Character.LETTER_NUMBER
                    && type != Character.OTHER_NUMBER) {
                return false;
            }
        }
        return true;
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_istitle_doc)
    final boolean unicode_istitle() {
        if (isBasicPlane()) {
            return str_istitle();
        }
        if (getCodePointCount() == 0) {
            return false;
        }
        boolean cased = false;
        boolean previous_is_cased = false;
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext();) {
            int codePoint = iter.next();
            if (Character.isUpperCase(codePoint) || Character.isTitleCase(codePoint)) {
                if (previous_is_cased) {
                    return false;
                }
                previous_is_cased = true;
                cased = true;
            } else if (Character.isLowerCase(codePoint)) {
                if (!previous_is_cased) {
                    return false;
                }
                previous_is_cased = true;
                cased = true;
            } else {
                previous_is_cased = false;
            }
        }
        return cased;
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_isspace_doc)
    final boolean unicode_isspace() {
        if (isBasicPlane()) {
            return str_isspace();
        }
        if (getCodePointCount() == 0) {
            return false;
        }
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext();) {
            if (!Character.isWhitespace(iter.next())) {
                return false;
            }
        }
        return true;
    }

    // end utf-16 aware
    @ExposedMethod(doc = "isunicode is deprecated.")
    final boolean unicode_isunicode() {
        Py.warning(Py.DeprecationWarning, "isunicode is deprecated.");
        return true;
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_encode_doc)
    final String unicode_encode(PyObject[] args, String[] keywords) {
        return str_encode(args, keywords);
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_decode_doc)
    final PyObject unicode_decode(PyObject[] args, String[] keywords) {
        return str_decode(args, keywords);
    }

    @ExposedMethod(doc = BuiltinDocs.unicode___getnewargs___doc)
    final PyTuple unicode___getnewargs__() {
        return new PyTuple(new PyUnicode(this.getString()));
    }

    @Override
    public PyObject __format__(PyObject formatSpec) {
        return unicode___format__(formatSpec);
    }

    @ExposedMethod(doc = BuiltinDocs.unicode___format___doc)
    final PyObject unicode___format__(PyObject formatSpec) {
        // Re-use the str implementation, which adapts itself to unicode.
        return str___format__(formatSpec);
    }

    @ExposedMethod(doc = BuiltinDocs.unicode__formatter_parser_doc)
    final PyObject unicode__formatter_parser() {
        return new MarkupIterator(this);
    }

    @ExposedMethod(doc = BuiltinDocs.unicode__formatter_field_name_split_doc)
    final PyObject unicode__formatter_field_name_split() {
        FieldNameIterator iterator = new FieldNameIterator(this);
        return new PyTuple(iterator.pyHead(), iterator);
    }

    @ExposedMethod(doc = BuiltinDocs.unicode_format_doc)
    final PyObject unicode_format(PyObject[] args, String[] keywords) {
        try {
            return new PyUnicode(buildFormattedString(args, keywords, null, null));
        } catch (IllegalArgumentException e) {
            throw Py.ValueError(e.getMessage());
        }
    }

    @Override
    public Iterator<Integer> iterator() {
        return newSubsequenceIterator();
    }

    @Override
    public PyComplex __complex__() {
        return new PyString(encodeDecimal()).__complex__();
    }

    @Override
    public int atoi(int base) {
        return new PyString(encodeDecimal()).atoi(base);
    }

    @Override
    public PyLong atol(int base) {
        return new PyString(encodeDecimal()).atol(base);
    }

    @Override
    public double atof() {
        return new PyString(encodeDecimal()).atof();
    }

    /**
     * Encode unicode into a valid decimal String. Throws a UnicodeEncodeError on invalid
     * characters.
     *
     * @return a valid decimal as an encoded String
     */
    private String encodeDecimal() {
        if (isBasicPlane()) {
            return encodeDecimalBasic();
        }

        int digit;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext(); i++) {
            int codePoint = iter.next();
            if (Character.isWhitespace(codePoint)) {
                sb.append(' ');
                continue;
            }
            digit = Character.digit(codePoint, 10);
            if (digit >= 0) {
                sb.append(digit);
                continue;
            }
            if (0 < codePoint && codePoint < 256) {
                sb.appendCodePoint(codePoint);
                continue;
            }
            // All other characters are considered unencodable
            codecs.encoding_error("strict", "decimal", getString(), i, i + 1,
                    "invalid decimal Unicode string");
        }
        return sb.toString();
    }

    /**
     * Encode unicode in the basic plane into a valid decimal String. Throws a UnicodeEncodeError on
     * invalid characters.
     *
     * @return a valid decimal as an encoded String
     */
    private String encodeDecimalBasic() {
        int digit;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getString().length(); i++) {
            char ch = getString().charAt(i);
            if (Character.isWhitespace(ch)) {
                sb.append(' ');
                continue;
            }
            digit = Character.digit(ch, 10);
            if (digit >= 0) {
                sb.append(digit);
                continue;
            }
            if (0 < ch && ch < 256) {
                sb.append(ch);
                continue;
            }
            // All other characters are considered unencodable
            codecs.encoding_error("strict", "decimal", getString(), i, i + 1,
                    "invalid decimal Unicode string");
        }
        return sb.toString();
    }
}
