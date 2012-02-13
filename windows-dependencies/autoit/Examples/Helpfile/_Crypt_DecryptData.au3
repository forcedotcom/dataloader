#include <Crypt.au3>

Local Const $sUserKey = "CryptPassword" ; Declare a password string to decrypt/encrypt the data.
Local $sData = "..upon a time there was a language without any standardized cryptographic functions. That language is no more." ; Data that will be encrypted.

Local $bEncrypted = _Crypt_EncryptData($sData, $sUserKey, $CALG_RC4) ; Encrypt the data using the generic password string.

$bEncrypted = _Crypt_DecryptData($bEncrypted, $sUserKey, $CALG_RC4) ; Decrypt the data using the generic password string. The return value is a binary string.
MsgBox(0, "Decrypted data", BinaryToString($bEncrypted)) ; Convert the binary string using BinaryToString to display the initial data we encrypted.



