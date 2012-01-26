Local $dll = DllOpen("user32.dll")
Local $result = DllCall($dll, "int", "MessageBox", "hwnd", 0, "str", "Some text", "str", "Some title", "int", 0)
DllClose($dll)
