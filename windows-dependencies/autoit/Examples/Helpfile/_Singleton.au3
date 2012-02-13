#include <Misc.au3>
If _Singleton("test", 1) = 0 Then
	MsgBox(0, "Warning", "An occurence of test is already running")
	Exit
EndIf
MsgBox(0, "OK", "the first occurence of test is running")
