;===============================================================================
;
; Description:      Show all icons in the given file
; Requirement(s):   Autoit 3.0.103+
; Author(s):        YDY (Lazycat)
;
;===============================================================================

#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <ButtonConstants.au3>
#include <StaticConstants.au3>

; Setting variables
Global $ahIcons[30], $ahLabels[30]
Global $iStartIndex = 1, $iCntRow, $iCntCol, $iCurIndex
Global $sFilename = @SystemDir & "\shell32.dll"; Default file is "shell32.dll"
Global $iOrdinal = -1

Global $hPrev

_Main()

Func _Main()
	Local $hGui, $hFile, $hFileSel, $hNext, $hToggle
	Local $iMsg, $sCurFilename, $sTmpFile

	; Creating GUI and controls
	$hGui = GUICreate("Icon Selector by Ordinal value", 385, 435, @DesktopWidth / 2 - 192, _
			@DesktopHeight / 2 - 235, -1, $WS_EX_ACCEPTFILES)
	GUICtrlCreateGroup("", 5, 1, 375, 40)
	GUICtrlCreateGroup("", 5, 50, 375, 380)
	$hFile = GUICtrlCreateInput($sFilename, 12, 15, 325, 16, -1, $WS_EX_STATICEDGE)
	GUICtrlSetState(-1, $GUI_DROPACCEPTED)
	GUICtrlSetTip(-1, "You can drop files from shell here...")
	$hFileSel = GUICtrlCreateButton("...", 345, 14, 26, 18)
	$hPrev = GUICtrlCreateButton("Previous", 10, 45, 60, 24, $BS_FLAT)
	GUICtrlSetState(-1, $GUI_DISABLE)
	$hNext = GUICtrlCreateButton("Next", 75, 45, 60, 24, $BS_FLAT)
	$hToggle = GUICtrlCreateButton("by Name", 300, 45, 60, 24, $BS_FLAT)

	; This code build two arrays of ID's of icons and labels for easily update
	For $iCntRow = 0 To 4
		For $iCntCol = 0 To 5
			$iCurIndex = $iCntRow * 6 + $iCntCol
			$ahIcons[$iCurIndex] = GUICtrlCreateIcon($sFilename, $iOrdinal * ($iCurIndex + 1), _
					60 * $iCntCol + 25, 70 * $iCntRow + 80)
			$ahLabels[$iCurIndex] = GUICtrlCreateLabel($iOrdinal * ($iCurIndex + 1), _
					60 * $iCntCol + 11, 70 * $iCntRow + 115, 60, 20, $SS_CENTER)
		Next
	Next

	GUISetState()

	While 1
		$iMsg = GUIGetMsg()
		; Code below will check if the file is dropped (or selected)
		$sCurFilename = GUICtrlRead($hFile)
		If $sCurFilename <> $sFilename Then
			$iStartIndex = 1
			$sFilename = $sCurFilename
			_GUIUpdate()
		EndIf
		; Main "Select" statement that handles other events
		Select
			Case $iMsg = $hFileSel
				$sTmpFile = FileOpenDialog("Select file:", "::{20D04FE0-3AEA-1069-A2D8-08002B30309D}", "Executables & dll's (*.exe;*.dll;*.ocx;*.icl)")
				If @error Then ContinueLoop
				GUICtrlSetData($hFile, $sTmpFile); GUI will be updated at next iteration
			Case $iMsg = $hPrev
				$iStartIndex = $iStartIndex - 30
				_GUIUpdate()
			Case $iMsg = $hNext
				$iStartIndex = $iStartIndex + 30
				_GUIUpdate()
			Case $iMsg = $hToggle
				If $iOrdinal = -1 Then
					$iOrdinal = 1
					GUICtrlSetData($hToggle, "by Ordinal")
					WinSetTitle($hGui, "", "Icon Selector by Name value")
				Else
					$iOrdinal = -1
					GUICtrlSetData($hToggle, "by Name")
					WinSetTitle($hGui, "", "Icon Selector by Ordinal value")
				EndIf
				_GUIUpdate()
			Case $iMsg = $GUI_EVENT_CLOSE
				Exit
		EndSelect
	WEnd
EndFunc   ;==>_Main

; Just updates GUI icons, labels and set state of "Previous" button
Func _GUIUpdate()
	For $iCntRow = 0 To 4
		For $iCntCol = 0 To 5
			$iCurIndex = $iCntRow * 6 + $iCntCol
			GUICtrlSetImage($ahIcons[$iCurIndex], $sFilename, $iOrdinal * ($iCurIndex + $iStartIndex))
			If $iOrdinal = -1 Then
				GUICtrlSetData($ahLabels[$iCurIndex], -($iCurIndex + $iStartIndex))
			Else
				GUICtrlSetData($ahLabels[$iCurIndex], '"' & ($iCurIndex + $iStartIndex) & '"')
			EndIf
		Next
	Next
	; This is because we don't want negative values
	If $iStartIndex = 1 Then
		GUICtrlSetState($hPrev, $GUI_DISABLE)
	Else
		GUICtrlSetState($hPrev, $GUI_ENABLE)
	EndIf
EndFunc   ;==>_GUIUpdate
