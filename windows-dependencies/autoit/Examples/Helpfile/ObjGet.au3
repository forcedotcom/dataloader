; Example getting an Object using it's class name
;
; Excel must be activated for this example to be successfull

Local $oExcel = ObjGet("", "Excel.Application") ; Get an existing Excel Object

If @error Then
	MsgBox(0, "ExcelTest", "Error Getting an active Excel Object. Error code: " & Hex(@error, 8))
	Exit
EndIf

$oExcel.Visible = 1 ; Let the guy show himself
$oExcel.workbooks.add ; Add a new workbook
Exit



; Example getting an Object using a file name
;
; An Excel file with filename Worksheet.xls must be created in the root directory
; of the C:\ drive in order for this example to work.

Local $FileName = "C:\Worksheet.xls"

If Not FileExists($FileName) Then
	MsgBox(0, "Excel File Test", "Can't run this test, because you didn't create the Excel file " & $FileName)
	Exit
EndIf

Local $oExcelDoc = ObjGet($FileName) ; Get an Excel Object from an existing filename

If IsObj($oExcelDoc) Then

	; Tip: Uncomment these lines to make Excel visible (credit: DaleHohm)
	; $oExcelDoc.Windows(1).Visible = 1; Set the first worksheet in the workbook visible
	; $oExcelDoc.Application.Visible = 1; Set the application visible (without this Excel will exit)

	Local $String = "" ; String for displaying purposes

	; Some document properties do not return a value, we will ignore those.
	Local $OEvent = ObjEvent("AutoIt.Error", "nothing"); Equal to VBscript's On Error Resume Next

	For $Property In $oExcelDoc.BuiltinDocumentProperties
		$String = $String & $Property.Name & ":" & $Property.Value & @CRLF
	Next

	MsgBox(0, "Excel File Test", "The document properties of " & $FileName & " are:" & @CRLF & @CRLF & $String)

	$oExcelDoc.Close ; Close the Excel document

Else
	MsgBox(0, "Excel File Test", "Error: Could not open " & $FileName & " as an Excel Object.")
EndIf
