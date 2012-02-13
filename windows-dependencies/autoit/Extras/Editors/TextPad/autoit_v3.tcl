!TCL=3080,  means function retuns a value; means it setserror
!TITLE=AutoIt v3
!SORT=Y
!CHARSET=ANSI

!TEXT=#cs...#ce
#cs
	\^
#ce
!
!TEXT=Func...EndFunc
Func functioname([ByRef] param1, [ByRef] paramN)
	\^
	[Return x]
EndFunc
!
!TEXT=ContinueLoop
ContinueLoop
!
!TEXT=Do...Until
Do
	\^
Until <expression>
!
!TEXT=ExitLoop
ExitLoop [level]
!
!TEXT=For...Next
For <variable> = <start> To <end> [Step <stepval>]
	\^
Next
!
!TEXT=If...Else...EndIf
If <expression> [Then]
	\^
Else

EndIf
!
!TEXT=Select...Case...EndSelect
Select
	Case <expression>
		\^
	Case Else

EndSelect
!
!TEXT=While...WEnd
While <expression>
	\^
WEnd
!

!TEXT=ClipGet
ClipGet()
!
!TEXT=ClipPut
ClipPut(\^"value")
!
!TEXT=EnvGet
EnvGet(\^"envvariable")
!
!TEXT=EnvSet
EnvSet(\^"envvariable", "value")
!
!TEXT=EnvUpdate
EnvUpdate()
!

!TEXT=DirCopy
DirCopy(\^"source dir", "dest dir" [, flag])
!
!TEXT=DirCreate
DirCreate(\^"path")
!
!TEXT=DirMove
DirMove(\^"source dir", "dest dir" [, flag])
!
!TEXT=DirRemove
DirRemove(\^"path" [,recurse])
!
!TEXT=DriveGetDrive
DriveGetDrive(\^"type")
!
!TEXT=DriveGetFileSystem
DriveGetFileSystem(\^"path")
!
!TEXT=DriveGetLabel
DriveGetLabel(\^"path")
!
!TEXT=DriveGetSerial
DriveGetSerial(\^"path")
!
!TEXT=DriveGetType
DriveGetType(\^"path")
!
!TEXT=DriveSetLabel
DriveSetLabel(\^"path", "label")
!
!TEXT=DriveSpaceFree
DriveSpaceFree(\^"path")
!
!TEXT=DriveSpaceTotal
DriveSpaceTotal(\^"path")
!
!TEXT=DriveStatus
DriveStatus(\^"path")
!
!TEXT=FileChangeDir
FileChangeDir(\^"path")
!
!TEXT=FileClose
FileClose(filehandle)
!
!TEXT=FileCopy
FileCopy(\^"source", "dest" [,flag])
!
!TEXT=FileCreateShortcut
FileCreateShortcut(\^"file", "lnk" [,"workdir", "args", "desc", "icon", "hotkey"])
!
!TEXT=FileDelete
FileDelete(\^"path")
!
!TEXT=FileExists
FileExists(\^"path")
!
!TEXT=FileFindFirstFile
FileFindFirstFile(\^"filename")
!
!TEXT=FileFindNextFile
FileFindNextFile(filehandle)
!
!TEXT=FileGetAttrib
FileGetAttrib(\^"filename")
!
!TEXT=FileGetLongName
FileGetLongName(\^"file")
!
!TEXT=FileGetShortName
FileGetShortName(\^"file")
!
!TEXT=FileGetSize
FileGetSize(\^"filename")
!
!TEXT=FileGetTime
FileGetTime(\^"filename" [,option])
!
!TEXT=FileGetVersion
FileGetVersion(\^"filename")
!
!TEXT=FileInstall
FileInstall(\^"source", "dest" [,flag])
!
!TEXT=FileMove
FileMove(\^"source", "dest" [,flag])
!
!TEXT=FileOpen
FileOpen(\^"filename", mode)
!
!TEXT=FileOpenDialog
FileOpenDialog(\^"title", "init dir", "filter", [options])
!
!TEXT=FileRead
FileRead(\^filehandle_or_filename, count)
!
!TEXT=FileReadLine
FileReadLine(\^filehandle_or_filename, [line])
!
!TEXT=FileRecycle
FileRecycle(\^"source")
!
!TEXT=FileSaveDialog
FileSaveDialog(\^"title", "init dir", "filter", [options])
!
!TEXT=FileSelectFolder
FileSelectFolder(\^"dialog text", "root dirtext", flag)
!
!TEXT=FileSetAttrib
FileSetAttrib(\^"file pattern", "+-RASHNOT" [, recurse])
!
!TEXT=FileSetTime
FileSetTime(\^"file pattern", "time", type [, recurse])
!
!TEXT=FileWrite
FileWrite(\^filehandle_or_filename, "line")
!
!TEXT=FileWriteLine
FileWriteLine(\^filehandle_or_filename, "line")
!
!TEXT=IniDelete
IniDelete(\^"filename", "section", "key")
!
!TEXT=IniRead
IniRead(\^"filename", "section", "key", "default")
!
!TEXT=IniWrite
IniWrite(\^"filename", "section", "key", "value")
!

