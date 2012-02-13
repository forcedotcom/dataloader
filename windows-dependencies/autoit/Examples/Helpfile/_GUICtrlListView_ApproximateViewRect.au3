#include <GUIConstantsEx.au3>
#include <GuiListView.au3>
#include <Constants.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $aXY, $hListView

	GUICreate("ListView Approximate View Rect", 400, 300)
	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268)
	GUISetState()

	; Add column
	_GUICtrlListView_InsertColumn($hListView, 0, "Column 1", 100)

	; Add items
	For $iI = 0 To 9
		_GUICtrlListView_AddItem($hListView, "Row " & $iI)
	Next

	MsgBox(4096, "Information", "Approximate View Rect")
	; Resize view
	$aXY = _GUICtrlListView_ApproximateViewRect($hListView)
	_WinAPI_SetWindowPos(GUICtrlGetHandle($hListView), 0, 2, 2, $aXY[0], $aXY[1], $SWP_NOZORDER)
	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
