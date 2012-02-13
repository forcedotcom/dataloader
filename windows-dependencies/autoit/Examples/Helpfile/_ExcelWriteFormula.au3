; ***************************************************************
; Example 1 - Write to a Cell using a Loop, after opening a workbook and returning its object identifier.  Then enters a Forumula.
; *****************************************************************

#include <Excel.au3>

Local $oExcel = _ExcelBookNew() ;Create new book, make it visible

For $i = 0 To 20 ;Loop
	_ExcelWriteCell($oExcel, $i, $i, 1) ;Write to the Cell
Next

_ExcelWriteFormula($oExcel, "=Average(R1C1:R20C1)", 1, 2) ;Uses R1C1 referencing

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out
