/**
 * <p>Copyright (c)
 * <p>2017 by David Ung, Christine Le, John Humlick, Alex Hsiao</p>
 *
 * <p>For instructional purposes only.  No warranties.</p>
 */

package christine.frontend;

import static christine.intermediate.icodeimpl.ICodeNodeTypeImpl.*;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import christine.backend.compiler.CodeGenerator;
import christine.backend.compiler.generators.DeclaredRoutineGenerator;
import org.antlr.v4.runtime.tree.ParseTree;

import christine.frontend.ChristineParser.ExprContext;
import christine.frontend.ChristineParser.NumberContext;
import christine.frontend.ChristineParser.StmtContext;
import christine.frontend.ChristineParser.ThingsContext;
import christine.frontend.ChristineParser.VariableContext;
import christine.intermediate.*;
import christine.intermediate.symtabimpl.*;
import christine.intermediate.ICodeFactory;
import christine.intermediate.ICodeNode;
import christine.intermediate.icodeimpl.ICodeNodeTypeImpl;

public class Pass2Visitor extends ChristineBaseVisitor<Integer>
{
    String programName;
    private PrintWriter jFile;
    private int jumpingCounter;
    private HashMap<String, String> methodReturnType;
    private HashMap<String, String> methodArguments;
    private SymTabStack stack;

    public Pass2Visitor(PrintWriter jFile, SymTabStack stack)
    {
        this.jFile = jFile;
        this.jumpingCounter = 0;
        this.methodReturnType = new HashMap<>();
        this.methodArguments = new HashMap<>();
        this.stack = stack;
    }

    @Override
    public Integer visitProgram(ChristineParser.ProgramContext ctx)
    {
        Integer value = visitChildren(ctx);
        jFile.close();
        return value;
    }

    @Override
    public Integer visitHeader(ChristineParser.HeaderContext ctx)
    {
        programName = ctx.IDENTIFIER().toString();
        return visitChildren(ctx);
    }

    @Override
    public Integer visitMainBlock(ChristineParser.MainBlockContext ctx)
    {
        // Emit the class constructor.
        jFile.println();
        jFile.println(".method public <init>()V");
        jFile.println();
        jFile.println("\taload_0");
        jFile.println("\tinvokenonvirtual    java/lang/Object/<init>()V");
        jFile.println("\treturn");
        jFile.println();
        jFile.println(".limit locals 1");
        jFile.println(".limit stack 1");
        jFile.println(".end method");

        // Emit the main program header.
        jFile.println();
        jFile.println(".method public static main([Ljava/lang/String;)V");
        jFile.println();
        jFile.println("\tnew RunTimer");
        jFile.println("\tdup");
        jFile.println("\tinvokenonvirtual RunTimer/<init>()V");
        jFile.println("\tputstatic        " + programName + "/_runTimer LRunTimer;");
        jFile.println("\tnew PascalTextIn");
        jFile.println("\tdup");
        jFile.println("\tinvokenonvirtual PascalTextIn/<init>()V");
        jFile.println("\tputstatic        " + programName + "/_standardIn LPascalTextIn;");

        Integer value = visitChildren(ctx);

        // Emit the main program epilogue.
        jFile.println();
        jFile.println("\tgetstatic     " + programName + "/_runTimer LRunTimer;");
        jFile.println("\tinvokevirtual RunTimer.printElapsedTime()V");
        jFile.println();
        jFile.println("\treturn");
        jFile.println();
        jFile.println(".limit locals 16");
        jFile.println(".limit stack 16");
        jFile.println(".end method");

        return value;
    }

    @Override
    public Integer visitStmt(ChristineParser.StmtContext ctx)
    {
        jFile.println("\n; " + ctx.getText().replaceAll("\n", " ").replaceAll("\r", "") + "\n");
        return visitChildren(ctx);
    }

