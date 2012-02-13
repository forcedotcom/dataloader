; ***************************************************************
; Example 1 - Open a New workbook and returns its object identifier.  Then Save the file without any alerts.
; *****************************************************************

#include <Excel.au3>

Local $oExcel = _ExcelBookNew()

_ExcelBookSave($oExcel) ;Save File With No Alerts
If Not @error Then MsgBox(0, "Success", "File was Saved!", 3)
