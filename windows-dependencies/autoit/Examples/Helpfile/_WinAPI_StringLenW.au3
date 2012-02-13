#include <WinAPI.au3>

; Make the string buffer. It's "wchar" type structure. Choosing the size of 64 characters.
Local $tStringBuffer = DllStructCreate("wchar Data[64]")
; Fill it with some data
DllStructSetData($tStringBuffer, "Data", "Gongoozle")

MsgBox(262144, "_WinAPI_StringLenW", "Length of a string inside the buffer is " & _WinAPI_StringLenW($tStringBuffer) & " characters.")
