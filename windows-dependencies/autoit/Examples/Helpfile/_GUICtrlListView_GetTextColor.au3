#include <GUIConstantsEx.au3>
#include <GuiListView.au3>
#include <Constants.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $hListView

	GUICreate("ListView Get Text Color", 400, 300)
	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268)
	GUISetState()

	; Set colors
	_GUICtrlListView_SetBkColor($hListView, $CLR_MONEYGREEN)
	_GUICtrlListView_SetTextColor($hListView, $CLR_BLACK)
	_GUICtrlListView_SetTextBkColor($hListView, $CLR_MONEYGREEN)

	; Add columns
	_GUICtrlListView_AddColumn($hListView, "Items", 100)

	; Add items
	_GUICtrlListView_BeginUpdate($hListView)
	For $iI = 1 To 10
		_GUICtrlListView_AddItem($hListView, "Item " & $iI)
	Next
	_GUICtrlListView_EndUpdate($hListView)

	; Show clors
	MsgBox(4160, "Information", "Back Color ....: " & _GUICtrlListView_GetBkColor($hListView) & @CRLF & _
			"Text Color ....: " & _GUICtrlListView_GetTextColor($hListView) & @CRLF & _
			"Text Back Color: " & _GUICtrlListView_GetTextBkColor($hListView))

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
