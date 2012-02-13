#include <File.au3>

Local $s_TempFile, $s_FileName

; generate unique filename in @TempDir
$s_TempFile = _TempFile()

; generate unique filename in given directory and starting with tst_
$s_FileName = _TempFile("C:\", "tst_", ".txt", 7)

MsgBox(4096, "Info", "Names suitable for new temporary file : " & @LF & $s_TempFile & @LF & $s_FileName)

Exit
