#include <Crypt.au3>

_Crypt_Startup() ; To optimize performance start the crypt library, though the same results will be shown if it isn't.

Local $sData = "..upon a time there was a language without any standardized cryptographic functions. That language is no more." ; Data that will be hashed.

Local $sOutput = "The following results show the supported algorithms for retrieving the hash of the data." & @CRLF & @CRLF & _
		"Text: " & $sData & @CRLF & _
		"MD2: " & _Crypt_HashData($sData, $CALG_MD2) & @CRLF & _
		"MD4: " & _Crypt_HashData($sData, $CALG_MD4) & @CRLF & _
		"MD5: " & _Crypt_HashData($sData, $CALG_MD5) & @CRLF & _
		"SHA1: " & _Crypt_HashData($sData, $CALG_SHA1)

MsgBox(0, "Supported algorithms", $sOutput)

_Crypt_Shutdown() ; Shutdown the crypt library.

