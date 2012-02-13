#include <Array.au3>

Local $avArrayTarget[5] = ["JPM", "Holger", "Jon", "Larry", "Jeremy"]
Local $avArraySource[5] = ["Valik", "Cyberslug", "Nutster", "Tylo", "JdeB"]

_ArrayDisplay($avArrayTarget, "$avArrayTarget BEFORE _ArrayConcatenate()")
_ArrayConcatenate($avArrayTarget, $avArraySource)
_ArrayDisplay($avArrayTarget, "$avArrayTarget AFTER _ArrayConcatenate()")
