#include <Array.au3>

Local $avArray[10] = [9, 8, 7, 6, 5, 4, 3, 2, 1, 0]

_ArrayDisplay($avArray, "$avArray BEFORE _ArrayReverse()")
_ArrayReverse($avArray)
_ArrayDisplay($avArray, "$avArray AFTER _ArrayReverse()")
_ArrayReverse($avArray, 3, 6)
_ArrayDisplay($avArray, "$avArray AFTER _ArrayReverse() from index 3 to 6")
