; ***************************************************************
; Example 1 - After opening a workbook and returning its object identifier, create and display an array of all the Sheet Names in the workbook
; *****************************************************************

#include <Excel.au3>
#include <Array.au3>

Local $oExcel = _ExcelBookNew() ;Create new book, make it visible

Local $aArray = _ExcelSheetList($oExcel)
_ArrayDisplay($aArray, "All The WorkSheets In this WorkBook")

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out

; ***************************************************************
; Example 2 - After opening a workbook and returning its object identifier, create an array of all the Sheet Names in the workbook
;				and Activate each Sheet by String Name
; *****************************************************************

#include <Excel.au3>

$oExcel = _ExcelBookNew() ;Create new book, make it visible

$aArray = _ExcelSheetList($oExcel)

For $i = $aArray[0] To 1 Step -1 ;Work backwards through loop
	_ExcelSheetActivate($oExcel, $aArray[$i]) ;Using the String Name returned in the Array Elements
	MsgBox(0, "ActiveSheet", "The Active Sheet should be:" & @CRLF & $aArray[$i])
Next

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out

; ***************************************************************
; Example 3 - After opening a workbook and returning its object identifier, create an array of all the Sheet Names in the workbook
;				and Activate each Sheet by Index
; *****************************************************************

#include <Excel.au3>

$oExcel = _ExcelBookNew() ;Create new book, make it visible

$aArray = _ExcelSheetList($oExcel)

For $i = $aArray[0] To 1 Step -1 ;Work backwards through loop
	_ExcelSheetActivate($oExcel, $i) ;Using the Index of the Array
	MsgBox(0, "ActiveSheet", "The Active Sheet should be:" & @CRLF & $aArray[$i])
Next

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out

; ***************************************************************
; Example 4 - After opening a workbook and returning its object identifier, create an array of all the Sheet Names in the workbook
;				and Activate each Sheet by Index.  On each sheet Write the array to the worksheet and put in some random numbers
; *****************************************************************

#include <Excel.au3>

$oExcel = _ExcelBookNew() ;Create new book, make it visible

$aArray = _ExcelSheetList($oExcel)

For $i = $aArray[0] To 1 Step -1 ;Work backwards through loop
	_ExcelSheetActivate($oExcel, $i) ;Using the Index of the Array
	_ExcelWriteArray($oExcel, 1, 1, $aArray, 1) ;Write the Array to the Active Worksheet
	; We can fill-up some cells using a simple loop and random Numbers
	For $y = 2 To 10
		For $x = 2 To 10
			_ExcelWriteCell($oExcel, Round(Random(1000, 10000), 0), $x, $y) ;Some random numbers to file
		Next
	Next
	MsgBox(0, "ActiveSheet", "The Active Sheet should be:" & @CRLF & $aArray[$i])
Next

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out
