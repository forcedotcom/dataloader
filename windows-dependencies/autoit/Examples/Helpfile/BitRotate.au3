Local $x = BitRotate(7, 2)
;  x == 28 because 111b left-rotated twice is 1 1100b == 28

Local $y = BitRotate(14, -2)
;  y == 32771 because 1110b right-rotated twice on 16 bits is 1000 0000 0000 0011b == 32771

Local $z = BitRotate(14, -2, "D")
;  z == -2147483645 because 1110b right-rotated twice on 16 bits is 1000 0000 0000 0000 0000 0000 0000 0011b == 2147483645
