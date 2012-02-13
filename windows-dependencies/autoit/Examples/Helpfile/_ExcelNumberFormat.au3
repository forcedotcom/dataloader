; ***************************************************************
; Example 1 - Write to a Cell using a Loop, after opening a workbook and returning its object identifier.  Format Numbers, then Save and Close file.
; *****************************************************************

#include <Excel.au3>

Local $oExcel = _ExcelBookNew() ;Create new book, make it visible

; We can fill-up some cells using a simple loop and random Numbers
For $y = 1 To 10
	For $x = 1 To 10
		_ExcelWriteCell($oExcel, Random(1000, 10000), $x, $y) ;Some random numbers to file
	Next
Next

Local $sFormat = "$#,##0.00" ;Format String tells _ExcelNumberFormat to make it a $ currency
_ExcelNumberFormat($oExcel, $sFormat, 1, 1, 5, 5) ;Start on Row 1, Start on Column 1, End on Row 5, End on Column 5

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out

; ***************************************************************
; Example 2 - Write to a Cell using a Loop, after opening a workbook and returning its object identifier.  Format Numbers, then Save and Close file.
; *****************************************************************

#include <Excel.au3>

$oExcel = _ExcelBookNew() ;Create new book, make it visible
Local $aFormatExamples[5] = ["Format Examples", "General", "hh:mm:ss", "$#,##0.00", "[Red]($#,##0.00)"] ;Array to Create Headers

For $i = 0 To UBound($aFormatExamples) - 1 ;Use loop to write headers
	_ExcelWriteCell($oExcel, $aFormatExamples[$i], 1, $i + 1) ; +1 to $i so that 0-base index and row match
Next

; We can fill-up some cells using a simple loop and random Numbers
For $y = 2 To 5 ;Start on Column 2
	For $x = 2 To 10
		_ExcelWriteCell($oExcel, Random(1000, 10000), $x, $y) ;Some random numbers to file
	Next
Next

ToolTip("Formatting Column(s) Soon...")
Sleep(3500) ;Pause to let user view action

; We can format using a simple loop
; Each Column will have a differnt type of Format
For $i = 1 To UBound($aFormatExamples) - 1
	_ExcelNumberFormat($oExcel, $aFormatExamples[$i], 2, $i, 11, $i)
Next

$oExcel.Columns.AutoFit ;AutoFits the Columns for better viewing
$oExcel.Rows.AutoFit ;AutoFits the Rows for better viewing

MsgBox(0, "Exiting", "Press OK to Save File and Exit")
_ExcelBookSaveAs($oExcel, @TempDir & "\Temp.xls", "xls", 0, 1) ; Now we save it into the temp directory; overwrite existing file if necessary
_ExcelBookClose($oExcel) ; And finally we close out
