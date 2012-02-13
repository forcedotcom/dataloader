;====================================================
;============= Example of a child window ============
;====================================================
; AutoIt version: 3.0.103
; Language:       English
; Author:         "SlimShady"
;
; ----------------------------------------------------------------------------
; Script Start
; ----------------------------------------------------------------------------

#include <GUIConstantsEx.au3>


_Main()

Func _Main()

	;Initialize variables
	Local $GUIWidth = 250, $GUIHeight = 250
	Local $ParentWin, $ParentWin_Pos, $ChildWin, $msg

	;Create main/parent window
	$ParentWin = GUICreate("Parent GUI", $GUIWidth, $GUIHeight)
	;Save the position of the parent window
	$ParentWin_Pos = WinGetPos($ParentWin, "")
	;Show the parent window/Make the parent window visible
	GUISetState(@SW_SHOW)

	;Create child window and add the parameter to make it the child of the parent window
	$ChildWin = GUICreate("Child GUI", $GUIWidth, $GUIHeight, $ParentWin_Pos[0] + 100, $ParentWin_Pos[1] + 100, -1, -1, $ParentWin)
	;Show the child window/Make the child window visible
	GUISetState(@SW_SHOW)

	;Switch to the parent window
	GUISwitch($ParentWin)

	;Loop until:
	;- user presses Esc when focused to the parent window
	;- user presses Alt+F4 when focused to the parent window
	;- user clicks the close button of the parent window
	While 1
		;After every loop check if the user clicked something in the GUI windows
		$msg = GUIGetMsg(1)
		Select
			;Check if user clicked on a close button of any of the 2 windows
			Case $msg[0] = $GUI_EVENT_CLOSE
				;Check if user clicked on the close button of the child window
				If $msg[1] = $ChildWin Then
					MsgBox(64, "Test", "Child GUI will now close.")
					;Switch to the child window
					GUISwitch($ChildWin)
					;Destroy the child GUI including the controls
					GUIDelete()
					;Check if user clicked on the close button of the parent window
				ElseIf $msg[1] = $ParentWin Then
					MsgBox(64, "Test", "Parent GUI will now close.")
					;Switch to the parent window
					GUISwitch($ParentWin)
					;Destroy the parent GUI including the controls
					GUIDelete()
					;Exit the script
					Exit
				EndIf

		EndSelect

	WEnd
EndFunc   ;==>_Main
