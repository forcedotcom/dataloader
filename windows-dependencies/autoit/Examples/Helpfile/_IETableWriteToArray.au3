; *******************************************************
; Example 1 - Open a browser with the table example, get a reference to the second table
;				on the page (index 1) and read its contents into a 2-D array
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("table")
Local $oTable = _IETableGetCollection($oIE, 1)
Local $aTableData = _IETableWriteToArray($oTable)

; *******************************************************
; Example 2 - Same as Example 1, except transpose the output array and display
;				the results with _ArrayDisplay()
; *******************************************************

#include <IE.au3>
#include <Array.au3>

$oIE = _IE_Example("table")
$oTable = _IETableGetCollection($oIE, 1)
$aTableData = _IETableWriteToArray($oTable, True)
_ArrayDisplay($aTableData)
