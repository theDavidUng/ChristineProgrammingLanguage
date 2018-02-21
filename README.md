# Christine Programming Language
Fully Functioning Programming Language

Authors: Ronald Mak, David Ung, Christine Le, John Humlick, Alex Hsiao

The majority of the code for the intermediate and backend was written by Ronald Mak for Pascal. However the entire code case was modified by David Ung, Christine Le, John Humlick, Alex Hsiao to fit the needs of the Christine language. The entire front end and visiter design implementaiton (front) was written by David Ung, Christine Le, John Humlick, Alex Hsiao.

The Christine language was inspired by the verbose recipe language called Chef. Christine requires expressions and declarations to be similar to everyday statements. Through our compiler written in Java, Christine is compiled into Jasmin assembly language. The compiler consists of five packages (frontend, intermediate, backend, util, and message). However, the majority of the work occurs in the frontend package, for type and variable names are declared in the first pass through the program code, while code generation happens during the second pass.
We have to major two major java files that generate the Jasmin assembly code for our program, Pass1Visiter.java and Pass2Visitor.java. Pass1Visitor pushes the global and local variables, method, and array into the Cross Reference table. A very critical role of Pass1Visitor is to push variables and method declaration information into the Symbol table stack and to generate the global variables and program headers in jasmin. Pass2Visitor generate the jasmin codes for variable operations, expressions, method operations, and logical statements, control statements, and arrays.


# How to Run

1. Run the main from Christine.java (src/christine/frontend/Christine/Christine.java) with the program args. being the program file written in the Christine language. ("test6.christine" from test_programs/test6.christine). After running Christine you will be see a newly created jasmin assembly file called file_name.j in your root directory.

2. Take the created jarmin file and run the file with the jasmin jar from the folder "jasmin-2.4". After running the command it will create a .class file for you to run with the packaged ChristineTool.jar

    a. Assuming your the .j file is "test.j", the following terminal command will be: java -jar ./jasmin.jar test.j

3. Run the generated class file with ChristineTool.jar

    a. Exmaple: java -cp ./ChristineTool.jar:. test

# Example:

If we were run "test6.christine":

    christine is breaking a program named test6;
    christine is doing her main thing {
        christine eats j as real;
        christine sticks 1.99 into j;
        christine eats i as integer;
        christine says {"j is equal to ", j};
        christine says {"I'm a funky language!\n"};
        christine needs 3 coffee for integer array coffeearray;
        christine needs 10 coffee for real array cafe;
        # Integer array test
        christine is putting a coffee bean 5 into spot 1 of the coffee array coffeearray;
        christine is retrieving the coffee bean at 1 from the coffee array coffeearray and placing the value into i;
        christine says {"i is equal to ", i};
        # Real array test
        christine is putting a coffee bean 5.3 into spot 2 of the coffee array cafe;
        christine is retrieving the coffee bean at 2 from the coffee array cafe and placing the value into j;
        christine says {"j is equal to ", j};
    }

Terminal Output would be:

    Davids-MacBook-Pro:jasmin-2.4 davidung$ java -jar ./jasmin.jar test6.j
    Generated: test6.class
    Davids-MacBook-Pro:jasmin-2.4 davidung$ java -cp ./ChristineTool.jar:. test6
    j is equal to 1.99 
    I'm a funky language!
    i is equal to 5
    j is equal to 5.3
                    0.01 seconds total execution time.
    Davids-MacBook-Pro:jasmin-2.4 davidung$ 


