#include <Array.au3>

Local $avArray[5] = [0, 1, 2, 1, 0]
Local $aiResult = _ArrayFindAll($avArray, 0)
_ArrayDisplay($avArray, "$avArray")
_ArrayDisplay($aiResult, "Results of searching for 0 in $avArray")
