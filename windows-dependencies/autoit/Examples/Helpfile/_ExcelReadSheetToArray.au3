; ******************************************************************************************
; Example 1 - After opening a workbook and returning its object identifier,
;				Fill some cells and Read the Values into an Array, using various paramaters.
; ******************************************************************************************

#include <Excel.au3>
#include <Array.au3>

Local $oExcel = _ExcelBookNew() ;Create new book, make it visible

; We can fill-up some cells using a simple loop and random Numbers
For $y = 1 To 10 ;Start on Column 1
	For $x = 1 To 15
		_ExcelWriteCell($oExcel, Round(Random(1000, 10000), 0), $x, $y) ;Some random numbers to file
	Next
Next

Local $aArray = _ExcelReadSheetToArray($oExcel) ;Using Default Parameters
_ArrayDisplay($aArray, "Array using Default Parameters")

$aArray = _ExcelReadSheetToArray($oExcel, 2) ;Starting on the 2nd Row
_ArrayDisplay($aArray, "Starting on the 2nd Row")

$aArray = _ExcelReadSheetToArray($oExcel, 1, 2) ;Starting on the 2nd Column
_ArrayDisplay($aArray, "Starting on the 2nd Column")

$aArray = _ExcelReadSheetToArray($oExcel, 1, 1, 5) ;Read 5 Rows
_ArrayDisplay($aArray, "Read 5 Rows")

$aArray = _ExcelReadSheetToArray($oExcel, 1, 1, 0, 2) ;Read 2 Columns
_ArrayDisplay($aArray, "Read 2 Columns")

$aArray = _ExcelReadSheetToArray($oExcel, 2, 3, 4, 5) ;Starting on the 2nd Row, 3rd Column, Read 4 Rows and 5 Columns
_ArrayDisplay($aArray, "Starting on the 2nd Row, 3rd Column, Read 4 Rows and 5 Columns")

$aArray = _ExcelReadSheetToArray($oExcel, 1, 1, 0, 0, True) ;Using Default Parameters, except Shifting Column (True)
_ArrayDisplay($aArray, "Array with Column shifting")

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out
