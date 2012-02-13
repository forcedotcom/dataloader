; ***************************************************************
; Example 1 - After opening a workbook and returning its object identifier, Add a New Sheet
; *****************************************************************
#include <Excel.au3>

Local $oExcel = _ExcelBookNew() ;Create new book, make it visible

_ExcelSheetAddNew($oExcel, "New Sheet Example")

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out