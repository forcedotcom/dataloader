#include <Array.au3>

Local $avArray[2]

$avArray[0] = "World!"
$avArray[1] = "Hello"

_ArrayDisplay($avArray, "$avArray BEFORE _ArraySwap()")
_ArraySwap($avArray[0], $avArray[1])
_ArrayDisplay($avArray, "$avArray AFTER _ArraySwap()")
