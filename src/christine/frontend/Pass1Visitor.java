/**
 * <p>Copyright (c)
 * <p>2017 by David Ung, Christine Le, John Humlick, Alex Hsiao</p>
 *
 * <p>For instructional purposes only.  No warranties.</p>
 */

package christine.frontend;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.PrintStream;
import java.util.HashMap;

import christine.intermediate.*;
import christine.intermediate.symtabimpl.*;
import christine.util.*;
import org.antlr.v4.runtime.tree.ParseTree;

import christine.frontend.ChristineParser.StmtContext;
import christine.intermediate.ICodeFactory;
import christine.intermediate.ICodeNode;
import christine.intermediate.icodeimpl.ICodeNodeTypeImpl;

import static christine.intermediate.symtabimpl.DefinitionImpl.*;
import static christine.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static christine.intermediate.symtabimpl.SymTabKeyImpl.*;
import static christine.intermediate.typeimpl.TypeFormImpl.ARRAY;
import static christine.intermediate.typeimpl.TypeFormImpl.RECORD;
import static christine.intermediate.typeimpl.TypeKeyImpl.ARRAY_ELEMENT_TYPE;
import java.util.HashMap;
import java.util.HashSet;


public class Pass1Visitor extends ChristineBaseVisitor<Integer>
{
    private SymTabStack symTabStack;
    private SymTabEntry programId;
    private ArrayList<SymTabEntry> variableIdList;
    private HashMap<String, TypeSpec> arrayIdMap;
    private PrintWriter jFile;
    private HashSet<String> variableCreatedSet;

    public Pass1Visitor()
    {
        // Create and initialize the symbol table stack.
        symTabStack = SymTabFactory.createSymTabStack();
        Predefined.initialize(symTabStack);

        this.variableCreatedSet = new HashSet<>();
    }

    public PrintWriter getAssemblyFile() { return jFile; }

    @Override
    public Integer visitProgram(ChristineParser.ProgramContext ctx)
    {
        Integer value = visitChildren(ctx);

        // Print the cross-reference table.
        CrossReferencer crossReferencer = new CrossReferencer();
        crossReferencer.print(symTabStack);

        return value;
    }

    @Override
    public Integer visitHeader(ChristineParser.HeaderContext ctx)
    {
        String programName = ctx.IDENTIFIER().toString();

        programId = symTabStack.enterLocal(programName);
        programId.setDefinition(DefinitionImpl.PROGRAM);
        programId.setAttribute(ROUTINE_SYMTAB, symTabStack.push());
        symTabStack.setProgramId(programId);

        // Create the assembly output file.
        try {
            jFile = new PrintWriter(new FileWriter(programName + ".j"));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }

        // Emit the program header.
        jFile.println(".class public " + programName);
        jFile.println(".super java/lang/Object");

        // Emit the RunTimer and PascalTextIn fields.
        jFile.println();
        jFile.println(".field private static _runTimer LRunTimer;");
        jFile.println(".field private static _standardIn LPascalTextIn;");

        return visitChildren(ctx);
    }

    @Override
    public Integer visitLocalVariableDeclaration(ChristineParser.LocalVariableDeclarationContext ctx)
    {
        Integer value = visitChildren(ctx);
        return value;
    }

    @Override
    public Integer visitDecl(ChristineParser.DeclContext ctx)
    {
        jFile.println("\n; " + ctx.getText() + "\n");
        return visitChildren(ctx);
    }

    @Override
    public Integer visitVarList(ChristineParser.VarListContext ctx)
    {
        variableIdList = new ArrayList<SymTabEntry>();
        return visitChildren(ctx);
    }

    @Override
    public Integer visitVarId(ChristineParser.VarIdContext ctx)
    {
        String variableName = ctx.IDENTIFIER().toString();

        SymTabEntry variableId = symTabStack.enterLocal(variableName);
        variableId.setDefinition(DefinitionImpl.VARIABLE);
        variableIdList.add(variableId);

        return visitChildren(ctx);
    }

