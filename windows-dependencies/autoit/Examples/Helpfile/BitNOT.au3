Local $x = BitNOT(5)

#cs Comments:
	Result is -6 because for 32-bit numbers
	5 == 00000000000000000000000000000101 binary
	-6 == 11111111111111111111111111111010 binary
	and the first bit is signed
#ce