!TEXT=HotKeySet
HotKeySet(\^"key", ["function"])
!
!TEXT=Send
Send(\^"keys", [flag])
!
!TEXT=SendAttachMode (Option)
AutoItSetOption("SendAttachMode", \^param)
!
!TEXT=SendCapslockMode (Option)
AutoItSetOption("SendCapslockMode", \^param)
!
!TEXT=SendKeyDelay (Option)
AutoItSetOption("SendKeyDelay", \^param)
!
!TEXT=SendKeyDownDelay (Option)
AutoItSetOption("SendKeyDownDelay", \^param)
!
!TEXT=SendSetCapslockState
SendSetCapslockState(state)
!

!TEXT=Abs
Abs(expression)
!
!TEXT=ACos
ACos(expression)
!
!TEXT=ASin
ASin(expression)
!
!TEXT=Atan
ATan(expression)
!
!TEXT=BitAND
BitAND(value1, value2)
!
!TEXT=BitNOT
BitNOT(value)
!
!TEXT=BitOR
BitOR(value1, value2)
!
!TEXT=BitShift
BitShift(value, shift)
!
!TEXT=BitXOR
BitXOR(value1, value2)
!
!TEXT=Cos
Cos(expression)
!
!TEXT=Exp
Exp(expression)
!
!TEXT=Log
Log(expression)
!
!TEXT=Mod
Mod(value1, value2)
!
!TEXT=Random
Random( [[Min ,] Max])
!
!TEXT=Round
Round(expression [, decimalplaces])
!
!TEXT=Sin
Sin(expression)
!
!TEXT=Sqrt
Sqrt(expression)
!
!TEXT=Tan
Tan(expression)
!

!TEXT=InputBox
InputBox(\^"title", "Prompt" [, "Default" [, "password char" [, Width, Height [, Left, Top] [, Timeout]]]] )
!
!TEXT=MsgBox
MsgBox(flag, "title", "text" [, timeout] )
!
!TEXT=ProgressOff
ProgressOff()
!
!TEXT=ProgressOn
ProgressOn(\^"title", "maintext" [, "subtext"] [, x pos] [, y pos] [, opt] )
!
!TEXT=ProgressSet
ProgressSet(percent [, "subtext"] [, "maintext"] )
!
!TEXT=SplashImageOn
SplashImageOn(\^"title", "file" [, width] [, height] [, x pos] [, y pos] [, opt] )
!
!TEXT=SplashOff
SplashOff()
!
!TEXT=SplashTextOn
SplashTextOn(\^"title", "text" [, w] [, h] [, x pos] [, y pos] [, opt] [, "fontname"] [, "fontsz"] [, "fontwt"] )
!

