; *****************************************************************************
; Example 1 - Declare a 1-dimensional array, return an Array of permutations
; *****************************************************************************

#include <Array.au3>

Local $aArray[4] = [1, 2, 3, 4]
Local $aNewArray = _ArrayPermute($aArray, ",") ;Using Default Parameters
_ArrayDisplay($aNewArray, "Array Permuted")
