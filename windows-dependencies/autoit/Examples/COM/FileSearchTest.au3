; AutoItCOM 3.1.1
;
; Test File
;
; Tests the SearchFiles() function of Microsoft Excel


Func SearchFiles($strFileSpec, $Subdirs = 0)

 $strFileList=""

 $oXlApp = ObjCreate("Excel.Application")

 $fsoFileSearch = $oXlApp.FileSearch

 If @error then 
  Msgbox(0,"SearchFiles","Error opening FileSearch Object")
 Else
   With $fsoFileSearch
      .NewSearch
      .LookIn = "c:\"
      .FileName = $strFileSpec
      .SearchSubFolders = $SubDirs

      $Number = .Execute()
      If $Number > 0 Then
	    For $i = 1 To .FoundFiles.Count
		$strFileList = $strFileList & .FoundFiles($i) & @CRLF
	    Next 
      EndIf
   EndWith
 Endif
 
 $fsoFileSearch = ""

 $oxlApp.quit

 $oxlApp=""

 Return $strFileList
EndFunc



; Example usage:

$Result = SearchFiles(@WindowsDir & "\*.txt",0)

MsgBox(0,"FileSearch Object test", "SearchFiles on '" & @WindowsDir & "\*.txt' resulted in:" & @CRLF & @CRLF & $Result )

