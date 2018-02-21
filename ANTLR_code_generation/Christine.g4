/**
 * Define a grammar called Christine
 */
grammar Christine;

@header {
package christine.frontend;
import christine.intermediate.*;
import christine.intermediate.symtabimpl.*;
}

/** The start rule; begin parsing here. */
program: header methods mainBlock;

header: VAR IDENTIFIER ';' NEWLINE;

methods	: method_declaration* ;

mainBlock : 'christine is doing her main thing' sBrack block eBrack;

block     : stmt*;

stmt: localVariableDeclaration NEWLINE
    | assignmentStmt 
    | if_stat NEWLINE 
    | until_loop NEWLINE 
    | print NEWLINE
    | record_declaration NEWLINE
    | recordOperations NEWLINE
    | methodInvocation NEWLINE
    ;

methodInvocation: 'christine casually strolls to' IDENTIFIER 'with' parameters ';'
                ;
    
recordOperations locals [TypeSpec type = null]
                : 'christine is retrieving the coffee bean at' INTEGER 'from the coffee array' IDENTIFIER ('and placing the value into' variable)? ';' #recordAccess
                | 'christine is putting a coffee bean' (IDENTIFIER  | number | CHAR) 'into spot' INTEGER 'of the coffee array' IDENTIFIER ';' #recordAssign
                ;
                
print: 'christine says' sBrack things (',' things)*? ('}' ';' | eBrack);
things locals [ TypeSpec type = null ]
	  : STRING		#printStr	// Plain string to print
	  | IDENTIFIER	#printVar	// We assume this is int for now
	  ;

assignmentStmt: 'christine sticks' (expr | sParen methodInvocation eParen) 'into' variable ';' NEWLINE;

variable : IDENTIFIER ;
    
expr locals [ TypeSpec type = null ]
	:   mulDivOp # mulDivExpr
    |   addSubOp # addSubExpr
    |   expr rel_op expr # relExpr
    |   expr AND expr # andExpr
    |   expr OR expr # orExpr
    |   variable # variableExpr
    |   number  # unsignedNumberExpr
    |   signedNumber  # signedNumberExpr
    |   '(' expr ')' # parenExpr
    ;

number locals [ TypeSpec type = null ]
    : INTEGER    # integerConst
    | FLOAT      # floatConst
    ;    
    
rel_op: LT | GT | LTEQ | GTEQ | EQ;
    
mulDivOp: 'christine puts' expr 'and' expr 'into a mixing bowl'
        | 'christine flushes' expr 'and' expr 'down the toilet'
        ;
        
addSubOp: 'christine puts' expr 'and' expr 'together'
        | 'christine takes' expr 'away from' expr
        ;

signedNumber locals [ TypeSpec type = null ]
    : sign number
    ;

sign : ADD_OP | SUB_OP ;
        
until_loop: 'christine repeats herself until' sParen expr eParen sBrack block eBrack
          ;
// We would like to separate control statements by indentation instead of brackets, but could not get it working in time
if_stat : 'christine needs a moment to decide' IF sParen expr eParen sBrack block eBrack  (ELIF sParen expr eParen sBrack block eBrack)*? (ELSE sBrack block eBrack)?
        ;

method_declaration	: 'christine is creating a method that returns nothing called' IDENTIFIER sParen arguments eParen sBrack block eBrack #VoidMethod
					| 'christine is creating a method that returns integer called' IDENTIFIER sParen arguments eParen sBrack returning_block eBrack #IntegerMethod
					;

localVariableDeclaration: variableDeclaration ';';
			
variableDeclaration: 'christine eats' declList;
declList     : decl ( ';' decl )* ;
decl         : varList ' as ' typeId ;
varList      : varId ( ',' varId )* ;
varId        : IDENTIFIER;
typeId       :
    		 |   'char'
    		 |   'integer'
    		 |   'real'
    		 ;
			
arguments : variableDeclaration (',' variableDeclaration)*?
		  | 
		  ;

record_declaration locals [ TypeSpec type = null ]: 'christine needs' INTEGER 'coffee for' typeId 'array' IDENTIFIER ';'
	 	;

parameters	: (IDENTIFIER | number) (',' IDENTIFIER | number)*? 
			|
			;

returning_block : retCombo*;   
 
retVal : 'christine is returning' (variable | number) ';';

retCombo : stmt   #returnStatement
		 | retVal #returnValue
		 ;

// Start parenthesis
sParen	: NEWLINE* '(' NEWLINE*; 

// End parenthesis 
eParen	: NEWLINE* ')' NEWLINE*; 

// Start curly bracket
sBrack	: NEWLINE* '{' NEWLINE*; 

// End curly bracket
eBrack	: NEWLINE* '}' NEWLINE*;

IF      :   'if';
ELIF    :   'else if';
ELSE    :   'else';
EQ      :   'equal to';
LT      :   'less than';
GT      :   'greater than';
LTEQ    :   'less than or equal to';
GTEQ    :   'greater than or equal to';
AND     :   'logical and';
OR      :   'logical or';
STRING		: '"' .*? '"' ;				  // match strings in quotes
IDENTIFIER  : [a-zA-Z][a-zA-Z0-9]* ;      // match identifiers <label id="code.tour.expr.3"/>
INTEGER :   [0-9]+ ;                      // match integers
FLOAT:      [0-9]+ '.' [0-9]+ ;           // match floats
CHAR:		'\'' . '\'' ;				  // match char
NEWLINE:    '\r'? '\n' ;                  // return newlines to parser (is end-statement signal)
WS  :       [ \t]+ -> skip ;              // toss out whitespace
COMMENT :	'#'.*? NEWLINE -> skip ;	  // comment - skip anything from # to newline
VAR: 'christine is breaking a program named';
RETURN	: 'christine is returning';
ADD_OP :   '+' ;
SUB_OP :   '-' ;

