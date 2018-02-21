# Christine Programming Language
Fully Functioning Programming Language

Authors: Ronald Mak, David Ung, Christine Le, John Humlick, Alex Hsiao

The majority of the code for the intermediate and backend was written by Ronald Mak for Pascal. However the entire code case was modified by David Ung, Christine Le, John Humlick, Alex Hsiao to fit the needs of the Christine language. The entire front end and visiter design implementaiton (front) was written by David Ung, Christine Le, John Humlick, Alex Hsiao.

The Christine language was inspired by the verbose recipe language called Chef. Christine requires expressions and declarations to be similar to everyday statements. Through our compiler written in Java, Christine is compiled into Jasmin assembly language. The compiler consists of five packages (frontend, intermediate, backend, util, and message). However, the majority of the work occurs in the frontend package, for type and variable names are declared in the first pass through the program code, while code generation happens during the second pass.
We have to major two major java files that generate the Jasmin assembly code for our program, Pass1Visiter.java and Pass2Visitor.java. Pass1Visitor pushes the global and local variables, method, and array into the Cross Reference table. A very critical role of Pass1Visitor is to push variables and method declaration information into the Symbol table stack and to generate the global variables and program headers in jasmin. Pass2Visitor generate the jasmin codes for variable operations, expressions, method operations, and logical statements, control statements, and arrays.