    @Override
    public Integer visitTypeId(ChristineParser.TypeIdContext ctx)
    {
        String typeName = ctx.getText();

        TypeSpec type;
        String typeIndicator;

        if (typeName.equalsIgnoreCase("integer")) {
            type = Predefined.integerType;
            typeIndicator = "I";
        }
        else if (typeName.equalsIgnoreCase("real")) {
            type = Predefined.realType;
            typeIndicator = "F";
        }
        else if (typeName.equalsIgnoreCase("char")) {
            type = Predefined.charType;
            typeIndicator = "C";
        }
        else {
            type = null;
            typeIndicator = "?";
        }

        for (SymTabEntry id : variableIdList) {

            if (!variableCreatedSet.contains(id.getName()))
            {
                id.setTypeSpec(type);
                // Emit a field declaration.
                jFile.println(".field private static " +
                        id.getName() + " " + typeIndicator);

                variableCreatedSet.add(id.getName());
            }
        }

        return visitChildren(ctx);
    }

    @Override
    public Integer visitAddSubExpr(ChristineParser.AddSubExprContext ctx)
    {
        Integer value = visitChildren(ctx);

        TypeSpec type1 = ctx.addSubOp().expr(0).type;
        TypeSpec type2 = ctx.addSubOp().expr(1).type;

        boolean integerMode =    (type1.equals(Predefined.integerType))
                && (type2.equals(Predefined.integerType));
        boolean realMode    =    (type1.equals(Predefined.realType))
                && (type2.equals(Predefined.realType));

        TypeSpec type = integerMode ? Predefined.integerType
                : realMode    ? Predefined.realType
                :               null;
        ctx.type = type;

        return value;
    }

    @Override
    public Integer visitMulDivExpr(ChristineParser.MulDivExprContext ctx)
    {
        Integer value = visitChildren(ctx);

        TypeSpec type1 = ctx.mulDivOp().expr(0).type;
        TypeSpec type2 = ctx.mulDivOp().expr(1).type;

        boolean integerMode =    (type1.equals(Predefined.integerType))
                && (type2.equals(Predefined.integerType));
        boolean realMode    =    (type1.equals(Predefined.realType))
                && (type2.equals(Predefined.realType));

        TypeSpec type = integerMode ? Predefined.integerType
                : realMode    ? Predefined.realType
                :               null;
        ctx.type = type;

        return value;
    }

    @Override
    public Integer visitVariableExpr(ChristineParser.VariableExprContext ctx)
    {
        String variableName = ctx.variable().IDENTIFIER().toString();
        SymTabEntry variableId = symTabStack.lookup(variableName);

        ctx.type = variableId.getTypeSpec();
        return visitChildren(ctx);
    }

    @Override
    public Integer visitSignedNumberExpr(ChristineParser.SignedNumberExprContext ctx)
    {
        Integer value = visitChildren(ctx);
        ctx.type = ctx.signedNumber().type;
        return value;
    }

    @Override
    public Integer visitSignedNumber(ChristineParser.SignedNumberContext ctx)
    {
        Integer value = visit(ctx.number());
        ctx.type = ctx.number().type;
        return value;
    }

    @Override
    public Integer visitUnsignedNumberExpr(ChristineParser.UnsignedNumberExprContext ctx)
    {
        Integer value = visit(ctx.number());
        ctx.type = ctx.number().type;
        return value;
    }

    @Override
    public Integer visitIntegerConst(ChristineParser.IntegerConstContext ctx)
    {
        ctx.type = Predefined.integerType;
        return visitChildren(ctx);
    }

    @Override
    public Integer visitFloatConst(ChristineParser.FloatConstContext ctx)
    {
        ctx.type = Predefined.realType;
        return visitChildren(ctx);
    }

    @Override
    public Integer visitParenExpr(ChristineParser.ParenExprContext ctx)
    {
        Integer value = visitChildren(ctx);
        ctx.type = ctx.expr().type;
        return value;
    }

    @Override
    public Integer visitUntil_loop(ChristineParser.Until_loopContext ctx)
    {
        return visitChildren(ctx);
    }

    @Override
    public Integer visitPrintStr(ChristineParser.PrintStrContext ctx)
    {
        Integer value = visitChildren(ctx);
        ctx.type = Predefined.charType;

        return value;
    }
    @Override
    public Integer visitPrintVar(ChristineParser.PrintVarContext ctx)
    {
        Integer value = visitChildren(ctx);
        SymTabEntry table_entry = symTabStack.lookup(ctx.getText());
        if (table_entry == null) {
            return value;
        }
        else if (table_entry.getTypeSpec() == Predefined.realType)
        {
            ctx.type = Predefined.realType;
        }
        else
        {
            ctx.type = Predefined.integerType;
        }
        return value;
    }

