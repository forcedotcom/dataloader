#include <GUIConstantsEx.au3>
#include <GuiAVI.au3>

$Debug_AVI = False ; Check ClassName being passed to AVI functions, set to True and use a handle to another control to see it work

Global $hAVI

_Main()

Func _Main()
	Local $Wow64 = ""
	If @AutoItX64 Then $Wow64 = "\Wow6432Node"
	Local $hGUI, $sFile = RegRead("HKEY_LOCAL_MACHINE\SOFTWARE" & $Wow64 & "\AutoIt v3\AutoIt", "InstallDir") & "\Examples\GUI\SampleAVI.avi"

	; Create GUI
	$hGUI = GUICreate("(External) AVI Destroy", 300, 100)
	$hAVI = _GUICtrlAVI_Create($hGUI, "", -1, 10, 10)
	GUISetState()

	; Play the sample AutoIt AVI
	_GUICtrlAVI_Open($hAVI, $sFile)

	; Play the sample AutoIt AVI
	_GUICtrlAVI_Play($hAVI)

	Sleep(3000)

	; Stop AVI clip after 3 seconds
	_GUICtrlAVI_Stop($hAVI)

	; Close AVI clip
	_GUICtrlAVI_Close($hAVI)

	MsgBox(4160, "Information", "Destroy AVI Control")
	_GUICtrlAVI_Destroy($hAVI)

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE



	GUIDelete()
EndFunc   ;==>_Main
