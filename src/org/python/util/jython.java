// Copyright (c) Corporation for National Research Initiatives
package org.python.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.python.Version;
import org.python.core.CodeFlag;
import org.python.core.CompileMode;
import org.python.core.Options;
import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyException;
import org.python.core.PyFile;
import org.python.core.PyList;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.core.PySystemState;
import org.python.core.imp;
import org.python.core.util.FileUtil;
import org.python.core.util.RelativeFile;
import org.python.modules._systemrestart;
import org.python.modules.thread.thread;

public class jython {
    private static final String COPYRIGHT =
        "Type \"help\", \"copyright\", \"credits\" or \"license\" for more information.";

    static final String usageHeader =
        "usage: jython [option] ... [-c cmd | -m mod | file | -] [arg] ...\n";

    private static final String usage = usageHeader +
        "Options and arguments:\n" + //(and corresponding environment variables):\n" +
        "-c cmd   : program passed in as string (terminates option list)\n" +
        //"-d       : debug output from parser (also PYTHONDEBUG=x)\n" +
        "-Dprop=v : Set the property `prop' to value `v'\n"+
        //"-E       : ignore environment variables (such as PYTHONPATH)\n" +
        "-C codec : Use a different codec when reading from the console.\n"+
        "-h       : print this help message and exit (also --help)\n" +
        "-i       : inspect interactively after running script\n" + //, (also PYTHONINSPECT=x)\n" +
        "           and force prompts, even if stdin does not appear to be a terminal\n" +
        "-jar jar : program read from __run__.py in jar file\n"+
        "-m mod   : run library module as a script (terminates option list)\n" +
        //"-O       : optimize generated bytecode (a tad; also PYTHONOPTIMIZE=x)\n" +
        //"-OO      : remove doc-strings in addition to the -O optimizations\n" +
        "-Q arg   : division options: -Qold (default), -Qwarn, -Qwarnall, -Qnew\n" +
        "-S       : don't imply 'import site' on initialization\n" +
        //"-t       : issue warnings about inconsistent tab usage (-tt: issue errors)\n" +
        "-u       : unbuffered binary stdout and stderr\n" + // (also PYTHONUNBUFFERED=x)\n" +
        //"           see man page for details on internal buffering relating to '-u'\n" +
        "-v       : verbose (trace import statements)\n" + // (also PYTHONVERBOSE=x)\n" +
        "-V       : print the Python version number and exit (also --version)\n" +
        "-W arg   : warning control (arg is action:message:category:module:lineno)\n" +
        //"-x       : skip first line of source, allowing use of non-Unix forms of #!cmd\n" +
        "file     : program read from script file\n" +
        "-        : program read from stdin (default; interactive mode if a tty)\n" +
        "arg ...  : arguments passed to program in sys.argv[1:]\n" +
        "Other environment variables:\n" +
        "JYTHONPATH: '" + File.pathSeparator +
        "'-separated list of directories prefixed to the default module\n" +
        "            search path.  The result is sys.path.";

    public static boolean shouldRestart;

