; *****************************************************************
; Example 1 - After opening a workbook and returning its object identifier: Sets the horizontal alignment of each cell in a range.
; *****************************************************************

#include <Excel.au3>

Local $oExcel = _ExcelBookNew() ;Create new book, make it visible
Local $sRangeOrRowStart = 1, $iColStart = 1, $iRowEnd = 10, $iColEnd = 10, $sHorizAlign = "left"

; We can fill-up some cells using a simple loop and random Numbers
For $i = 1 To 10
	For $j = 1 To 10
		_ExcelWriteCell($oExcel, Round(Random(1, 100), 0), $i, $j) ;Round off some random numbers to file
	Next
Next

MsgBox(0, "_ExcelHorizontalAlignSet", "Notice the Alignment" & @CRLF & "Press OK to Continue")

_ExcelHorizontalAlignSet($oExcel, $sRangeOrRowStart, $iColStart, $iRowEnd, $iColEnd, $sHorizAlign)
MsgBox(0, "_ExcelHorizontalAlignSet", "Alignment should be 'left'")

$sHorizAlign = "center"
_ExcelHorizontalAlignSet($oExcel, $sRangeOrRowStart, $iColStart, $iRowEnd, $iColEnd, $sHorizAlign)
MsgBox(0, "_ExcelHorizontalAlignSet", "Alignment should be 'center'")

$sHorizAlign = "right"
_ExcelHorizontalAlignSet($oExcel, $sRangeOrRowStart, $iColStart, $iRowEnd, $iColEnd, $sHorizAlign)
MsgBox(0, "_ExcelHorizontalAlignSet", "Alignment should be 'right'")

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out