!TEXT=AdlibDisable
AdlibDisable()
!
!TEXT=AdlibEnable
AdlibEnable(\^"function" [,time] )
!
!TEXT=AutoItSetOption
AutoItSetOption(\^"option", param )
!
!TEXT=AutoItWinGetTitle
AutoItWinGetTitle()
!
!TEXT=AutoItWinSetTitle
AutoItWinSetTitle(\^"newtitle")
!
!TEXT=BlockInput
BlockInput(flag)
!
!TEXT=Break
Break(mode)
!
!TEXT=Call
Call(\^"function")
!
!TEXT=CDTray
CDTray(\^"drive", "status")
!
!TEXT=ExpandEnvStrings (Option)
AutoItSetOption("ExpandEnvStrings", \^param)
!
!TEXT=Opt
Opt(\^"option", param )
!
!TEXT=MustDeclaredVars (Option)
AutoItSetOption("MustDeclaredVars", \^param)
!
!TEXT=PixelCoordMode (Option)
AutoItSetOption("PixelCoordMode", \^param)
!
!TEXT=PixelGetColor
PixelGetColor(x, y)
!
!TEXT=PixelSearch
PixelSearch(left, top, right, bottom, color [, shade-variation] [, step]])
!
!TEXT=SetError
SetError(code)
!
!TEXT=SoundPlay
SoundPlay(\^"filename" [,wait] )
!
!TEXT=SoundSetWaveVolume
SoundSetWaveVolume(percent)
!
!TEXT=TimerStart
TimerStart()
!
!TEXT=TimerStop
TimerStop(timestamp)
!
!TEXT=TrayIconHide (Option)
AutoItSetOption("TrayIconHide", \^param)
!
!TEXT=TrayIconDebug (Option)
AutoItSetOption("TrayIconDebug", \^param)
!

!TEXT=MouseClick
MouseClick(\^"button" [[[,x ,y] ,clicks] ,speed] )
!
!TEXT=MouseClickDelay (Option)
AutoItSetOption("MouseClickDelay", \^param)
!
!TEXT=MouseClickDownDelay (Option)
AutoItSetOption("MouseClickDownDelay", \^param)
!
!TEXT=MouseCoordMode (Option)
AutoItSetOption("MouseCoordMode", \^param)
!
!TEXT=MouseClickDrag
MouseClickDrag( "button" ,x1,y1,x2,y2 [,speed] )
!
!TEXT=MouseClickDragDelay (Option)
AutoItSetOption("MouseClickDragDelay", \^param)
!
!TEXT=MouseCoordMode (Option)
AutoItSetOption("MouseCoordMode", \^param)
!
!TEXT=MouseGetCursor
MouseGetCursor()
!
!TEXT=MouseGetPos
MouseGetPos()
!
!TEXT=MouseMove
MouseMove( x, y [,speed] )
!

!TEXT=ProcessClose
ProcessClose(\^"process")
!
!TEXT=ProcessExists
ProcessExists(\^"process")
!
!TEXT=ProcessWait
ProcessWait(\^"process", [timeout])
!
!TEXT=ProcessWaitClose
ProcessWaitClose(\^"process", [timeout] )
!
!TEXT=Run
Run(\^"filename", ["workingdir"], [flag] )
!
!TEXT=RunAsSet
RunAsSet(["user", "domain", "password"] )
!
!TEXT=RunErrorsFatal (Option)
AutoItSetOption("RunErrorsFatal", \^param)
!
!TEXT=RunWait
RunWait(\^"filename", ["workingdir"], [flag] )
!
!TEXT=Shutdown
Shutdown(code)
!
!TEXT=Sleep
Sleep(msDelay)
!

!TEXT=RegDelete
RegDelete(\^"keyname", ["valuename"] )
!
!TEXT=RegRead
RegRead(\^"keyname", "valuename")
!
!TEXT=RegWrite
RegWrite(\^"keyname", "valuename", "type", value)
!