    @Override
    public Integer visitPrint(ChristineParser.PrintContext ctx)
    {
        return visitChildren(ctx);
    }

    @Override
    public Integer visitVoidMethod(ChristineParser.VoidMethodContext ctx)
    {
        SymTabEntry tableEntry = symTabStack.lookup(ctx.IDENTIFIER().getText());

        if (tableEntry == null)
        {
            SymTabEntry table = symTabStack.enterLocal(ctx.IDENTIFIER().getText());

            String variableName = ctx.IDENTIFIER().getText();
            SymTabEntry variableId = symTabStack.enterLocal(variableName);
            variableId.setDefinition(DefinitionImpl.FUNCTION);

            Integer value = visitChildren(ctx);

            return value;
        }

        return 0;
    }

    @Override
    public Integer visitIntegerMethod(ChristineParser.IntegerMethodContext ctx)
    {
        SymTabEntry tableEntry = symTabStack.lookup(ctx.IDENTIFIER().getText());

        if (tableEntry == null)
        {
            SymTabEntry table = symTabStack.enterLocal(ctx.IDENTIFIER().getText());

            String variableName = ctx.IDENTIFIER().getText();
            SymTabEntry variableId = symTabStack.enterLocal(variableName);
            variableId.setDefinition(DefinitionImpl.FUNCTION);

            Integer value = visitChildren(ctx);

            return value;
        }

        return 0;
    }

    @Override
    public Integer visitBlock(ChristineParser.BlockContext ctx) {
        return visitChildren(ctx);
    }

    public SymTabStack getSymtabStack()
    {
        return symTabStack;
    }

    @Override
    public Integer visitRecord_declaration(ChristineParser.Record_declarationContext ctx)
    {
        String arrayName = ctx.IDENTIFIER().toString();
        SymTabEntry arrayId = symTabStack.enterLocal(arrayName);
        arrayId.setDefinition(DefinitionImpl.ARRAY);

        String typeId;

        // set type spec
        if(ctx.typeId().getText().equalsIgnoreCase("integer"))
        {
            arrayId.setTypeSpec(Predefined.integerType);
            typeId = "I";
        }
        else if(ctx.typeId().getText().equalsIgnoreCase("real"))
        {
            arrayId.setTypeSpec(Predefined.realType);
            typeId = "F";
        }
        else if(ctx.typeId().getText().equalsIgnoreCase("char"))
        {
            arrayId.setTypeSpec(Predefined.charType);
            typeId = "C";
        }
        else
        {
            System.out.println("I don't know what to do with array " +
                               arrayName);
            return visitChildren(ctx);
        }

        ctx.type = arrayId.getTypeSpec();

        // put array entry in map
        if(arrayIdMap != null)
        {
            arrayIdMap.put(arrayId.getName(), arrayId.getTypeSpec());
        }
        else
        {
            arrayIdMap = new HashMap<>();
            arrayIdMap.put(arrayId.getName(), arrayId.getTypeSpec());
        }

        jFile.println(".field private static " + arrayName + " [" + typeId);

        return visitChildren(ctx);
    }

    @Override
    public Integer visitRecordAssign(ChristineParser.RecordAssignContext ctx)
    {
        String arrayName;

        if(ctx.number() != null)
        {
            arrayName = ctx.IDENTIFIER().get(0).toString();
        }
        else
        {
            arrayName = ctx.IDENTIFIER().get(0).toString();
        }

        // make sure array exists
        if(symTabStack.lookup(arrayName) != null)
        {
            ctx.type = arrayIdMap.get(arrayName);
        }

        return visitChildren(ctx);
    }

    @Override
    public Integer visitRecordAccess(ChristineParser.RecordAccessContext ctx)
    {
        String arrayName = ctx.IDENTIFIER().toString();

        // make sure array exists
        if(symTabStack.lookup(arrayName) != null)
        {
            ctx.type = arrayIdMap.get(arrayName);
        }

        return visitChildren(ctx);
    }
}