    @Override
    public Integer visitAssignmentStmt(ChristineParser.AssignmentStmtContext ctx)
    {
        Integer value;
        String typeIndicator;


        if (ctx.expr() == null)
        {
            value = visit(ctx.methodInvocation());
            typeIndicator = "I";
        }
        else
        {
            value = visit(ctx.expr());

            typeIndicator = (ctx.expr().type.getIdentifier().getName().equals("integer")) ? "I"
                    : (ctx.expr().type.getIdentifier().getName().equals("real")) ? "F"
                    :                                    "?";
        }

        // Emit a field put instruction.
        jFile.println("\tputstatic\t" + programName
                +  "/" + ctx.variable().IDENTIFIER().toString()
                + " " + typeIndicator);

        return value;
    }

    @Override
    public Integer visitAddSubExpr(ChristineParser.AddSubExprContext ctx)
    {
        Integer value = visitChildren(ctx);

        //TypeSpec type1 = ctx.expr(0).type;
        //TypeSpec type2 = ctx.expr(1).type;

        TypeSpec type1 = ctx.addSubOp().expr(0).type;
        TypeSpec type2 = ctx.addSubOp().expr(1).type;

        boolean integerMode =    (type1.equals(Predefined.integerType))
                && (type2.equals(Predefined.integerType));
        boolean realMode    =    (type1.equals(Predefined.realType))
                && (type2.equals(Predefined.realType));

        String op = ctx.addSubOp().getChild(0).getText();

        String opcode;

        if (op.equals("christine puts")) {
            opcode = integerMode ? "iadd"
                    : realMode    ? "fadd"
                    :               "????";
        }
        else {
            opcode = integerMode ? "isub"
                    : realMode    ? "fsub"
                    :               "????";
        }

        // Emit an add or subtract instruction.
        jFile.println("\t" + opcode);

        return value;
    }

    @Override
    public Integer visitMulDivExpr(ChristineParser.MulDivExprContext ctx)
    {
        Integer value = visitChildren(ctx);

        //TypeSpec type1 = ctx.expr(0).type;
        //TypeSpec type2 = ctx.expr(1).type;

        TypeSpec type1 = ctx.mulDivOp().expr(0).type;
        TypeSpec type2 = ctx.mulDivOp().expr(1).type;

        boolean integerMode =    (type1.equals(Predefined.integerType))
                && (type2.equals(Predefined.integerType));
        boolean realMode    =    (type1.equals(Predefined.realType))
                && (type2.equals(Predefined.realType));

        String op = ctx.mulDivOp().getChild(0).getText();
        String opcode;

        if (op.equals("christine puts")) {
            opcode = integerMode ? "imul"
                    : realMode    ? "fmul"
                    :               "f???";
        }
        else {
            opcode = integerMode ? "idiv"
                    : realMode    ? "fdiv"
                    :               "????";
        }

        // Emit a multiply or divide instruction.
        jFile.println("\t" + opcode);

        return value;
    }

    @Override
    public Integer visitVariableExpr(ChristineParser.VariableExprContext ctx)
    {
        String variableName = ctx.variable().IDENTIFIER().toString();
        TypeSpec type = ctx.type;

        String typeIndicator = (type.equals(Predefined.integerType)) ? "I"
                : (type.equals(Predefined.realType))    ? "F"
                :                                    "?";

        // Emit a field get instruction.
        jFile.println("\tgetstatic\t" + programName +
                "/" + variableName + " " + typeIndicator);

        return visitChildren(ctx);
    }

    @Override
    public Integer visitSignedNumber(ChristineParser.SignedNumberContext ctx)
    {
        Integer value = visitChildren(ctx);
        TypeSpec type = ctx.number().type;

        if (ctx.sign().getChild(0) == ctx.sign().SUB_OP()) {
            String opcode = (type.equals(Predefined.integerType)) ? "ineg"
                    : (type.equals(Predefined.realType))    ? "fneg"
                    :                                    "?neg";

            // Emit a negate instruction.
            jFile.println("\t" + opcode);
        }

        return value;
    }

