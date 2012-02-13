#include <WinAPI.au3>
#include <WindowsConstants.au3>

_Main()

Func _Main()
	Local $hwnd, $hparent
	$hwnd = GUICreate("test")
	$hparent = _WinAPI_GetAncestor($hwnd, $GA_PARENT)
	MsgBox(4096, "Parent", "Get Ancestor of " & $hwnd & ": " & $hparent)
	MsgBox(4096, "Root", "Get Ancestor of " & $hparent & ": " & _WinAPI_GetAncestor($hwnd, $GA_ROOT))
	MsgBox(4096, "Root Owner", "Get Ancestor of " & $hparent & ": " & _WinAPI_GetAncestor($hwnd, $GA_ROOTOWNER))
EndFunc   ;==>_Main
