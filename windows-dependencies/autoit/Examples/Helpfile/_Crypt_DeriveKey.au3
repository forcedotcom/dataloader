#include <Crypt.au3>

Local $aStringsToEncrypt[6] = ["AutoIt", "SciTE", "Crypt", ".au3", 42, "42"]
Local $sOutput = ""

Local $hKey = _Crypt_DeriveKey("CryptPassword", $CALG_RC4) ; Declare a password string and algorithm to create a cryptographic key.

For $iWord In $aStringsToEncrypt
	$sOutput &= $iWord & @TAB & " = " & _Crypt_EncryptData($iWord, $hKey, $CALG_USERKEY) & @CRLF ; Encrypt the text with the cryptographic key.
Next

MsgBox(0, "Encrypted data", $sOutput)

_Crypt_DestroyKey($hKey) ; Destroy the cryptographic key.
