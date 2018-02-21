/**
 * <p>Copyright (c)
 * <p>2017 by David Ung, Christine Le, John Humlick, Alex Hsiao</p>
 *
 * <p>For instructional purposes only.  No warranties.</p>
 */


package christine.frontend;

import christine.intermediate.SymTabEntry;
import christine.intermediate.SymTabStack;
import christine.util.ParseTreePrinter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;


public class Christine
{
    public static void main(String[] args) throws Exception
    {
        String inputFile = null;

        if (args.length > 0) inputFile = args[0];
        InputStream is = (inputFile != null)
                ? new FileInputStream(inputFile)
                : System.in;

        ANTLRInputStream input = new ANTLRInputStream(is);
        ChristineLexer lexer = new ChristineLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ChristineParser parser = new ChristineParser(tokens);
        ParseTree tree = parser.program();

        Pass1Visitor pass1 = new Pass1Visitor();
        pass1.visit(tree);

        PrintWriter jFile = pass1.getAssemblyFile();

        CompilerVisitor compiler = new CompilerVisitor();
        compiler.visit(tree);

        SymTabStack stack = pass1.getSymtabStack();

        Pass2Visitor pass2 = new Pass2Visitor(jFile, stack);
        pass2.visit(tree);

    }
}