    @Override
    public Integer visitIntegerConst(ChristineParser.IntegerConstContext ctx)
    {
        // Emit a load constant instruction.
        jFile.println("\tldc\t" + ctx.getText());

        return visitChildren(ctx);
    }

    @Override
    public Integer visitFloatConst(ChristineParser.FloatConstContext ctx)
    {
        // Emit a load constant instruction.
        jFile.println("\tldc\t" + ctx.getText());

        return visitChildren(ctx);
    }

    @Override
    public Integer visitUntil_loop(ChristineParser.Until_loopContext ctx)
    {
        String start = Integer.toString(jumpingCounter++);
        String end = Integer.toString(jumpingCounter++);

        jFile.println("\tLBL" + start + ":");
        jFile.println("\t" + "; visitUntil_loop");

        visit(ctx.expr());
        jFile.println("\tifeq LBL" + end);

        Integer value = visit(ctx.block());

        jFile.println("\tgoto LBL" + start);
        jFile.println("\tLBL" + end + ":");
        jFile.println("\t" + "; visitUntil_loop");

        return value;
    }

    @Override
    public Integer visitIf_stat(ChristineParser.If_statContext ctx)
    {
        String end = Integer.toString(jumpingCounter++);
        String next = Integer.toString(jumpingCounter++);

        int i = 0;
        visit(ctx.expr(0));
        jFile.println("\tifne LBL" + next);
        visit(ctx.block(0));
        jFile.println("\tgoto LBL" + end);

        for (i = 0; i < ctx.ELIF().size(); i++)
        {
            visit(ctx.ELIF(i));
            jFile.println("\tLBL" + next + ":");
            next = Integer.toString(jumpingCounter++);
            visit(ctx.expr(i + 1));
            jFile.println("\tifne LBL" + next);
            visit(ctx.block(i + 1));
            jFile.println("\tgoto LBL" + end);
        }

        if (ctx.ELSE() != null)
        {
            visit(ctx.ELSE());
            jFile.println("\tLBL" + next + ":");
            next = Integer.toString(jumpingCounter++);
            visit(ctx.block(i + 1));
        }

        jFile.println("\tLBL" + end + ":");
        jFile.println("\tLBL" + next + ":");
        jumpingCounter++;

        return 0;
    }

    @Override
    public Integer visitRelExpr(ChristineParser.RelExprContext ctx)
    {
        String relational_operator;
        Integer value = visitChildren(ctx);

        switch (ctx.rel_op().getText())
        {
            case "equal to":
                relational_operator = "if_icmpeq";
                break;
            case "less than":
                relational_operator = "if_icmplt";
                break;
            case "greater than":
                relational_operator = "if_icmpgt";
                break;
            case "less than or equal to":
                relational_operator = "if_icmple";
                break;
            case "greater than or equal to":
                relational_operator = "if_icmpge";
                break;
            default:
                System.out.println("Unknown operator provided: " + ctx.rel_op().getText());
                relational_operator = "????";
                break;
        }

        String label1 = Integer.toString(jumpingCounter++);
        String label2 = Integer.toString(jumpingCounter++);

        jFile.println("\t" + relational_operator + " LBL" + label1);
        jFile.println("\ticonst_1");
        jFile.println("\tgoto LBL" + label2);
        jFile.println("\tLBL" + label1 + ":");
        jFile.println("\t" + "; visitRelExpr");
        jFile.println("\ticonst_0");
        jFile.println("\tLBL" + label2 + ":");
        jFile.println("\t" + "; visitRelExpr");

        return value;
    }


    @Override
    public Integer visitPrintStr(ChristineParser.PrintStrContext ctx)
    {
        jFile.println("\tgetstatic java/lang/System/out Ljava/io/PrintStream;");
        jFile.println("\tldc " + ctx.getText());
        jFile.println("\tinvokevirtual java/io/PrintStream/print(Ljava/lang/String;)V");
        return visitChildren(ctx);

    }