!TEXT=StringAddCR
StringAddCR(\^"string")
!
!TEXT=StringInStr
StringInStr(\^"string", "substring", [casesense] )
!
!TEXT=StringIsAlNum
StringIsAlNum(\^"string")
!
!TEXT=StringIsAlpha
StringIsAlpha(\^"string")
!
!TEXT=StringIsASCII
StringIsASCII(\^"string")
!
!TEXT=StringIsDigit
StringIsDigit(\^"string")
!
!TEXT=StringIsFloat
StringIsFloat(\^"string")
!
!TEXT=StringIsInt
StringIsInt(\^"string")
!
!TEXT=StringIsLower
StringIsLower(\^"string")
!
!TEXT=StringIsSpace
StringIsSpace(\^"string")
!
!TEXT=StringIsUpper
StringIsUpper(\^"string")
!
!TEXT=StringIsXDigit
StringIsXDigit(\^"string")
!
!TEXT=StringLeft
StringLeft(\^"string", count)
!
!TEXT=StringLen
StringLen(\^"string")
!
!TEXT=StringLower
StringLower(\^"string")
!
!TEXT=StringMid
StringMid(\^"string", start, count)
!
!TEXT=StringReplace
StringReplace(\^"string", "searchstring", "replacestring" [, count [, casesense]] )
!
!TEXT=StringRight
StringRight(\^"string", count)
!
!TEXT=StringSplit
StringSplit(\^"string", "delimiters")
!
!TEXT=StringStripCR
StringStripCR(\^"string")
!
!TEXT=StringStripWS
StringStripWS(\^"string", flag)
!
!TEXT=StringTrimLeft
StringTrimLeft(\^"string", count)
!
!TEXT=StringTrimRight
StringTrimRight(\^"string", count)
!
!TEXT=StringUpper
StringUpper(\^"string")
!

!TEXT=Asc
Asc(\^"char")
!
!TEXT=Chr
Chr(ASCIIcode)
!
!TEXT=Dec
Dec("hex")
!
!TEXT=Eval
Eval(expression)
!
!TEXT=Hex
Hex(number, length)
!
!TEXT=Int
Int(expression)
!
!TEXT=IsAdmin
IsAdmin()
!
!TEXT=IsArray
IsArray(variable)
!
!TEXT=IsDeclared
IsDeclared("string")
!
!TEXT=IsFloat
IsFloat(variable)
!
!TEXT=IsInt
IsInt(variable)
!
!TEXT=IsNumber
IsNumber(variable)
!
!TEXT=IsString
IsString(variable)
!
!TEXT=Number
Number(expression)
!
!TEXT=String
String(expression)
!
!TEXT=UBound
UBound(Array [, Dimension] )
!

!TEXT=ControlCommand
ControlCommand(\^"title", "text", "classnameNN", "command" ,"option")
!
!TEXT=ControlCommand - IsVisible
ControlCommand(\^"title", "text", "classnameNN", "IsVisible", "")
!
!TEXT=ControlCommand - IsEnabled
ControlCommand(\^"title", "text", "classnameNN", "IsEnabled", "")
!
!TEXT=ControlCommand - ShowDropDown
ControlCommand(\^"title", "text", "classnameNN", "ShowDropDown", "")
!
!TEXT=ControlCommand - HideDropDown
ControlCommand(\^"title", "text", "classnameNN", "HideDropDown", "")
!
!TEXT=ControlCommand - AddString
ControlCommand(\^"title", "text", "classnameNN", "AddString", "string")
!
!TEXT=ControlCommand - DelString
ControlCommand(\^"title", "text", "classnameNN", "DelString", occurrence)
!
!TEXT=ControlCommand - FindString
ControlCommand(\^"title", "text", "classnameNN", "FindString", "string")
!
!TEXT=ControlCommand - SetCurrentSelection
ControlCommand(\^"title", "text", "classnameNN", "SetCurrentSelection", occurrence)
!
!TEXT=ControlCommand - SelectString
ControlCommand(\^"title", "text", "classnameNN", "SelectString", "string")
!
!TEXT=ControlCommand - IsChecked
ControlCommand(\^"title", "text", "classnameNN", "IsChecked", "")
!
!TEXT=ControlCommand - Check
ControlCommand(\^"title", "text", "classnameNN", "Check", "")
!
!TEXT=ControlCommand - UnCheck
ControlCommand(\^"title", "text", "classnameNN", "UnCheck", "")
!
!TEXT=ControlCommand - GetCurrentSelection
GetCurrentSelection(\^"title", "text", "classnameNN", "GetCurrentSelection", "")
!
!TEXT=ControlCommand - GetLineCount
ControlCommand(\^"title", "text", "classnameNN", "GetLineCount", "")
!
!TEXT=ControlCommand - GetCurrentLine
ControlCommand(\^"title", "text", "classnameNN", "GetCurrentLine", "")
!
!TEXT=ControlCommand - GetLine
ControlCommand(\^"title", "text", "classnameNN", "GetLine", line#)
!
!TEXT=ControlCommand - GetSelected
ControlCommand(\^"title", "text", "classnameNN", "GetSelected", "")
!
!TEXT=ControlCommand - EditPaste
ControlCommand(\^"title", "text", "classnameNN", "EditPaste", "string")
!
!TEXT=ControlCommand - CurrentTab
ControlCommand(\^"title", "text", "classnameNN", "CurrentTab", "")
!
!TEXT=ControlCommand - TabRight
ControlCommand(\^"title", "text", "classnameNN", "TabRight", "")
!
!TEXT=ControlCommand - TabLeft
ControlCommand(\^"title", "text", "classnameNN", "TabLeft", "")
!


!TEXT=ControlDisable
ControlDisable(\^"title", "text", "classnameNN")
!
!TEXT=ControlEnable
ControlEnable(\^"title", "text", "classnameNN")
!
!TEXT=ControlFocus
ControlFocus(\^"title", "text", "classnameNN")
!
!TEXT=ControlGetFocus
ControlGetPos(\^"title", ["text"] )
!
!TEXT=ControlGetPos
ControlGetPos(\^"title", "text", "classnameNN")
!
!TEXT=ControlGetText
ControlGetText(\^"title", "text", "classnameNN")
!
!TEXT=ControlHide
ControlHide(\^"title", "text", "classnameNN")
!
!TEXT=ControlClick
ControlClick(\^"title", "text", "classnameNN" [, button] [, clicks]])
!
!TEXT=ControlMove
ControlMove(\^"title", "text", "classnameNN", x, y [,width] [,height] )
!
!TEXT=ControlSend
ControlSend(\^"title", "text", "classnameNN", "string")
!
!TEXT=ControlSetText
ControlSetText(\^"title", "text", "classnameNN", "new text")
!
!TEXT=ControlShow
ControlShow(\^"title", "text", "classnameNN")
!
!TEXT=StatusbarGetText
StatusbarGetText(\^"title", [["text"], part] )
!

