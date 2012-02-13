; ***************************************************************
; Example 1 - Write to a Cell using a Loop, after opening a workbook and returning its object identifier.  Read the cells into an array, display array,
;				then Save and Close file.
; *****************************************************************

#include <Excel.au3>
#include <Array.au3>

Local $oExcel = _ExcelBookNew() ;Create new book, make it visible

For $i = 1 To 5 ;Loop
	_ExcelWriteCell($oExcel, $i, $i, 1) ;Write to the Cell Vertically using values 1 to 5
Next

For $i = 1 To 5 ;Loop
	_ExcelWriteCell($oExcel, Asc($i), 1, $i + 2) ;Write to the Cell Horizontally, using Asc just to use different values for reading purposes
Next

Local $aArray1 = _ExcelReadArray($oExcel, 1, 1, 5, 1) ;Direction is Vertical
Local $aArray2 = _ExcelReadArray($oExcel, 1, 3, 5) ;Direction is Horizontal
_ArrayDisplay($aArray2, "Horizontal")
_ArrayDisplay($aArray1, "Vertical")

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out