    public static void runJar(String filename) {
        // TBD: this is kind of gross because a local called `zipfile' just magically
        // shows up in the module's globals.  Either `zipfile' should be called
        // `__zipfile__' or (preferrably, IMO), __run__.py should be imported and a main()
        // function extracted.  This function should be called passing zipfile in as an
        // argument.
        //
        // Probably have to keep this code around for backwards compatibility (?)
        try {
            ZipFile zip = new ZipFile(filename);

            ZipEntry runit = zip.getEntry("__run__.py");
            if (runit == null) {
                throw Py.ValueError("jar file missing '__run__.py'");
            }

            PyStringMap locals = new PyStringMap();

            // Stripping the stuff before the last File.separator fixes Bug #931129 by
            // keeping illegal characters out of the generated proxy class name
            int beginIndex;
            if ((beginIndex = filename.lastIndexOf(File.separator)) != -1) {
                filename = filename.substring(beginIndex + 1);
            }

            locals.__setitem__("__name__", new PyString(filename));
            locals.__setitem__("zipfile", Py.java2py(zip));

            InputStream file = zip.getInputStream(runit);
            PyCode code;
            try {
                code = Py.compile(file, "__run__", CompileMode.exec);
            } finally {
                file.close();
            }
            Py.runCode(code, locals, locals);
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    public static void main(String[] args) {
        do {
            shouldRestart = false;
            run(args);
        } while (shouldRestart);
    }

    public static void run(String[] args) {
        // Parse the command line options
        CommandLineOptions opts = new CommandLineOptions();
        if (!opts.parse(args)) {
            if (opts.version) {
                System.err.println("Jython " + Version.PY_VERSION);
                System.exit(0);
            }
            if (!opts.runCommand && !opts.runModule) {
                System.err.println(usage);
            }

            int exitcode = opts.help ? 0 : -1;
            System.exit(exitcode);
        }

        // Setup the basic python system state from these options
        PySystemState.initialize(PySystemState.getBaseProperties(), opts.properties, opts.argv);

        // Now create an interpreter
        InteractiveConsole interp = newInterpreter();

        PyList warnoptions = new PyList();
        for (String wopt : opts.warnoptions) {
            warnoptions.append(new PyString(wopt));
        }
        Py.getSystemState().setWarnoptions(warnoptions);

        // Decide if stdin is interactive
        if (!opts.fixInteractive && opts.interactive) {
            opts.interactive = ((PyFile)Py.defaultSystemState.stdin).isatty();
            if (!opts.interactive) {
                PySystemState systemState = Py.getSystemState();
                systemState.ps1 = systemState.ps2 = Py.EmptyString;
            }
        }

        // Print banner and copyright information (or not)
        if (opts.interactive && opts.notice && !opts.runModule) {
            System.err.println(InteractiveConsole.getDefaultBanner());
        }

        if (Options.importSite) {
            try {
                imp.load("site");
                if (opts.interactive && opts.notice && !opts.runModule) {
                    System.err.println(COPYRIGHT);
                }
            } catch (PyException pye) {
                if (!pye.match(Py.ImportError)) {
                    System.err.println("error importing site");
                    Py.printException(pye);
                    System.exit(-1);
                }
            }
        }

        if (opts.division != null) {
            if ("old".equals(opts.division)) {
                Options.divisionWarning = 0;
            } else if ("warn".equals(opts.division)) {
                Options.divisionWarning = 1;
            } else if ("warnall".equals(opts.division)) {
                Options.divisionWarning = 2;
            } else if ("new".equals(opts.division)) {
                Options.Qnew = true;
                interp.cflags.setFlag(CodeFlag.CO_FUTURE_DIVISION);
            }
        }

        // was there a filename on the command line?
        if (opts.filename != null) {
            String path;
            try {
                 path = new File(opts.filename).getCanonicalFile().getParent();
            } catch (IOException ioe) {
                 path = new File(opts.filename).getAbsoluteFile().getParent();
            }
            if (path == null) {
                path = "";
            }
            Py.getSystemState().path.insert(0, new PyString(path));
            if (opts.jar) {
                runJar(opts.filename);
            } else if (opts.filename.equals("-")) {
                try {
                    interp.locals.__setitem__(new PyString("__file__"), new PyString("<stdin>"));
                    interp.execfile(System.in, "<stdin>");
                } catch (Throwable t) {
                    Py.printException(t);
                }
            } else {
                try {
                   interp.locals.__setitem__(new PyString("__file__"),
                                             new PyString(opts.filename));

                   FileInputStream file;
                   try {
                       file = new FileInputStream(new RelativeFile(opts.filename));
                   } catch (FileNotFoundException e) {
                       throw Py.IOError(e);
                   }
                   if (FileUtil.isatty(file.getFD())) {
                       opts.interactive = true;
                       interp.interact(null, new PyFile(file));
                       System.exit(0);
                   } else {
                       interp.execfile(file, opts.filename);
                   }
                } catch (Throwable t) {
                    if (t instanceof PyException
                        && ((PyException)t).match(_systemrestart.SystemRestart)) {
                        // Shutdown this instance...
                        shouldRestart = true;
                        shutdownInterpreter();
                        // ..reset the state...
                        Py.setSystemState(new PySystemState());
                        // ...and start again
                    } else {
                        Py.printException(t);
                        if (!opts.interactive) {
                            interp.cleanup();
                            System.exit(-1);
                        }
                    }
                }
            }
        }
        else {
            // if there was no file name on the command line, then "" is the first element
            // on sys.path.  This is here because if there /was/ a filename on the c.l.,
            // and say the -i option was given, sys.path[0] will have gotten filled in
            // with the dir of the argument filename.
            Py.getSystemState().path.insert(0, Py.EmptyString);

            if (opts.command != null) {
                try {
                    interp.exec(opts.command);
                } catch (Throwable t) {
                    Py.printException(t);
                    System.exit(1);
                }
            }

            if (opts.moduleName != null) {
                // PEP 338 - Execute module as a script
                try {
                    interp.exec("import runpy");
                    interp.set("name", Py.newString(opts.moduleName));
                    interp.exec("runpy.run_module(name, run_name='__main__', alter_sys=True)");
                    interp.cleanup();
                    System.exit(0);
                } catch (Throwable t) {
                    Py.printException(t);
                    interp.cleanup();
                    System.exit(-1);
                }
            }
        }

        if (opts.fixInteractive || (opts.filename == null && opts.command == null)) {
            if (opts.encoding == null) {
                opts.encoding = PySystemState.registry.getProperty(
                                "python.console.encoding", null);
            }
            if (opts.encoding != null) {
                if (!Charset.isSupported(opts.encoding)) {
                    System.err.println(opts.encoding
                                       + " is not a supported encoding on this JVM, so it can't "
                                       + "be used in python.console.encoding.");
                    System.exit(1);
                }
                interp.cflags.encoding = opts.encoding;
            }
            try {
                interp.interact(null, null);
            } catch (Throwable t) {
                Py.printException(t);
            }
        }
        interp.cleanup();
        if (opts.fixInteractive || opts.interactive) {
            System.exit(0);
        }
    }

    /**
     * Returns a new python interpreter using the InteractiveConsole subclass from the
     * <tt>python.console</tt> registry key.
     */
    private static InteractiveConsole newInterpreter() {
        try {
            String interpClass =
                    PySystemState.registry.getProperty("python.console",
                                                       "org.python.util.InteractiveConsole");
            return (InteractiveConsole)Class.forName(interpClass).newInstance();
        } catch (Throwable t) {
            return new InteractiveConsole();
        }
    }

    /**
     * Run any finalizations on the current interpreter in preparation for a SytemRestart.
     */
    public static void shutdownInterpreter() {
        // Stop all the active threads
        thread.interruptAllThreads();
        // Close all sockets -- not all of their operations are stopped by
        // Thread.interrupt (in particular pre-nio sockets)
        try {
            imp.load("socket").__findattr__("_closeActiveSockets").__call__();
        } catch (PyException pye) {
            // continue
        }
    }
}

class CommandLineOptions {
    public String filename;
    public boolean jar, interactive, notice;
    public boolean runCommand, runModule;
    public boolean fixInteractive;
    public boolean help, version;
    public String[] argv;
    public Properties properties;
    public String command;
    public List<String> warnoptions = Generic.list();
    public String encoding;
    public String division;
    public String moduleName;

    public CommandLineOptions() {
        filename = null;
        jar = fixInteractive = false;
        interactive = notice = true;
        runModule = false;
        properties = new Properties();
        help = version = false;
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
        try {
            System.setProperty(key, value);
        } catch (SecurityException e) {
            // continue
        }
    }

    public boolean parse(String[] args) {
        int index = 0;

        while (index < args.length && args[index].startsWith("-")) {
            String arg = args[index];
            if (arg.equals("-h") || arg.equals("-?") || arg.equals("--help")) {
                help = true;
                return false;
            } else if (arg.equals("-V") || arg.equals("--version")) {
                version = true;
                return false;
            } else if (arg.equals("-")) {
                if (!fixInteractive) {
                    interactive = false;
                }
                filename = "-";
            } else if (arg.equals("-i")) {
                fixInteractive = true;
                interactive = true;
            } else if (arg.equals("-jar")) {
                jar = true;
                if (!fixInteractive) {
                    interactive = false;
                }
            } else if (arg.equals("-u")) {
                Options.unbuffered = true;
            } else if (arg.equals("-v")) {
                Options.verbose++;
            } else if (arg.equals("-vv")) {
                Options.verbose += 2;
            } else if (arg.equals("-vvv")) {
                Options.verbose +=3 ;
            } else if (arg.equals("-S")) {
                Options.importSite = false;
            } else if (arg.equals("-c")) {
                runCommand = true;
                if (arg.length() > 2) {
                    command = arg.substring(2);
                } else if ((index + 1) < args.length) {
                    command = args[++index];
                } else {
                    System.err.println("Argument expected for the -c option");
                    System.err.print(jython.usageHeader);
                    System.err.println("Try `jython -h' for more information.");
                    return false;
                }
                if (!fixInteractive) {
                    interactive = false;
                }
                index++;
                break;
            } else if (arg.equals("-W")) {
                warnoptions.add(args[++index]);
            } else if (arg.equals("-C")) {
                encoding = args[++index];
            } else if (arg.equals("-E")) {
                // XXX: accept -E (ignore environment variables) to be compatiable with
                // CPython. do nothing for now (we could ignore the registry)
            } else if (arg.startsWith("-D")) {
                String key = null;
                String value = null;
                int equals = arg.indexOf("=");
                if (equals == -1) {
                    String arg2 = args[++index];
                    key = arg.substring(2, arg.length());
                    value = arg2;
                } else {
                    key = arg.substring(2, equals);
                    value = arg.substring(equals + 1, arg.length());
                }
                setProperty(key, value);
            } else if (arg.startsWith("-Q")) {
                if (arg.length() > 2) {
                    division = arg.substring(2);
                } else {
                    division = args[++index];
                }
            } else if (arg.startsWith("-m")) {
                runModule = true;
                if (arg.length() > 2) {
                    moduleName = arg.substring(2);
                } else if ((index + 1) < args.length) {
                    moduleName = args[++index];
                } else {
                    System.err.println("Argument expected for the -m option");
                    System.err.print(jython.usageHeader);
                    System.err.println("Try `jython -h' for more information.");
                    return false;
                }
                if (!fixInteractive) {
                    interactive = false;
                }

                index++;
                int n = args.length - index + 1;
                argv = new String[n];
                argv[0] = moduleName;
                for (int i = 1; index < args.length; i++, index++) {
                    argv[i] = args[index];
                }
                return true;
            } else {
                String opt = args[index];
                if (opt.startsWith("--")) {
                    opt = opt.substring(2);
                } else if (opt.startsWith("-")) {
                    opt = opt.substring(1);
                }
                System.err.println("Unknown option: " + opt);
                return false;
            }
            index += 1;
        }
        notice = interactive;
        if (filename == null && index < args.length && command == null) {
            filename = args[index++];
            if (!fixInteractive) {
                interactive = false;
            }
            notice = false;
        }
        if (command != null) {
            notice = false;
        }

        int n = args.length - index + 1;
        argv = new String[n];
        if (filename != null) {
            argv[0] = filename;
        } else if (command != null) {
            argv[0] = "-c";
        } else {
            argv[0] = "";
        }

        for (int i = 1; i < n; i++, index++) {
            argv[i] = args[index];
        }

        return true;
    }
}
