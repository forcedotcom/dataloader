; ***************************************************************
; Example 1 - After opening a workbook and returning its object identifier.  Declare an Array, then input the Array
; *****************************************************************

#include <Excel.au3>

Local $oExcel = _ExcelBookNew() ;Create new book, make it visible

;Declare the Array
Local $aArray[5] = ["LocoDarwin", "Jon", "big_daddy", "DaleHolm", "GaryFrost"]

_ExcelWriteArray($oExcel, 1, 1, $aArray) ; Write the Array Horizontally
_ExcelWriteArray($oExcel, 5, 1, $aArray, 1) ; Write the Array Vertically, starting on the 5th Row

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out