!TEXT=CaretCoordMode (Option)
AutoItSetOption("CaretCoordMode", \^param)
!
!TEXT=WinActivate
WinActivate(\^"title", ["text"] )
!
!TEXT=WinActive
WinActive(\^"title", ["text"] )
!
!TEXT=WinClose
WinClose(\^"title", ["text"] )
!
!TEXT=WinDetectHiddenText (Option)
AutoItSetOption("WinDetectHiddenText", \^param)
!
!TEXT=WinExists
WinExists(\^"title", ["text"] )
!
!TEXT=WinGetCaretPos
WinGetCaretPos( )
!
!TEXT=WinGetClassList
WinGetClassList(\^"title", ["text"] )
!
!TEXT=WinGetClientSize
WinGetClientSize(\^"title", ["text"] )
!
!TEXT=WinGetPos
WinGetPos(\^"title", ["text"] )
!
!TEXT=WinGetSate
WinGetSate(\^"title", ["text"] )
!
!TEXT=WinGetText
WinGetText(\^"title", ["text"] )
!
!TEXT=WinGetTitle
WinGetTitle(\^"title", ["text"] )
!
!TEXT=WinKill
WinKill(\^"title", ["text"] )
!
!TEXT=WinMenuSelectItem
WinMenuSelectItem(\^"title", "text", "item" [, "item"] [, "item"] [, "item"] [, "item"] [, "item"] [, "item"] )
!
!TEXT=WinMinimizeAll
WinMinimizeAll()
!
!TEXT=WinMinimizeAllUndo
WinMinimizeAllUndo()
!
!TEXT=WinMove
WinMove(\^"title", "text", x, y, [width], [height] )
!
!TEXT=WinSearchChildren (Option)
AutoItSetOption("WinSearchChildren", \^param)
!
!TEXT=WinSetOnTop
WinSetOnTop(\^"title", "text", flag)
!
!TEXT=WinSetTitle
WinSetTitle(\^"title", "text", "newtitle" )
!
!TEXT=WinShow
WinShow(\^"title", "text", flag)
!
!TEXT=WinTitleMatchMode (Option)
AutoItSetOption("WinTitleMatchMode", \^param)
!
!TEXT=WinWait
WinWait(\^"title", ["text"], [timeout] )
!
!TEXT=WinWaitActive
WinWaitActive(\^"title", ["text"], [timeout] )
!
!TEXT=WinWaitClose
WinWaitClose(\^"title", ["text"], [timeout] )
!
!TEXT=WinWaitDelay (Option)
AutoItSetOption("WinWaitDelay", \^param)
!
!TEXT=WinWaitNotActive
WinWaitNotActive(\^"title", ["text"], [timeout] )
!

