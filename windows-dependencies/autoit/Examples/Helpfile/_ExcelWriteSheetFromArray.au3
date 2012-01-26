; ***************************************************************
; Example 1 - After opening a workbook and returning its object identifier.  Declare a 2-D Array, then input the Array
; *****************************************************************

#include <Excel.au3>

Local $oExcel = _ExcelBookNew() ;Create new book, make it visible

;Declare the Array
Local $aArray[5][2] = [["LocoDarwin", 1],["Jon", 2],["big_daddy", 3],["DaleHolm", 4],["GaryFrost", 5]] ;0-Base Array
_ExcelWriteSheetFromArray($oExcel, $aArray, 1, 1, 0, 0) ;0-Base Array parameters

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out