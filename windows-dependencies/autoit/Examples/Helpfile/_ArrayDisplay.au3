#include <Array.au3>

;===============================================================================
; Example 1
;===============================================================================
Local $asControls = StringSplit(WinGetClassList("[active]", ""), @LF)
_ArrayDisplay($asControls, "Class List of Active Window")

;===============================================================================
; Example 2 (using a manually-defined array)
;===============================================================================
Local $avArray[10]

$avArray[0] = "JPM"
$avArray[1] = "Holger"
$avArray[2] = "Jon"
$avArray[3] = "Larry"
$avArray[4] = "Jeremy"
$avArray[5] = "Valik"
$avArray[6] = "Cyberslug"
$avArray[7] = "Nutster"
$avArray[8] = "JdeB"
$avArray[9] = "Tylo"

_ArrayDisplay($avArray, "$avArray set manually 1D")
_ArrayDisplay($avArray, "$avArray set manually 1D transposed", -1, 1)

;===============================================================================
; Example 3 (using an array returned by StringSplit())
;===============================================================================
$avArray = StringSplit(WinGetClassList("", ""), @LF)
_ArrayDisplay($avArray, "$avArray as a list classes in window")

;===============================================================================
; Example 4 (a 2D array)
;===============================================================================
Local $avArray[2][5] = [["JPM", "Holger", "Jon", "Larry", "Jeremy"],["Valik", "Cyberslug", "Nutster", "JdeB", "Tylo"]]
_ArrayDisplay($avArray, "$avArray as a 2D array")
_ArrayDisplay($avArray, "$avArray as a 2D array, transposed", -1, 1)
