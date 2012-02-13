#include <WinAPI.au3>
MsgBox(4096, "ExtractIconEx", "# of Icons in file shell32.dll: " & _WinAPI_ExtractIconEx("shell32.dll", -1, 0, 0, 0))
