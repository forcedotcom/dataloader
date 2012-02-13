; *******************************************************
; Example 1 - Create a word window with a new blank document,
;				then add some pictures to the document.
; *******************************************************
;
#include <Word.au3>

Local $sPath = @WindowsDir & "\"
Local $search = FileFindFirstFile($sPath & "*.bmp")

; Check if the search was successful
If $search = -1 Then
	MsgBox(0, "Error", "No images found")
	Exit
EndIf

Local $oWordApp = _WordCreate()
Local $oDoc = _WordDocGetCollection($oWordApp, 0)

While 1
	Local $file = FileFindNextFile($search)
	If @error Then ExitLoop
	Local $oShape = _WordDocAddPicture($oDoc, $sPath & $file, 0, 1)
	If Not @error Then $oShape.Range.InsertAfter(@CRLF)
WEnd

; Close the search handle
FileClose($search)
