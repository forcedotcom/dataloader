; ExcelDataTest.AU3
; 
; Retrieves information from an Excel sheet
; Modifies the data
; Returns the modified data in back in the sheet


$FileName="C:\temp\test.xls"		; Change this to the path/name of your Excel sheet
$FileName=@ScriptDir & "\Worksheet.xls"
$CellRange="A1:E3"					; Change this to the range of cells you want to modify

if not FileExists($FileName) then	; Just a check to be sure..
	Msgbox (0,"Excel Data Test","Error: Can't find file " & $FileName)
	Exit
endif

$oExcelDoc = ObjGet($FileName)	; Get an Excel Object from an existing filename

If (not @error) and IsObj($oExcelDoc) then			; Check again if everything went well
													; NOTE: $oExcelDoc is a "Workbook Object", not Excel itself!	
    $oDocument=$oExcelDoc.Worksheets(1)				; We use the 'Default' worksheet
	
	$aArray=$oDocument.range($CellRange).value		; Retrieve the cell values from given range 

	; The data should now be in the 2-dimensional array: $aArray
	
	If IsArray($aArray) and Ubound($aArray,0)>0 then	; Check if we retrieved any data
		
		Msgbox (0,"Excel Data Test","Debugging information for retrieved cells:" & @CRLF & _
			"Number of dimensions: " & UBound($aArray,0) & @CRLF & _ 
			"Size of first dimension: " & Ubound($aArray,1) & @CRLF & _ 
			"Size of second dimension: " & Ubound($aArray,2))

		; The next lines are just for debugging purposes, it puts all cell values into
		; a string to display in a MsgBox.
		; Note: you can't use a FOR..IN loop for a multi-dimensional array!
	
		$string=""
		for $x=0 to ubound($aArray,1)-1
			for $y=0 to ubound ($aArray,2)-1
				$string=$string & "(" & $x & "," & $y & ")=" & $aArray[$x][$y] & @CRLF
			Next
		Next
		Msgbox(0,"Excel Data Test","Debug information: Read Cell contents: " & @CRLF & $string)


		; Now we modify the data
		$aArray[0][0]="This is cell A1"
		$aArray[1][1]="This is cell B2"
		
		$oDocument.range($CellRange).value	= $aArray	; Write the data back in one shot
		
		$oExcelDoc.Windows(1).Visible = True			; Otherwise the worksheet window will be saved 'hidden'
		$oExcelDoc.Save									; Save the workbook

	Else
		Msgbox (0,"Excel Data Test","Error: Could not retrieve data from cell range: " & $CellRange)
	EndIf
	
	$oExcelDoc.saved=1				; Prevent questions from excel to save the file
	$oExcelDoc.close				; Get rid of Excel.
Else
	Msgbox (0,"Excel Data Test","Error: Could not open "& $FileName & " as an Excel Object.")
Endif

$oExcelDoc=0