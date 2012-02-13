#include <Crypt.au3>

_Crypt_Startup() ; To optimize performance start the crypt library, though the same results will be shown if it isn't.

Local Const $sUserKey = "CryptPassword" ; Declare a password string to decrypt/encrypt the data.
Local $sData = "..upon a time there was a language without any standardized.." ; Data that will be hashed.

Local $sOutput = "The following results show the supported algorithms for encrypting the data." & @CRLF & @CRLF & _
		"Text: " & $sData & @CRLF & _
		"3DES: " & _Crypt_EncryptData($sData, $sUserKey, $CALG_3DES) & @CRLF & @CRLF & _
		"AES (128bit): " & _Crypt_EncryptData($sData, $sUserKey, $CALG_AES_128) & @CRLF & @CRLF & _
		"AES (192bit): " & _Crypt_EncryptData($sData, $sUserKey, $CALG_AES_192) & @CRLF & @CRLF & _
		"AES (256bit): " & _Crypt_EncryptData($sData, $sUserKey, $CALG_AES_256) & @CRLF & @CRLF & _
		"DES: " & _Crypt_EncryptData($sData, $sUserKey, $CALG_DES) & @CRLF & @CRLF & _
		"RC2: " & _Crypt_EncryptData($sData, $sUserKey, $CALG_RC2) & @CRLF & @CRLF & _
		"RC4: " & _Crypt_EncryptData($sData, $sUserKey, $CALG_RC4)

MsgBox(0, "Supported algorithms", $sOutput)

_Crypt_Shutdown() ; Shutdown the crypt library.
