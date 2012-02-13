#include <GUIConstantsEx.au3>
#include <GuiListView.au3>

$Debug_LV = False ; Check ClassName being passed to ListView functions, set to True and use a handle to another control to see it work

_Main()

Func _Main()
	Local $iI, $iTimer, $hListView

	; Create GUI
	GUICreate("ListView Add Array", 400, 300)
	$hListView = GUICtrlCreateListView("", 2, 2, 394, 268)
	GUISetState()

	; Add columns
	_GUICtrlListView_AddColumn($hListView, "Items", 100)
	_GUICtrlListView_AddColumn($hListView, "SubItems 1", 100)
	_GUICtrlListView_AddColumn($hListView, "SubItems 2", 100)
	_GUICtrlListView_AddColumn($hListView, "SubItems 3", 100)

	_GUICtrlListView_SetItemCount($hListView, 5000)

	; One column load
	Local $aItems[5000][1]
	For $iI = 0 To UBound($aItems) - 1
		$aItems[$iI][0] = "Item " & $iI
	Next
	$iTimer = TimerInit()
	_GUICtrlListView_AddArray($hListView, $aItems)
	MsgBox(4160, "Information", "Load time: " & TimerDiff($iTimer) / 1000 & " seconds")

	_GUICtrlListView_DeleteAllItems(GUICtrlGetHandle($hListView)) ; items added with UDF function can be deleted using UDF function

	; Four column load
	Dim $aItems[5000][4]
	For $iI = 0 To UBound($aItems) - 1
		$aItems[$iI][0] = "Item " & $iI
		$aItems[$iI][1] = "Item " & $iI & "-1"
		$aItems[$iI][2] = "Item " & $iI & "-2"
		$aItems[$iI][3] = "Item " & $iI & "-3"
	Next
	$iTimer = TimerInit()
	_GUICtrlListView_AddArray($hListView, $aItems)
	MsgBox(4160, "Information", "Load time: " & TimerDiff($iTimer) / 1000 & " seconds")

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main
