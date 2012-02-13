Local $MyDocsFolder = "::{450D8FBA-AD25-11D0-98A8-0800361B1103}"

Local $var = FileSaveDialog("Choose a name.", $MyDocsFolder, "Scripts (*.aut;*.au3)", 2)
; option 2 = dialog remains until valid path/file selected

If @error Then
	MsgBox(4096, "", "Save cancelled.")
Else
	MsgBox(4096, "", "You chose " & $var)
EndIf


; Multiple filter group
$var = FileSaveDialog("Choose a name.", $MyDocsFolder, "Scripts (*.aut;*.au3)|Text files (*.ini;*.txt)", 2)
; option 2 = dialog remains until valid path/file selected

If @error Then
	MsgBox(4096, "", "Save cancelled.")
Else
	MsgBox(4096, "", "You chose " & $var)
EndIf
