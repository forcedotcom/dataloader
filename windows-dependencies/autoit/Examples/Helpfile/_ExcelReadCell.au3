; ***************************************************************
; Example 1 - Write to a Cell after opening a workbook and returning its object identifier.  Read the cell, then Save and Close file.
; *****************************************************************

#include <Excel.au3>

Local $oExcel = _ExcelBookNew() ;Create new book, make it visible

_ExcelWriteCell($oExcel, "I Wrote to This Cell", 1, 1) ;Write to the Cell
Local $sCellValue = _ExcelReadCell($oExcel, 1, 1)
MsgBox(0, "", "The Cell Value is: " & @CRLF & $sCellValue, 2)

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out

; ***************************************************************
; Example 2 - Write to a Cell using a Loop, after opening a workbook and returning its object identifier.  Read the cells, then Save and Close file.
; *****************************************************************

#include <Excel.au3>

$oExcel = _ExcelBookNew() ;Create new book, make it visible

For $i = 1 To 5 ;Loop
	_ExcelWriteCell($oExcel, $i, $i, 1) ;Write to the Cell
Next

For $i = 1 To 5 ;Loop
	$sCellValue = _ExcelReadCell($oExcel, $i, 1)
	MsgBox(0, "", "The Cell Value is: " & @CRLF & $sCellValue, 2)
Next

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out
