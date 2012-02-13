#include <Crypt.au3>
#include <ComboConstants.au3>
#include <EditConstants.au3>
#include <GUIConstantsEx.au3>
#include <WinAPI.au3>
#include <WindowsConstants.au3>

Global $hKey = -1, $iInputEdit = -1, $iOutputEdit = -1

GUICreate("Realtime Encryption", 400, 320)
$iInputEdit = GUICtrlCreateEdit("", 0, 0, 400, 150, $ES_WANTRETURN)
$iOutputEdit = GUICtrlCreateEdit("", 0, 150, 400, 150, $ES_READONLY)
Local $iCombo = GUICtrlCreateCombo("", 0, 300, 100, 20, $CBS_DROPDOWNLIST)
GUICtrlSetData(-1, "3DES|AES (128bit)|AES (192bit)|AES (256bit)|DES|RC2|RC4", "RC4")
GUIRegisterMsg($WM_COMMAND, "WM_COMMAND")
GUISetState(@SW_SHOW)

_Crypt_Startup() ; To optimize performance start the crypt library.

Local $bAlgorithm = $CALG_RC4
$hKey = _Crypt_DeriveKey("CryptPassword", $bAlgorithm) ; Declare a password string and algorithm to create a cryptographic key.

While 1
	Switch GUIGetMsg()
		Case $GUI_EVENT_CLOSE
			Exit

		Case $iCombo ; Check when the combobox is selected and retrieve the correct algorithm.
			Switch GUICtrlRead($iCombo) ; Read the combobox selection.
				Case "3DES"
					$bAlgorithm = $CALG_3DES

				Case "AES (128bit)"
					If @OSVersion = "WIN_2000" Then
						MsgBox(16, "Error", "Sorry, this algorithm is not available on Windows 2000.") ; Show an error if the system is Windows 2000.
						ContinueLoop
					EndIf
					$bAlgorithm = $CALG_AES_128

				Case "AES (192bit)"
					If @OSVersion = "WIN_2000" Then
						MsgBox(16, "Error", "Sorry, this algorithm is not available on Windows 2000.")
						ContinueLoop
					EndIf
					$bAlgorithm = $CALG_AES_192

				Case "AES (256bit)"
					If @OSVersion = "WIN_2000" Then
						MsgBox(16, "Error", "Sorry, this algorithm is not available on Windows 2000.")
						ContinueLoop
					EndIf
					$bAlgorithm = $CALG_AES_256

				Case "DES"
					$bAlgorithm = $CALG_DES

				Case "RC2"
					$bAlgorithm = $CALG_RC2

				Case "RC4"
					$bAlgorithm = $CALG_RC4

			EndSwitch

			_Crypt_DestroyKey($hKey) ; Destroy the cryptographic key.
			$hKey = _Crypt_DeriveKey("CryptPassword", $bAlgorithm) ; Re-declare a password string and algorithm to create a new cryptographic key.

			Local $sRead = GUICtrlRead($iInputEdit)
			If StringStripWS($sRead, 8) <> "" Then ; Check there is text available to encrypt.
				Local $bEncrypted = _Crypt_EncryptData($sRead, $hKey, $CALG_USERKEY) ; Encrypt the text with the new cryptographic key.
				GUICtrlSetData($iOutputEdit, $bEncrypted) ; Set the output box with the encrypted text.
			EndIf
	EndSwitch
WEnd

_Crypt_DestroyKey($hKey) ; Destroy the cryptographic key.
_Crypt_Shutdown() ; Shutdown the crypt library.

Func WM_COMMAND($hWnd, $iMsg, $wParam, $lParam)
	#forceref $hWnd, $iMsg, $lParam

	Switch _WinAPI_LoWord($wParam)
		Case $iInputEdit
			Switch _WinAPI_HiWord($wParam)
				Case $EN_CHANGE
					Local $bEncrypted = _Crypt_EncryptData(GUICtrlRead($iInputEdit), $hKey, $CALG_USERKEY) ; Encrypt the text with the cryptographic key.
					GUICtrlSetData($iOutputEdit, $bEncrypted) ; Set the output box with the encrypted text.
			EndSwitch
	EndSwitch
EndFunc   ;==>WM_COMMAND
