; AutoIt V3.1.1++
;
; Test File
;
; Scripting.FileSystemObject example

; This example returns file information for AutoIt.exe

$objFS    = ObjCreate("Scripting.FileSystemObject")

$strPath = @AutoItExe

$objFile = $objFS.GetFile($strPath)

WITH $objFile

Msgbox(0, $strpath ,  _
 @AutoITexe             & " "  & @CRLF & _
 "File Version: "       & $objFS.GetFileVersion($strpath) & @CRLF & _
 "File Size: "          & Round((.Size/1024),2) & " KB" & @CRLF & _
 "Date Created: "       & .DateCreated & @CRLF & _
 "Date Last Modified: " & .DateLastModified  )

ENDWITH

