Local $message = "Hold down Ctrl or Shift to choose multiple files."

Local $var = FileOpenDialog($message, @WindowsDir & "\", "Images (*.jpg;*.bmp)", 1 + 4)

If @error Then
	MsgBox(4096, "", "No File(s) chosen")
Else
	$var = StringReplace($var, "|", @CRLF)
	MsgBox(4096, "", "You chose " & $var)
EndIf


; Multiple filter group
$message = "Hold down Ctrl or Shift to choose multiple files."

$var = FileOpenDialog($message, @WindowsDir & "", "Images (*.jpg;*.bmp)|Videos (*.avi;*.mpg)", 1 + 4)

If @error Then
	MsgBox(4096, "", "No File(s) chosen")
Else
	$var = StringReplace($var, "|", @CRLF)
	MsgBox(4096, "", "You chose " & $var)
EndIf

