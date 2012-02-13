#include <Crypt.au3>
#include <ComboConstants.au3>
#include <GUIConstantsEx.au3>

Local $bAlgorithm = $CALG_SHA1

GUICreate("Hash File", 425, 70)
Local $iInput = GUICtrlCreateInput(@ScriptFullPath, 5, 5, 200, 20)
Local $iBrowse = GUICtrlCreateButton("...", 210, 5, 35, 20)
Local $iCombo = GUICtrlCreateCombo("", 250, 5, 100, 20, $CBS_DROPDOWNLIST)
GUICtrlSetData(-1, "MD2|MD4|MD5|SHA1", "SHA1")
Local $iCalculate = GUICtrlCreateButton("Calculate", 355, 40, 65, 25)
Local $iHashLabel = GUICtrlCreateLabel("Hash Digest", 5, 50, 350, 25)
GUISetState(@SW_SHOW)

_Crypt_Startup() ; To optimize performance start the crypt library.

While 1
	Switch GUIGetMsg()
		Case $GUI_EVENT_CLOSE
			Exit

		Case $iBrowse
			Local $sFilePath = FileOpenDialog("Open a file", "", "All files (*.*)") ; Select a file to find the hash.
			If @error Then
				ContinueLoop
			EndIf
			GUICtrlSetData($iInput, $sFilePath) ; Set the inputbox with the filepath.
			GUICtrlSetData($iHashLabel, "Hash Digest") ; Reset the hash digest label.

		Case $iCombo ; Check when the combobox is selected and retrieve the correct algorithm.
			Switch GUICtrlRead($iCombo) ; Read the combobox selection.
				Case "MD2"
					$bAlgorithm = $CALG_MD2

				Case "MD4"
					$bAlgorithm = $CALG_MD4

				Case "MD5"
					$bAlgorithm = $CALG_MD5

				Case "SHA1"
					$bAlgorithm = $CALG_SHA1

			EndSwitch

		Case $iCalculate
			Local $sRead = GUICtrlRead($iInput)
			If StringStripWS($sRead, 8) <> "" And FileExists($sRead) Then ; Check there is a file available to find the hash digest
				Local $bHash = _Crypt_HashFile($sRead, $bAlgorithm) ; Create a hash of the file.
				GUICtrlSetData($iHashLabel, $bHash) ; Set the hash digest label with the hash data.
			EndIf
	EndSwitch
WEnd

_Crypt_Shutdown() ; Shutdown the crypt library.