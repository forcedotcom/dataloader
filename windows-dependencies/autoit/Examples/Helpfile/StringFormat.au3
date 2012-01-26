Local $n = 43951789;
Local $u = -43951789;

; notice the double %%, this prints a literal '%' character
printf("%%d = '%d'\n", $n);				'43951789'			standard integer representation
printf("%%e = '%e'\n", $n);				'4.395179e+007'		scientific notation
printf("%%u = '%u'\n", $n);				'43951789'			unsigned integer representation of a positive integer
printf("%%u <0 = '%u'\n", $u);			'4251015507'		unsigned integer representation of a negative integer
printf("%%f = '%f'\n", $n);				'43951789.000000'	floating point representation
printf("%%.2f = '%.2f'\n", $n);			'43951789.00'		floating point representation 2 digits after the decimal point
printf("%%o = '%o'\n", $n);				'247523255'			octal representation
printf("%%s = '%s'\n", $n);				'43951789'			string representation
printf("%%x = '%x'\n", $n);				'29ea6ad'			hexadecimal representation (lower-case)
printf("%%X = '%X'\n", $n);				'29EA6AD'			hexadecimal representation (upper-case)

printf("%%+d = '%+d'\n", $n);			'+43951789'			sign specifier on a positive integer
printf("%%+d <0= '%+d'\n", $u);			'-43951789'			sign specifier on a negative integer


Local $s = 'monkey';
Local $t = 'many monkeys';

printf("%%s = [%s]\n", $s);				[monkey]			standard string output
printf("%%10s = [%10s]\n", $s);			[    monkey]		right-justification with spaces
printf("%%-10s = [%-10s]\n", $s);		[monkey    ]		left-justification with spaces
printf("%%010s = [%010s]\n", $s);		[0000monkey]		zero-padding works on strings too
printf("%%10.10s = [%10.10s]\n", $t);	[many monke]		left-justification but with a cutoff of 10 characters

printf("%04d-%02d-%02d\n", 2008, 4, 1);

Func Printf($format, $var1, $var2 = -1, $var3 = -1)
	If $var2 = -1 Then
		ConsoleWrite(StringFormat($format, $var1))
	Else
		ConsoleWrite(StringFormat($format, $var1, $var2, $var3))
	EndIf
EndFunc   ;==>Printf
