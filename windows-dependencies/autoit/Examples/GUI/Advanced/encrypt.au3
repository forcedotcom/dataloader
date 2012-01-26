;====================================================
;============= Encryption Tool With GUI =============
;====================================================
; AutoIt version: 3.0.103
; Language:       English
; Author:         "Wolvereness"
;
; ----------------------------------------------------------------------------
; Script Start
; ----------------------------------------------------------------------------

#include <GUIConstantsEx.au3>
#include <String.au3>
;#include files for encryption and GUI constants

_Main()

Func _Main()
	Local $WinMain, $EditText, $InputLevel, $InputPass, $UpDownLevel
	Local $EncryptButton, $DecryptButton, $string
	#forceref $UpDownLevel
;~~
	$WinMain = GUICreate('Encryption tool', 400, 400)
	; Creates window
;~~
	$EditText = GUICtrlCreateEdit('', 5, 5, 380, 350)
	; Creates main edit
;~~
	$InputPass = GUICtrlCreateInput('', 5, 360, 100, 20, 0x21)
	; Creates the password box with blured/centered input
;~~
	$InputLevel = GUICtrlCreateInput(1, 110, 360, 50, 20, 0x2001)
	$UpDownLevel = GUICtrlSetLimit(GUICtrlCreateUpdown($InputLevel), 10, 1)
	; These two make the level input with the Up|Down ability
;~~
	$EncryptButton = GUICtrlCreateButton('Encrypt', 170, 360, 105, 35)
	$DecryptButton = GUICtrlCreateButton('Decrypt', 285, 360, 105, 35)
	; Encryption/Decryption buttons
;~~
	GUICtrlCreateLabel('Password', 5, 385)
	GUICtrlCreateLabel('Level', 110, 385)
	; Simple text labels so you know what is what
;~~
	GUISetState()
	; Shows window
;~~

	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
			Case $EncryptButton
				; When you press Encrypt
;~~
				GUISetState(@SW_DISABLE, $WinMain)
				; Stops you from changing anything
;~~
				$string = GUICtrlRead($EditText)
				; Saves the editbox for later
;~~
				GUICtrlSetData($EditText, 'Please wait while the text is Encrypted/Decrypted.')
				; Friendly message
;~~
				GUICtrlSetData($EditText, _StringEncrypt(1, $string, GUICtrlRead($InputPass), GUICtrlRead($InputLevel)))
				; Calls the encryption. Sets the data of editbox with the encrypted string
;~~
				GUISetState(@SW_ENABLE, $WinMain)
				; This turns the window back on
;~~
			Case $DecryptButton
				; When you press Decrypt
;~~
				GUISetState(@SW_DISABLE, $WinMain)
				; Stops you from changing anything
;~~
				$string = GUICtrlRead($EditText)
				; Saves the editbox for later
;~~
				GUICtrlSetData($EditText, 'Please wait while the text is Encrypted/Decrypted.')
				; Friendly message
;~~
				GUICtrlSetData($EditText, _StringEncrypt(0, $string, GUICtrlRead($InputPass), GUICtrlRead($InputLevel)))
				; Calls the encryption. Sets the data of editbox with the encrypted string
;~~
				GUISetState(@SW_ENABLE, $WinMain)
				; This turns the window back on
;~~
		EndSwitch
	WEnd
EndFunc   ;==>_Main