    @Override
    public Integer visitPrintVar(ChristineParser.PrintVarContext ctx)
    {
            TypeSpec type = ctx.type;
            if (type.getIdentifier().getName().equals("integer"))
            {
                jFile.println("\tgetstatic java/lang/System/out Ljava/io/PrintStream;");
                jFile.println("\tnew       java/lang/StringBuilder");
                jFile.println("\tdup");
                jFile.println("\tldc \"\"");
                jFile.println("\tinvokenonvirtual java/lang/StringBuilder/<init>(Ljava/lang/String;)V");
                jFile.println("\tgetstatic     " + programName + "/" + ctx.getText() + " I");
                jFile.println("\tinvokevirtual java/lang/StringBuilder/append(I)Ljava/lang/StringBuilder;");
                jFile.println("\tinvokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;");
                jFile.println("\tinvokevirtual java/io/PrintStream/print(Ljava/lang/String;)V");
                return visitChildren(ctx);
            }
            else if (type.getIdentifier().getName().equals("real"))
            {
                jFile.println("\tgetstatic java/lang/System/out Ljava/io/PrintStream;");
                jFile.println("\tnew       java/lang/StringBuilder");
                jFile.println("\tdup");
                jFile.println("\tldc \"\"");
                jFile.println("\tinvokenonvirtual java/lang/StringBuilder/<init>(Ljava/lang/String;)V");
                jFile.println("\tgetstatic     " + programName + "/" + ctx.getText() + " F");
                jFile.println("\tinvokevirtual java/lang/StringBuilder/append(F)Ljava/lang/StringBuilder;");
                jFile.println("\tinvokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;");
                jFile.println("\tinvokevirtual java/io/PrintStream/print(Ljava/lang/String;)V");
                return visitChildren(ctx);
            }
            else
            {
                System.out.println("I was asked to print a variable I don't know anything about! " +
                                   ctx.toString());
                return visitChildren(ctx);
            }
    }

    @Override
    public Integer visitMethodInvocation(ChristineParser.MethodInvocationContext ctx)
    {
        String[] parametersList = ctx.parameters().getText().split(",");

        for (int index = 0; index < parametersList.length; index++)
        {
            if (isNumeric(parametersList[index]))
            {
                jFile.println("\tsipush " + parametersList[index]);
            }
            else
            {
                SymTabEntry singleVar = stack.lookup(parametersList[index]);
                String singleType = singleVar.getTypeSpec().getIdentifier().getName();

                String typeIndicator = "?";
                if (singleType.equals("integer"))
                {
                    typeIndicator = "I";
                }
                else if (singleType.equals("real"))
                {
                    typeIndicator = "F";
                }

                jFile.println("\tgetstatic \t\t" + programName + "/" + parametersList[index] + " " + typeIndicator);
            }
        }

        StringBuilder methodName = new StringBuilder(ctx.IDENTIFIER().getText()
                + "(" + methodArguments.get(ctx.IDENTIFIER().getText()) + ")"
                + methodReturnType.get(ctx.IDENTIFIER().getText()));

        jFile.println("\tinvokestatic " + programName + "/" + methodName);

        return 0;
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }

        return true;
    }

    @Override
    public Integer visitVoidMethod(ChristineParser.VoidMethodContext ctx)
    {
        String methodName = ctx.IDENTIFIER().getText();
        StringBuilder methodHeader = new StringBuilder(".method public static " + methodName + "(");
        methodReturnType.put(methodName, "V");

        StringBuilder argumentHeader = new StringBuilder();
        for (ChristineParser.VariableDeclarationContext singleVar: ctx.arguments().variableDeclaration())
        {
            argumentHeader.append(visitArgumentType(singleVar));
        }

        methodHeader.append(argumentHeader.toString());
        methodArguments.put(methodName, argumentHeader.toString());

        jFile.println(";visiting void method declaration");
        jFile.println(methodHeader + ")V\n");

        for (int index = 0; index < ctx.arguments().variableDeclaration().size(); index++)
        {
            ChristineParser.VariableDeclarationContext singleVar = ctx.arguments().variableDeclaration().get(index);
            String name = singleVar.declList().decl(0).varList().varId(0).getText();

            jFile.println("iload " + index);
            jFile.println("putstatic " + programName + "/" +  name + " " + visitArgumentType(singleVar));
        }

        jFile.println(";visiting method statements");

        Integer value = visitChildren(ctx.block());

        jFile.println("return");
        jFile.println(".limit locals 16");
        jFile.println(".limit stack 16");
        jFile.println(".end method");

        return value;
    }

    @Override
    public Integer visitIntegerMethod(ChristineParser.IntegerMethodContext ctx)
    {
        String methodName = ctx.IDENTIFIER().getText();
        StringBuilder methodHeader = new StringBuilder(".method public static " + methodName + "(");
        methodReturnType.put(methodName, "I");

        StringBuilder argumentHeader = new StringBuilder();
        for (ChristineParser.VariableDeclarationContext singleVar: ctx.arguments().variableDeclaration())
        {
            argumentHeader.append(visitArgumentType(singleVar));
        }

        methodHeader.append(argumentHeader.toString());
        methodArguments.put(methodName, argumentHeader.toString());

        jFile.println(";visiting void method declaration");
        jFile.println(methodHeader + ")I\n");

        for (int index = 0; index < ctx.arguments().variableDeclaration().size(); index++)
        {
            ChristineParser.VariableDeclarationContext singleVar = ctx.arguments().variableDeclaration().get(index);
            String name = singleVar.declList().decl(0).varList().varId(0).getText();

            jFile.println("iload " + index);
            jFile.println("putstatic " + programName + "/" +  name + " " + visitArgumentType(singleVar));
        }

        jFile.println(";visiting method statements");

        Integer value = visitChildren(ctx.returning_block());

        jFile.println(".limit locals 16");
        jFile.println(".limit stack 16");
        jFile.println(".end method");

        return value;
    }

    public String visitArgumentType(ChristineParser.VariableDeclarationContext ctx)
    {
        String typeString = ctx.declList().decl(0).typeId().getText();

        if (typeString.equals("integer"))
        {
            return "I";
        }
        else if (typeString.equals("char"))
        {
            return "C";
        }
        else if (typeString.equals("real"))
        {
            return "F";
        }

        return "?";
    }

    @Override
    public Integer visitOrExpr(ChristineParser.OrExprContext ctx)
    {
        Integer value = visitChildren(ctx);

        for (ExprContext expr: ctx.expr())
        {
            value = visit(expr);
            if (value != null && value == 1)
            {
                return value;
            }
        }

        return value;
    }

    @Override
    public Integer visitAndExpr(ChristineParser.AndExprContext ctx)
    {
        Integer value = visitChildren(ctx);

        for (ExprContext expr: ctx.expr())
        {
            value = visit(expr);
            if (value != null && value == 0)
            {
                return value;
            }
        }

        return value;
    }
    @Override
    public Integer visitReturnValue(ChristineParser.ReturnValueContext ctx)
    {
        VariableContext var = ctx.retVal().variable();
        NumberContext num = ctx.retVal().number();

        if (var != null)
        {
            System.out.print("Variable detected " + var.getText() + " ");
            SymTabEntry varTab = stack.lookup(var.getText());
            String typeIndicator = "?";

            if (varTab.getTypeSpec().getIdentifier().getName().equals("integer"))
            {
                typeIndicator = "I";
            }
            else if (varTab.getTypeSpec().getIdentifier().getName().equals("real"))
            {
                typeIndicator = "F";
            }
            System.out.println(typeIndicator);
            jFile.println("getstatic \t\t" + programName + "/" + var.getText() + " " + typeIndicator);
            jFile.println(typeIndicator.toLowerCase() + "return");
        }
        else if (num != null)
        {
            System.out.println("Number detected " + num.getText());
            jFile.println("sipush " + num.getText());
            if (num.getText().contains("."))
            {
                jFile.println("freturn");
            }
            else
            {
                jFile.println("ireturn");
            }
        }

        return visitChildren(ctx);
    }

    @Override
    public Integer visitRecord_declaration(ChristineParser.Record_declarationContext ctx)
    {
        String arrayName = ctx.IDENTIFIER().toString();
        String type;
        String typeId = ctx.typeId().getText();

        if(typeId.equals("integer"))
        {
            typeId = "int";
            type = "I";
        }
        else if(typeId.equals("real"))
        {
            type = "F";
            typeId = "float";
        }
        else if(typeId.equals("char"))
        {
            type = "C";
            typeId = "char";
        }
        else
        {
            type = "?";
        }

        jFile.println("\tbipush " + ctx.INTEGER());
        jFile.println("\tnewarray " + typeId);
        jFile.println("\tputstatic " + programName + "/" + arrayName + " [" + type);
        return visitChildren(ctx);
    }

    @Override
    public Integer visitRecordAssign(ChristineParser.RecordAssignContext ctx)
    {
        String arrayName;
        String value;
        String type;

        if(ctx.type.getIdentifier().getName().equals("integer"))
        {
            type = "I";
        }
        else if(ctx.type.getIdentifier().getName().equals("real"))
        {
            type = "F";
        }
        else if(ctx.type.getIdentifier().getName().equals("char"))
        {
            type = "C";
        }
        else
        {
            type = "?";
        }

        if(ctx.IDENTIFIER().size() > 1)
        {
            arrayName = ctx.IDENTIFIER().get(1).toString();
            value = ctx.IDENTIFIER().get(0).toString();
        }
        else if (ctx.CHAR() != null)
        {
            arrayName = ctx.IDENTIFIER().get(0).toString();
            value = ctx.CHAR().getText();
        }
        else
        {
            arrayName = ctx.IDENTIFIER().get(0).toString();
            value = ctx.number().getText();
        }

        if(ctx.type != null) {
            // get array address
            jFile.println("\tgetstatic " + programName + "/" + arrayName + " [" + type);
            jFile.println("\tdup");

            // get index
            jFile.println("\tldc " + ctx.INTEGER());

            // value to insert
            jFile.println("\tldc " + value);

            if(type.equals("I"))
            {
                jFile.println("\tiastore");
            }
            else if(type.equals("F"))
            {
                jFile.println("\tfastore");
            }
            else
            {
                jFile.println("\tcastore");
            }

            // store value
            jFile.println("\tputstatic " + programName + "/" + arrayName + " [" + type);

        }
        return 0;
    }

    @Override
    public Integer visitRecordAccess(ChristineParser.RecordAccessContext ctx)
    {
        String type;
        String loadInstr;

        if(ctx.type.getIdentifier().getName().equals("integer"))
        {
            type = "I";
            loadInstr = "\tiaload";
        }
        else if(ctx.type.getIdentifier().getName().equals("real"))
        {
            type = "F";
            loadInstr = "\tfaload";
        }
        else if(ctx.type.getIdentifier().getName().equals("char"))
        {
            type = "C";
            loadInstr = "\tcaload";
        }
        else
        {
            type = "?";
            loadInstr = "?";
        }
        jFile.println("\tgetstatic " + programName + "/" + ctx.IDENTIFIER().toString() + " [" + type);
        jFile.println("\tdup");
        jFile.println("\tldc " + ctx.INTEGER());
        jFile.println(loadInstr);

        if(ctx.variable() != null)
        {
            jFile.println("\tputstatic " + programName + "/" + ctx.variable().getText() + " " + type);
        }

        return visitChildren(ctx);
    }

} //End Class


