; *******************************************************
; Example 1 - calling the MessageBox API directly
; *******************************************************

Local $result = DllCall("user32.dll", "int", "MessageBox", "hwnd", 0, "str", "Some text", "str", "Some title", "int", 0)


; *******************************************************
; Example 2 - calling a function that modifies parameters
; *******************************************************

Local $hwnd = WinGetHandle("[CLASS:Notepad]")
$result = DllCall("user32.dll", "int", "GetWindowText", "hwnd", $hwnd, "str", "", "int", 32768)
MsgBox(0, "", $result[0]) ; number of chars returned
MsgBox(0, "", $result[2]) ; Text returned in param 2


; *******************************************************
; Example 3 - Show the Windows PickIconDlg
; *******************************************************

Local $sFileName = @SystemDir & '\shell32.dll'

; Create a structure to store the icon index
Local $stIcon = DllStructCreate("int")
Local $stString = DllStructCreate("wchar[260]")
Local $structsize = DllStructGetSize($stString) / 2
DllStructSetData($stString, 1, $sFileName)

; Run the PickIconDlg - '62' is the ordinal value for this function
DllCall("shell32.dll", "none", 62, "hwnd", 0, "ptr", DllStructGetPtr($stString), "int", $structsize, "ptr", DllStructGetPtr($stIcon))

$sFileName = DllStructGetData($stString, 1)
Local $nIconIndex = DllStructGetData($stIcon, 1)

; Show the new filename and icon index
MsgBox(0, "Info", "Last selected file: " & $sFileName & @LF & "Icon-Index: " & $nIconIndex)
