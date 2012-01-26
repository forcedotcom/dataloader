#include <WinAPI.au3>

Local $hCurProcessPseudo = _WinAPI_GetCurrentProcess()
ConsoleWrite("Pseudo handle for current process = " & $hCurProcessPseudo & @CRLF)

Local $hCurProcess = _WinAPI_DuplicateHandle($hCurProcessPseudo, $hCurProcessPseudo, $hCurProcessPseudo, Default, True, $DUPLICATE_SAME_ACCESS)
ConsoleWrite("Real handle for current process = " & $hCurProcess & @CRLF)

;...

; Close handle when no longer needed
_WinAPI_CloseHandle($hCurProcess)

