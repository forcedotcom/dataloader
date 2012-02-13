; Wscript.filesystem Example
;
; Based on AutoItCOM version 3.1.0
;
; Beta version 06-02-2005
;

$Folder = @TempDir	; Folder to test the size of 

$objFSO = ObjCreate("Scripting.FileSystemObject")

if @error then 
	Msgbox (0,"Wscript.filesystem Test","I'm sorry, but creation of object $objFSO failed. Error code: " & @error)
	exit
endif

$objFolder = $objFSO.GetFolder($Folder)	; Get object to the given folder

Msgbox (0,"Wscript.filesystem Test","Your " & $Folder & " folder size is: " & Round($objFolder.Size/1024) & " Kilobytes")


exit
