#include <Debug.au3>

_DebugSetup("_DebugReportVar examples", True)

Local $Array2D[5][2]
For $r = 0 To UBound($Array2D, 1) - 1
	For $c = 0 To UBound($Array2D, 2) - 1
		$Array2D[$r][$c] = $r & "," & $c
	Next
Next
_DebugReportVar("Array2D", $Array2D)

Local $Array[7] = [1, 1.1, "string", Binary(0x010203), Ptr(-1), False, Default]
_DebugReportVar("Array", $Array)

Local $Array3D[5][2][10]
_DebugReportVar("Array3D", $Array3D)

Local $int = -1
_DebugReportVar("int", $int)

Local $int64 = 2 ^ 63
_DebugReportVar("int64", $int64)

Local $bool = True
_DebugReportVar("bool", $bool)

Local $float = 1.1
_DebugReportVar("float", $float)

Local $keyword = Default
_DebugReportVar("keyword", $keyword)

Local $string = "stringstring"
_DebugReportVar("string", $string)

Local $binary = Binary("0x0102030405060708")
_DebugReportVar("binary", $binary)

$binary = Binary("abcdefghij")
_DebugReportVar("binary", $binary)

Local $ptr = Ptr(0)
_DebugReportVar("ptr", $ptr)

Local $hwnd = WinActive("", "")
_DebugReportVar("hwnd", $hwnd)

Local $dllstruct = DllStructCreate("int")
_DebugReportVar("dllstruct", $dllstruct)

Local $obj = ObjCreate("shell.application")
_DebugReportVar("obj", $obj)
