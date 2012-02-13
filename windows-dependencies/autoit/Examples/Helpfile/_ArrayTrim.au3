#include <Array.au3>

Local $avArray[5]
$avArray[0] = "ab"
$avArray[1] = "bc"
$avArray[2] = "cd"
$avArray[3] = "de"
$avArray[4] = "ef"

_ArrayDisplay($avArray, "$avArray BEFORE _ArrayTrim()")
_ArrayTrim($avArray, 1, 1, 0, 4)
_ArrayDisplay($avArray, "$avArray AFTER _ArrayTrim() right 1 character from items 1 to 3")