!TEXT=@error
@error
!
!TEXT=@SEC
@SEC
!
!TEXT=@MIN
@MIN
!
!TEXT=@HOUR
@HOUR
!
!TEXT=@MDAY
@MDAY
!
!TEXT=@MON
@MON
!
!TEXT=@YEAR
@YEAR
!
!TEXT=@WDAY
@WDAY
!
!TEXT=@YDAY
@YDAY
!
!TEXT=@ProgramFilesDir
@ProgramFilesDir
!
!TEXT=@CommonFilesDir
@CommonFilesDir
!
!TEXT=@MyDocumentsDir
@MyDocumentsDir
!
!TEXT=@AppDataCommonDir
@AppDataCommonDir
!
!TEXT=@DesktopCommonDir
@DesktopCommonDir
!
!TEXT=@DocumentsCommonDir
@DocumentsCommonDir
!
!TEXT=@FavoritesCommonDir
@FavoritesCommonDir
!
!TEXT=@ProgramsCommonDir
@ProgramsCommonDir
!
!TEXT=@StartMenuCommonDir
@StartMenuCommonDir
!
!TEXT=@StartupCommonDir
@StartupCommonDir
!
!TEXT=@AppDataDir
@AppDataDir
!
!TEXT=@DesktopDir
@DesktopDir
!
!TEXT=@FavoritesDir
@FavoritesDir
!
!TEXT=@ProgramsDir
@ProgramsDir
!
!TEXT=@StartMenuDir
@StartMenuDir
!
!TEXT=@StartupDir
@StartupDir
!
!TEXT=@StartupDir
@StartupDir
!
!TEXT=@UserProfileDir
@UserProfileDir
!
!TEXT=@HomeDrive
@HomeDrive
!
!TEXT=@HomePath
@HomePath
!
!TEXT=@HomeShare
@HomeShare
!
!TEXT=@LogonDNSDomain
@LogonDNSDomain
!
!TEXT=@LogonDomain
@LogonDomain
!
!TEXT=@LogonServer
@LogonServer
!
!TEXT=@Computername
@Computername
!
!TEXT=@Username
@Username
!
!TEXT=@TempDir
@TempDir
!
!TEXT=@WindowsDir
@WindowsDir
!
!TEXT=@SystemDir
@SystemDir
!
!TEXT=@SW_HIDE
@SW_HIDE
!
!TEXT=@SW_MINIMIZE
@SW_MINIMIZE
!
!TEXT=@SW_MAXIMIZE
@SW_MAXIMIZE
!
!TEXT=@SW_RESTORE
@SW_RESTORE
!
!TEXT=@SW_SHOW
@SW_SHOW
!
!TEXT=@ScriptFullPath
@ScriptFullPath
!
!TEXT=@ScriptName
@ScriptName
!
!TEXT=@ScriptDir
@ScriptDir
!
!TEXT=@WorkingDir
@WorkingDir
!
!TEXT=@OSType
@OSType
!
!TEXT=@OSVersion
@OSVersion
!
!TEXT=@OSBuild
@OSBuild
!
!TEXT=@OSServicePack
@OSServicePack
!
!TEXT=@AutoItVersion
@AutoItVersion
!
!TEXT=@IPAddress1
@IPAddress1
!
!TEXT=@IPAddress2
@IPAddress2
!
!TEXT=@IPAddress3
@IPAddress3
!
!TEXT=@IPAddress4
@IPAddress4
!
!TEXT=@CR
@CR
!
!TEXT=@LF
@LF
!
!TEXT=@CRLF
@CRLF
!
!TEXT=@DesktopWidth
@DesktopWidth
!
!TEXT=@DesktopHeight
@DesktopHeight
!
!TEXT=CmdLine[0]
CmdLine[0]
!
