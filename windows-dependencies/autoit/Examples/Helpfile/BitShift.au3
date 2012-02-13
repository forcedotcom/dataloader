Local $x = BitShift(14, 2)
;  x == 3 because 1110b right-shifted twice is 11b == 3

Local $y = BitShift(14, -2)
;  y == 56 because 1110b left-shifted twice is 111000b == 56

Local $z = BitShift(1, -31)
;  z == -2147483648 because in 2's-complement notation, the
;  32nd digit from the right has a negative sign.
