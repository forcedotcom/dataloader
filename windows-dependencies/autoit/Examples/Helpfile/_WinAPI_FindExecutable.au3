#include <WinAPI.au3>
MsgBox(4096, "Find Executable", "file " & @ScriptName & @LF & "Executable: " & _WinAPI_FindExecutable(@ScriptName))
