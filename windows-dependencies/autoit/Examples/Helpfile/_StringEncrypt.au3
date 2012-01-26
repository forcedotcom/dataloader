#include <GUIConstantsEx.au3>
#include <String.au3>

_Main()

Func _Main()
	Local $WinMain, $EditText, $InputPass, $InputLevel, $EncryptButton, $DecryptButton, $string
	; GUI and String stuff
	$WinMain = GUICreate('Encryption tool', 400, 400)
	; Creates window
	$EditText = GUICtrlCreateEdit('', 5, 5, 380, 350)
	; Creates main edit
	$InputPass = GUICtrlCreateInput('', 5, 360, 100, 20, 0x21)
	; Creates the password box with blured/centered input
	$InputLevel = GUICtrlCreateInput(1, 110, 360, 50, 20, 0x2001)
	GUICtrlSetLimit(GUICtrlCreateUpdown($InputLevel), 10, 1)
	; These two make the level input with the Up|Down ability
	$EncryptButton = GUICtrlCreateButton('Encrypt', 170, 360, 105, 35)
	; Encryption button
	$DecryptButton = GUICtrlCreateButton('Decrypt', 285, 360, 105, 35)
	; Decryption button
	GUICtrlCreateLabel('Password', 5, 385)
	GUICtrlCreateLabel('Level', 110, 385)
	; Simple text labels so you know what is what
	GUISetState()
	; Shows window

	While 1
		Switch GUIGetMsg()
			Case $GUI_EVENT_CLOSE
				ExitLoop
			Case $EncryptButton
				GUISetState(@SW_DISABLE, $WinMain) ; Stops you from changing anything
				$string = GUICtrlRead($EditText) ; Saves the editbox for later
				GUICtrlSetData($EditText, 'Please wait while the text is Encrypted/Decrypted.') ; Friendly message
				GUICtrlSetData($EditText, _StringEncrypt(1, $string, GUICtrlRead($InputPass), GUICtrlRead($InputLevel)))
				; Calls the encryption. Sets the data of editbox with the encrypted string
				; The encryption starts with 1/0 to tell it to encrypt/decrypt
				; The encryption then has the string that we saved for later from edit box
				; It then reads the password box & Reads the level box
				GUISetState(@SW_ENABLE, $WinMain) ; This turns the window back on
			Case $DecryptButton
				GUISetState(@SW_DISABLE, $WinMain) ; Stops you from changing anything
				$string = GUICtrlRead($EditText) ; Saves the editbox for later
				GUICtrlSetData($EditText, 'Please wait while the text is Encrypted/Decrypted.') ; Friendly message
				GUICtrlSetData($EditText, _StringEncrypt(0, $string, GUICtrlRead($InputPass), GUICtrlRead($InputLevel)))
				; Calls the encryption. Sets the data of editbox with the encrypted string
				; The encryption starts with 1/0 to tell it to encrypt/decrypt
				; The encryption then has the string that we saved for later from edit box
				; It then reads the password box & Reads the level box
				GUISetState(@SW_ENABLE, $WinMain) ; This turns the window back on
		EndSwitch
	WEnd ; Continue loop untill window is closed
	Exit
EndFunc   ;==>_Main
