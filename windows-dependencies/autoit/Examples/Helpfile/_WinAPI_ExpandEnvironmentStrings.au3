#include <WinAPI.au3>
MsgBox(4096, "Environment string", "%windir% = " & _WinAPI_ExpandEnvironmentStrings("%windir%"))
