#include <SQLite.au3>
#include <SQLite.dll.au3>
#include <File.au3>

;Filenames
Local $sTsvFile = FileGetShortName(_TempFile(@ScriptDir, "~", ".tsv"))
Local $sDbFile = FileGetShortName(_TempFile(@ScriptDir, "~", ".db"))

;Create Tsv File
FileWriteLine($sTsvFile, "a" & @TAB & "b" & @TAB & "c")
FileWriteLine($sTsvFile, "a1" & @TAB & "b1" & @TAB & "c1")
FileWriteLine($sTsvFile, "a2" & @TAB & "b2" & @TAB & "c2")

;import (using SQLite3.exe)
Local $sIn, $sOut, $i, $sCreate = "CREATE TABLE TblImport (";
For $i = 1 To _StringCountOccurance(FileReadLine($sTsvFile, 1), @TAB) + 1
	$sCreate &= "Column_" & $i & ","
Next
$sCreate = StringTrimRight($sCreate, 1) & ");"
$sIn = $sCreate & @CRLF ; Create Table
$sIn &= ".separator \t" & @CRLF ; Select @TAB as Separator
$sIn &= ".import '" & $sTsvFile & "' TblImport" & @CRLF
_SQLite_SQLiteExe($sDbFile, $sIn, $sOut, -1, True)

If @error = 0 Then
	;Show Table (using SQLite3.dll)
	Local $iRows, $iColumns, $aRes
	_SQLite_Startup()
	ConsoleWrite("_SQLite_LibVersion=" & _SQLite_LibVersion() & @CRLF)
	_SQLite_Open($sDbFile)
	_SQLite_GetTable2d(-1, "SELECT ROWID,* FROM TblImport;", $aRes, $iRows, $iColumns)
	_SQLite_Display2DResult($aRes) ; Output to Console
	_SQLite_Close()
	_SQLite_Shutdown()
Else
	If @error = 2 Then
		ConsoleWrite("ERROR: Sqlite3.exe file not found" & @CRLF)
	Else
		ConsoleWrite("ERROR: @error=" & @error & " when calling _SQLite_SQLiteExe" & @CRLF)
	EndIf
EndIf

;Remove Temp Files
FileDelete($sTsvFile)
FileDelete($sDbFile)

;~ Output:
;~ 	rowid  Column_1  Column_2  Column_3
;~ 	1      a         b         c
;~ 	2      a1        b1        c1
;~ 	3      a2        b2        c2



Func _StringCountOccurance($sSearchString, $sSubString, $fCaseSense = 0) ; Returns Number of $sSubString in $sSearchString
	Local $iOccCnt = 1
	Do
		If StringInStr($sSearchString, $sSubString, $fCaseSense, $iOccCnt) > 0 Then
			$iOccCnt += 1
		Else
			ExitLoop
		EndIf
	Until 0
	Return $iOccCnt - 1
EndFunc   ;==>_StringCountOccurance
