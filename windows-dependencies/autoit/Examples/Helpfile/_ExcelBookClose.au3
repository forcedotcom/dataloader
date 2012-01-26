; ***************************************************************
; Example 1 - Open a new Excel Window and Close it, with default parameters
; *****************************************************************

#include <Excel.au3>

Local $oExcel = _ExcelBookNew() ; Example 1 - Create a Microsoft Excel window
_ExcelBookClose($oExcel) ;By default, this method Saves the file under the "My Documents" folder

; ***************************************************************
; Example 2 - Open a new Excel Window and Close it, with default parameters
; *****************************************************************

#include <Excel.au3>

$oExcel = _ExcelBookNew() ; Example 1 - Create a Microsoft Excel window
_ExcelBookClose($oExcel, 0) ;This method will either: 1) Close the file, or 2) if a change has been made to the Excel Window, then Prompt the user

; ***************************************************************
; Example 3 - Open a new Excel Window and Close it, with default parameters
; *****************************************************************

#include <Excel.au3>

$oExcel = _ExcelBookNew() ; Example 1 - Create a Microsoft Excel window
_ExcelBookClose($oExcel, 1, 0) ;This method will save then Close the file, without any of the normal prompts, regardless of changes
