; ***************************************************************
; Example 1 - Write to a Cell using a Loop, after opening a workbook and returning its object identifier.  Delete 1 row, then Save and Close file.
; *****************************************************************

#include <Excel.au3>

Local $oExcel = _ExcelBookNew() ;Create new book, make it visible

For $i = 1 To 5 ;Loop
	_ExcelWriteCell($oExcel, $i, $i, 1) ;Write to the Cell Vertically using values 1 to 5
Next

ToolTip("Deleting Rows Soon...")
Sleep(3500)

_ExcelRowDelete($oExcel, 1, 1) ;Delete Row 1 and only delete 1 Row

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out

; ***************************************************************
; Example 2 - Write to a Cell using a Loop, after opening a workbook and returning its object identifier.  Delete some rows, then Save and Close file.
; *****************************************************************

#include <Excel.au3>

$oExcel = _ExcelBookNew() ; Open new book, make it visible

For $i = 1 To 5 ;Loop
	_ExcelWriteCell($oExcel, $i, $i, 1) ;Write to the Cell Vertically using values 1 to 5
Next

ToolTip("Deleting Rows Soon...")
Sleep(3500)

_ExcelRowDelete($oExcel, 3, 2) ;Delete Rows starting at row 3, and delete 2 Rows

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out
