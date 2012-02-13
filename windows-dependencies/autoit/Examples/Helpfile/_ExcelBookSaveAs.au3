; ***************************************************************
; Example 1 - Open an existing workbook and returns its object identifier.  Then SaveAs the file under a new name, without any alerts.
; *****************************************************************

#include <Excel.au3>

Local $sFilePath1 = @ScriptDir & "\Test.xls" ;This file should already exist
Local $oExcel = _ExcelBookOpen($sFilePath1)

If @error = 1 Then
	MsgBox(0, "Error!", "Unable to Create the Excel Object")
	Exit
ElseIf @error = 2 Then
	MsgBox(0, "Error!", "File does not exist - Shame on you!")
	Exit
EndIf

_ExcelBookSaveAs($oExcel, @TempDir & "\SaveAsExample", "xls")
If Not @error Then MsgBox(0, "Success", "File was Saved!", 3)
_ExcelBookClose($oExcel, 1, 0) ;This method will save then Close the file, without any of the normal prompts, regardless of changes


; ***************************************************************
; Example 2 - Open an existing workbook and returns its object identifier.  Then SaveAs the file under a new name, without any alerts.
;				Overwrite File if it exists, and protect the file using the password option. Then Open the File to show the Password Protection
; *****************************************************************

#include <Excel.au3>

$sFilePath1 = @ScriptDir & "\Test.xls" ;This file should already exist
$oExcel = _ExcelBookOpen($sFilePath1)

;Show any errors that might occur when Opening the File
If @error = 1 Then
	MsgBox(0, "Error!", "Unable to Create the Excel Object")
	Exit
ElseIf @error = 2 Then
	MsgBox(0, "Error!", "File does not exist - Shame on you!")
	Exit
EndIf

_ExcelBookSaveAs($oExcel, @TempDir & "\SaveAsExample2", "xls", 0, 1, "ReadOnly") ;Save the File as 'SaveAsExample2.xls"
If Not @error Then MsgBox(0, "Success", "File was Saved!", 3)
_ExcelBookClose($oExcel, 1, 0) ;This method will save then Close the file, without any of the normal prompts, regardless of changes

$oExcel = _ExcelBookOpen(@TempDir & "\SaveAsExample2.xls", 1, False) ;Open The previous File to show the password protection
