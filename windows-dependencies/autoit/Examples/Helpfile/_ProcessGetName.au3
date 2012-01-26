#include <Process.au3>

Run("notepad.exe")
WinWaitActive("[CLASS:Notepad]", "")
Local $pid = WinGetProcess("[CLASS:Notepad]", "")
Local $name = _ProcessGetName($pid)

MsgBox(0, "Notepad - " & $pid, $name)
