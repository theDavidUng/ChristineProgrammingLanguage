/**
 * <p>Copyright (c)
 * <p>2017 by David Ung, Christine Le, John Humlick, Alex Hsiao</p>
 *
 * <p>For instructional purposes only.  No warranties.</p>
 */

package christine.frontend;

import java.util.ArrayList;

import christine.intermediate.*;
import christine.intermediate.symtabimpl.*;
import christine.util.*;

import static christine.intermediate.symtabimpl.SymTabKeyImpl.*;
import static christine.intermediate.symtabimpl.DefinitionImpl.*;

public class CompilerVisitor extends ChristineBaseVisitor<Integer>
{
    private SymTabStack symTabStack;
    private SymTabEntry programId;
    private ArrayList<SymTabEntry> variableIdList;
    private TypeSpec dataType;

    public CompilerVisitor()
    {
        // Create and initialize the symbol table stack.
        symTabStack = SymTabFactory.createSymTabStack();
        Predefined.initialize(symTabStack);
    }

    @Override
    public Integer visitProgram(ChristineParser.ProgramContext ctx)
    {
        System.out.println("Visiting program");
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

        return visitChildren(ctx);
    }

    @Override
    public Integer visitDecl(ChristineParser.DeclContext ctx)
    {
        System.out.println("Visiting dcl");
        return visitChildren(ctx);
    }

    @Override
    public Integer visitVarList(ChristineParser.VarListContext ctx)
    {
        System.out.println("Visiting variable list");
        variableIdList = new ArrayList<SymTabEntry>();

        return visitChildren(ctx);
    }

    @Override
    public Integer visitVarId(ChristineParser.VarIdContext ctx)
    {
        String variableName = ctx.IDENTIFIER().toString();

        SymTabEntry variableId = symTabStack.enterLocal(variableName);
        variableId.setDefinition(VARIABLE);
        variableIdList.add(variableId);

        return visitChildren(ctx);
    }

    @Override
    public Integer visitTypeId(ChristineParser.TypeIdContext ctx)
    {
        //String typeName = ctx.IDENTIFIER().toString();
        String typeName = ctx.getText();

        dataType = typeName.equalsIgnoreCase("integer")
                ? Predefined.integerType
                : typeName.equalsIgnoreCase("real")
                ? Predefined.realType
                : null;

        for (SymTabEntry id : variableIdList)
        {
            id.setTypeSpec(dataType);
        }

        return visitChildren(ctx);
    }
}