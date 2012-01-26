; *******************************************************
; Example 1 - Open a browser with the table example, get a reference to the first table
;				on the page (index 0) and read its contents into a 2-D array
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("table")
Local $oTable = _IETableGetCollection($oIE, 0)
Local $aTableData = _IETableWriteToArray($oTable)

; *******************************************************
; Example 2 - Open a browser with the table example, get a reference to the
;				table collection and display the number of tables on the page
; *******************************************************

#include <IE.au3>

$oIE = _IE_Example("table")
$oTable = _IETableGetCollection($oIE)
Local $iNumTables = @extended
MsgBox(0, "Table Info", "There are " & $iNumTables & " tables on the page")
