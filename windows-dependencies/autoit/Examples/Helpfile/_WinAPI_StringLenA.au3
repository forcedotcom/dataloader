#include <WinAPI.au3>

; Make the string buffer. It's "char" type structure. Choosing the size of 64 characters.
Local $tStringBuffer = DllStructCreate("char Data[64]")
; Fill it with some data
DllStructSetData($tStringBuffer, "Data", "Callipygian")

MsgBox(262144, "_WinAPI_StringLenA", "Length of a string inside the buffer is " & _WinAPI_StringLenA($tStringBuffer) & " characters.")
