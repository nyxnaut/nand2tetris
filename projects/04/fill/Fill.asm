// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// read keyboard input
// which key is pressed is determined by the word at @KBD

// intialize keyboard to zero on start
D=0
@KBD
M=D

//create a pointer to the current screen position (starts at @SCREEN)
@SCREEN
D=A
@sptr
M=D

(WAITING)
	// loop till keyboard input then jump to FILL
	@KBD
	D=M
	@CHECK
	D;JEQ
	@FILL
	0;JMP
(CHECK)
	@sptr
	D=M
	@screen
	D=D-A
	@EMPTY
	D;JGE
	@WAITING
	0;JMP
(FILL)
	@sptr
	D=M
	@KBD
	D=D-A
	@WAITING
	D;JEQ
	@sptr
	A=M
	M=-1
	D=A+1
	@sptr
	M=D
	@WAITING
	0;JMP

(EMPTY)
	@sptr
	D=M
	@SCREEN
	D=D-A;
	@WAITING
	D;JEQ
	@sptr
	A=M-1
	M=0
	D=A
	@sptr
	M=D
	@WAITING
	0;JMP
