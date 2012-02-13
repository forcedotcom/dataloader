; ***************************************************************
; Example 1 - Write a Hyperlink to a Cell, then Save and Close file.
; *****************************************************************

#include <Excel.au3>

Local $oExcel = _ExcelBookNew() ;Create new book, make it visible

Local $sLinkText = "AutoIt Website" ;Text Viewed in the Cell, equivalent to OuterText
Local $sAddress = "http://www.AutoItScript.com" ;Actual Link, equivalent to href
Local $sScreenTip = "AutoIt is Awesome! And Don't You Forget it!" ;The Screen Tip that Appears on MouseOver
_ExcelHyperlinkInsert($oExcel, $sLinkText, $sAddress, $sScreenTip, 1, 2) ;Insert At Row 1 Column 2

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out
