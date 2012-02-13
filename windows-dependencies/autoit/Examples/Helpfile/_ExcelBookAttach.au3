; **************************************************************************************************************
; Example 1 - Attach to the first existing instance of Microsoft Excel where the search string matches based on the selected mode.
; **************************************************************************************************************

#include <Excel.au3>
#include <File.au3>

Local $sFilePath = @TempDir & "\Temp.xls"
If Not _FileCreate($sFilePath) Then ;Create an .XLS file to attach to
	MsgBox(4096, "Error", " Error Creating File - " & @error)
EndIf

_ExcelBookOpen($sFilePath)
Local $oExcel = _ExcelBookAttach($sFilePath) ;with Default Settings ($s_mode = "FilePath" ==> Full path to the open workbook)
_ExcelWriteCell($oExcel, "If you can read this, then Success!", 1, 1) ;Write to the Cell
MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookClose($oExcel, 1, 0) ;This method will save then Close the file, without any of the normal prompts, regardless of changes

; **************************************************************************************************************
; Example 2 - Attach to the first existing instance of Microsoft Excel where the search string matches based on the selected mode.
; **************************************************************************************************************

#include <Excel.au3>
#include <File.au3>

$sFilePath = @TempDir & "\Temp.xls"
If Not _FileCreate($sFilePath) Then ;Create an .XLS file to attach to
	MsgBox(4096, "Error", " Error Creating File - " & @error)
EndIf

_ExcelBookOpen($sFilePath)
$oExcel = _ExcelBookAttach("Temp.xls", "FileName") ;with $s_mode = "FileName" ==> Name of the open workbook
_ExcelWriteCell($oExcel, "If you can read this, then Success!", 1, 1) ;Write to the Cell
MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookClose($oExcel, 1, 0) ;This method will save then Close the file, without any of the normal prompts, regardless of changes

; **************************************************************************************************************
; Example 3 - Attach to the first existing instance of Microsoft Excel where the search string matches based on the selected mode.
; **************************************************************************************************************

#include <Excel.au3>
#include <File.au3>

$sFilePath = @TempDir & "\Temp.xls"
If Not _FileCreate($sFilePath) Then ;Create an .XLS file to attach to
	MsgBox(4096, "Error", " Error Creating File - " & @error)
EndIf

_ExcelBookOpen($sFilePath)
$oExcel = _ExcelBookAttach("Microsoft Excel - Temp.xls", "Title") ;with $s_mode = "Title" ==> Title of the Excel window
_ExcelWriteCell($oExcel, "If you can read this, then Success!", 1, 1) ;Write to the Cell
MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookClose($oExcel, 1, 0) ;This method will save then Close the file, without any of the normal prompts, regardless of changes
