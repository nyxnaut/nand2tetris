/**
This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to <http://unlicense.org/>
**/
/** 
 *  The public API was specified as part of www.nand2tetris.org
 *  and the book "The Elements of Computing Systems"
 *  by Nisan and Schocken, MIT Press.
 *  
 *  Written by William Newell https://www.github.com/w-n-c
**/
/**
 *  The Hack assembly language contains 2 instructions and a pseudo-command:
 *  A-instructions: @value
 *    value is either a natural number or a symbol referring to such number
 *  C-instructions: dest=comp;jump (dest or jump optional)
 *    dest defines the register to store the output
 *    comp defines the logic for the ALU to compute
 *    jump specifies the conditions for execution to jump to another part of the program
 * 
 *    Example: subtracts A from D register, and jumps to the address at A if D is zero
 *      D=D-A;JEQ
 *  L pseudo-command (symbol)
 *    symbol is defined to reference the address of the next command in the file   
 *    
 *    Example: 
 *    (INFINITE_LOOP)
 *    @INFINITE_LOOP //sets A register to the address specified by inifite loop (so to this line)
 *    0;JMP          //Jumps to the address stored in the A register
 **/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

 /**
  * The Parser class handles file IO, text parsing, and binary generation for the 
  * Hack Assembly language as specified by www.nand2tetris.org (see credit above)
  **/
public class Parser {
  // A reader for the assembly input file
  private static BufferedReader codeFile;
  // A string containing the current assembly command
  private static String command;

  // a look-ahead to determine if there are more commands to parse
  public static final boolean hasMoreCommands() throws IOException {
    codeFile.mark(256);
    if (codeFile.readLine() != null) {
      codeFile.reset();
      return true;
    } else {
      return false;
    }
  }
  
  // reads the next line from codeFile, stripping comments and empty lines 
  // and assigns a new value to command
  public static final void advance() throws IOException {
    do {
      command = codeFile.readLine().trim();
      // skip comment lines
      if (command.matches("^\\/\\/.*")) {
        command = "";
        continue;
      }
      // strip inline comments and whitespace
      if (command.contains("//")) {
        command = command.split("\\/\\/")[0].trim();
      }
    } while (command.isEmpty()); // skip empty lines
  }

  // Returns a String specifying the type of registry command contained in the command variable
  public static final String commandType() throws ParsingException {
    // A commands begin with an @ and can contain letters, numbers, underscore, period, dollar sign, and colon
    // but the symbol portion (after @) cannot start with a number
    if (command.matches("^@[\\w_\\.\\$:][\\w\\d_\\.\\$:]*")) {
      return "A_COMMAND";
    // C commands have text surrounding a ";" or an "="
    } else if (command.matches(".+(;|=).+")) {
      return "C_COMMAND";
    // L commands are parens wrapping a symbol (so same character restictions as with A commands)
    } else if (command.matches("^\\([\\w_\\.\\$:][\\w\\d_\\.\\$:]*\\)$")) {
      return "L_COMMAND";
    } else {
      throw new ParsingException("Unable to match command to known types");
    }
  }

  // returns the symbol of A or L commands.
  // If the A command is numeric (@256) the value is returned as a string
  public static String symbol() throws ParsingException {
    String symbol;
    switch (command.charAt(0)) {
      case '@':
        symbol = command.split("@")[1];
        break;
      case '(':
        symbol = command.split("\\(")[1].split("\\)")[0];
        break;
      default: throw new ParsingException("invalid symbol syntax");
    }
    return symbol;
  }

  // returns destination mnemonic of the C command (8 possibilities)
  public static final String dest() {
    String[] c = command.split("=");
    if (c.length < 2) {
      return "null";
    } else {
      return c[0];
    }
  }

	// returns comp mnemonic of the C command (28 possibilities)
  public static final String comp() {
    String c;
    if (Parser.dest() != "null") {
      c = command.split("=")[1];
    } else {
      c = command;
    }
    if (Parser.jump() != "null") {
      c = c.split(";")[0];
    }
    return c;
  }

	// returns jump mnemonic of the C command (8 possibilities)
  public static final String jump() {
    String[] c = command.split(";");
    if (c.length < 2 ) {
      return "null";
    } else {
      return c[1];
    }
  }

  // pads converted values/addresses to the requisite 16 bits
  private static final String padTo16Bit(int value) throws ParsingException {
    String binary = Integer.toBinaryString(value);

    // the first bit is always zero so the address can only be 15 bits long
    if (binary.length() > 15) {
      throw new ParsingException("memory address outside acceptable range");
    }
    for (int i = binary.length(); i <= 15; i++) {
      binary = "0" + binary;
    }
    return binary;
  }

  // returns true if an A command symbol is a raw address (e.g. @256)
  private static final Boolean isNumericAddress(String text) {
    try {
      int val = Integer.parseInt(text);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  /**
   * Main opens the file passed as a command line argument.
   * It passes through the file twice. The first pass associates addresses
   * with any L pseudo-command symbols and writes them to SymbolTable
   * In the second pass it reads each command and symbol, generating binary output
   * and writing that output to a file
   **/
  public static void main(String args[]) throws IOException {
    // loads file for reading
    if (!args[0].matches(".*\\.asm$")) {
      throw new IOException("filename must end in .asm");
    }
    codeFile = new BufferedReader(new FileReader(args[0]));

    int address = 0; // tracks memory address for L commands
    while(Parser.hasMoreCommands()) {
      Parser.advance();
      switch (Parser.commandType()) {
        case "A_COMMAND":
          address++;
          break;
        case "C_COMMAND":
          address++;
          break;
        case "L_COMMAND":
          SymbolTable.addEntry(Parser.symbol(), address);
          break;
        default:
          throw new ParsingException("error constructing symbol table");
      }
    }
    /**
    * The second pass of the parser converts every command and symbol to
    * its appropriate binary representation and writes the binary output
    * to a [name].hack file
    * Any symbol that is not predefined or defined through an L command
    * is treated as a variable. Variables are mapped to consecutive memory locations
    * beginning at ram address 16.
    **/

    // create output file and file buffer
    String outputFile = args[0].split("\\.asm$")[0]+".hack"; 
    BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
    codeFile = new BufferedReader(new FileReader(args[0])); // read from beginning

    String binary; //string containing binary output of parsed command
    address = 16; // start of variable addressing space in memory;

    while(Parser.hasMoreCommands()) {
      Parser.advance();
      switch (Parser.commandType()) {
        case "A_COMMAND":
          if (Parser.isNumericAddress(Parser.symbol())) {
            binary = Parser.padTo16Bit(Integer.parseInt(Parser.symbol()));
          } else if (SymbolTable.contains(Parser.symbol())) {
            binary = Parser.padTo16Bit(SymbolTable.getAddress(Parser.symbol()));
          } else {
            SymbolTable.addEntry(Parser.symbol(), address);
            binary = Parser.padTo16Bit(address);
            address++;
          }
          output.write(binary);
          output.newLine();
          break;
        case "C_COMMAND":
          binary = "111" + Code.comp() + Code.dest() + Code.jump();
          output.write(binary);
          output.newLine();
          break;
        case "L_COMMAND":
          // L commands produce no binary output
          break;
        default:
          // vague but how it got past first pass without error is beyond me
          throw new ParsingException("Error in second parser pass");
      }
    }  
    output.flush(); // writes anything remainining in buffer to file
  }
}