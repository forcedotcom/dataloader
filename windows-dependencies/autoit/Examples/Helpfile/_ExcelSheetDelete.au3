; ***************************************************************
; Example 1 - After opening a workbook and returning its object identifier, Delete a Worksheet by String Name
; *****************************************************************

#include <Excel.au3>

Local $oExcel = _ExcelBookNew() ;Create new book, make it visible

_ExcelSheetDelete($oExcel, "Sheet1") ;Delete Sheet by string name of SheetName

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out

; ***************************************************************
; Example 2 - After opening a workbook and returning its object identifier, Delete a Worksheet by Index
; *****************************************************************

#include <Excel.au3>

$oExcel = _ExcelBookNew() ;Create new book, make it visible

_ExcelSheetDelete($oExcel, 1) ;Delete Sheet by index of SheetName

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out
