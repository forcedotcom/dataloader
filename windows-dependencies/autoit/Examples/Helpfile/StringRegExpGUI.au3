; =======================================================================
;Original idea by w0uter
; Modified by steve8tch
;2008 updated to work with newer PCRE implemetation
;	removed use of message boxes and splash screens
;	created status bar to display messages
;	added timer
;2011 updated
;	fixes:
;		Help file button did not work with compiled scripts
;		x64 OS aware.
;		Different separator charactor used in GUICtrlSetData (thanks FichteFoll)
;		Display formating fixed when number of results from StringRegExp = 9, or 99, or 999 etc (thanks FichteFoll)
; =======================================================================
#include <GuiConstantsEx.au3>
#include <EditConstants.au3>
#include <WindowsConstants.au3>
#include <ButtonConstants.au3>
#include <StaticConstants.au3>
Opt("MustDeclareVars", 1)
Global Const $sSep = Chr(11) ; --> 0x0B = VT - use as a separator for use in the combo control. By default this uses a "|" and this charactor is likely to be used in a pattern file
Opt("GUIDataSeparatorChar",$sSep)
Global $sInitialDir = @ScriptDir
Global $sPatterns = "(.*)"
Global Const $iGreen = 0xAAFFD5
Global Const $iGrey = 0xD4D0C8
Global Const $iRed = 0xFF8888
Global Const $iBlack = 0x000000
Global Const $iBlue = 0x0000FF
Global Const $iSoftYellow = 0xFBFFC6
Global Const $iYellow = 0xFFFC8A
Global $hGui_StringToTest ; $hGui_StringToTest holds the currently selected tab for the input string to be tested (ie from the edit box or from the text file)
; results from StringRegExp come in 3 forms: a single string (rtn flag 0), a single array (rtn flag 1,2,3) or an array of arrays (rtn flag 4)
Global $bResultTrueFalseExpected = False
Global $bArrayOfArraysExpected = False
Global $sInitialDir ; use to save the location of the previous "Browse" function
Global $sPatterns = readDatFile()
GUICreate("StringRegExp Original idea -by w0uter, modified Steve8tch", 550, 596, (@DesktopWidth - 550) / 2, (@DesktopHeight - 570) / 2)
GUICtrlCreateGroup("The pattern   -  $ptn", 10, 210, 530, 60)
GUICtrlCreateGroup("Output", 140, 280, 400, 280)
GUICtrlCreateGroup("       Return Flag", 10, 280, 120, 120)
GUICtrlCreateGroup("           Offset", 10, 410, 120, 50)
GUICtrlCreateGroup("@Error     @Extended", 10, 470, 120, 50)
Global $hGui_Radio_0 = GUICtrlCreateRadio("0", 60, 300, 50, 18)
Global $hGui_Radio_1 = GUICtrlCreateRadio("1", 60, 318, 50, 18)
Global $hGui_Radio_2 = GUICtrlCreateRadio("2", 60, 336, 50, 18)
Global $hGui_Radio_3 = GUICtrlCreateRadio("3", 60, 354, 50, 18)
Global $hGui_Radio_4 = GUICtrlCreateRadio("4", 60, 372, 50, 18)
GUICtrlSetState($hGui_Radio_1, $GUI_CHECKED)

Global $hGui_tab = GUICtrlCreateTab(10, 10, 530, 190)
Global $hGui_tabitem1 = GUICtrlCreateTabItem("Copy and Paste the text to check - $str")
Global $hGui_inputEditBox = GUICtrlCreateEdit("", 20, 40, 510, 150, BitOR($ES_WANTRETURN, $WS_VSCROLL, $WS_HSCROLL, $ES_AUTOVSCROLL, $ES_AUTOHSCROLL))
GUICtrlSetBkColor($hGui_inputEditBox, $iSoftYellow)
Global $hGui_tabitem2 = GUICtrlCreateTabItem("Load text from File")
Global $hGui_browse = GUICtrlCreateButton("Browse for file", 20, 40, 100, 20)
Global $hGui_pathToInputFile = GUICtrlCreateEdit("", 130, 40, 400, 20, BitOR($ES_WANTRETURN, $WS_HSCROLL, $ES_AUTOHSCROLL))
Global $hGui_inputFromFile = GUICtrlCreateEdit("", 20, 70, 510, 120, BitOR($ES_WANTRETURN, $WS_VSCROLL, $WS_HSCROLL, $ES_AUTOVSCROLL, $ES_AUTOHSCROLL))
GUICtrlSetBkColor($hGui_inputFromFile, $iSoftYellow)
GUICtrlCreateTabItem("");
Global $hGui_Out = GUICtrlCreateEdit("", 150, 296, 380, 262, BitOR($ES_WANTRETURN, $WS_VSCROLL, $WS_HSCROLL, $ES_AUTOVSCROLL, $ES_AUTOHSCROLL))
GUICtrlSetBkColor($hGui_Out, $iSoftYellow)
Global $hGui_Pattern = GUICtrlCreateCombo("", 70, 230, 430, 30)
GUICtrlSetFont($hGui_Pattern, 14, -1, -1, "Arial")
GUICtrlSetColor($hGui_Pattern, $iBlue)
GUICtrlSetBkColor($hGui_Pattern, $iYellow)
GUICtrlSetData($hGui_Pattern, $sPatterns, "(.*)")
Global $hGui_doPtnAdd = GUICtrlCreateButton("Add", 504, 225, 30, 18)
Global $hGui_doPtnDel = GUICtrlCreateButton("Del", 504, 245, 30, 18)
Global $hGui_test = GUICtrlCreateButton("Test", 20, 235, 40, 20, $BS_DEFPUSHBUTTON)
Global $hGui_Offset = GUICtrlCreateInput("1", 40, 430, 60, 20)
Global $hGui_err = GUICtrlCreateInput("", 20, 490, 40, 20, $ES_READONLY)
Global $hGui_ext = GUICtrlCreateInput("", 70, 490, 50, 20, $ES_READONLY)
Global $hGui_Help = GUICtrlCreateButton("StringRegExp HELP", 10, 530, 120, 30)
;Global $hGui_Exit = GUICtrlCreateButton("EXIT", 10, 530, 55, 30)
Global $hGui_timerDisplay = GUICtrlCreateLabel("Time (ms)", 3, 573, 142, 20, $SS_SUNKEN)
Global $hGUI_StatusBar = GUICtrlCreateLabel("Status..", 150, 573, 395, 20, $SS_SUNKEN)
$hGui_StringToTest = $hGui_inputEditBox ; default - read the string to be tested from the edit box
; setup tool tips
; GUICtrlSetTip required IE version 5+
If Number(StringLeft(RegRead("HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Internet Explorer", "Version"), 1)) > 4 Then
	GUICtrlSetTip($hGui_Radio_0, "Returns 1 (matched) or 0 (no match)", "Return Flag= 0", 1, 1)
	GUICtrlSetTip($hGui_Radio_1, "Returns an array containing the matches.", "Return Flag = 1", 1, 1)
	GUICtrlSetTip($hGui_Radio_2, "Returns the full matched string AND an array containing the matches (Perl / PHP style).", "Return Flag = 2", 1, 1)
	GUICtrlSetTip($hGui_Radio_3, "Returns an array containing the global matches.", "Return Flag = 3", 1, 1)
	GUICtrlSetTip($hGui_Radio_4, "Returns an array of arrays containing the full matched strings AND global matches. (Perl / PHP style).", "Return Flag = 4", 1, 1)
	GUICtrlSetTip($hGui_Offset, "[optional] The string position to start the match (starts at 1) The default is 1.", "Offset option", 1, 1)
Else
	GUICtrlSetTip($hGui_Radio_0, "Returns 1 (matched) or 0 (no match)")
	GUICtrlSetTip($hGui_Radio_1, "Returns an array containing the matches.")
	GUICtrlSetTip($hGui_Radio_2, "Returns the full matched string AND an array containing the matches (Perl / PHP style).")
	GUICtrlSetTip($hGui_Radio_3, "Returns an array containing the global matches.")
	GUICtrlSetTip($hGui_Radio_4, "Returns an array of arrays containing the full matched strings AND global matches. (Perl / PHP style).")
	GUICtrlSetTip($hGui_Offset, "[optional] The string position to start the match (starts at 1) The default is 1.")
EndIf

GUISetState()
While 1
	Switch GUIGetMsg()
		Case $GUI_EVENT_CLOSE
			Exit
		Case $hGui_test
			doStringRegExpTest()
		Case $hGui_browse
			doBrowseForFile()
		Case $hGui_tab
			If GUICtrlRead($hGui_tab) = 0 Then
				$hGui_StringToTest = $hGui_inputEditBox
			Else
				$hGui_StringToTest = $hGui_inputFromFile
			EndIf
		Case $hGui_doPtnAdd
			doPtnAdd(GUICtrlRead($hGui_Pattern))
		Case $hGui_doPtnDel
			doPtnDel(GUICtrlRead($hGui_Pattern))
		Case $hGui_Help
			doDisplayHelp()
		Case Else
			;;
	EndSwitch
WEnd

Func doStringRegExpTest()
	Local $aA, $aB ;use for arrays
	Local $i, $j ; use for stepping through arrays
	Local $c ;  counter
	Local $hTimer, $t ; use for timing
	Local $iErr, $iExt ; use to hold result of @error and @extended
	Local $sResult = "" ; use to hold result
	Local $iStrLgth ; use to the string length of the number of results expected. (eg Use in the StringFormat function
	Local $x, $y ; local vars.
	GUICtrlSetData($hGui_Out, "")
	GUICtrlSetData($hGUI_StatusBar, "Performing test..... please wait.")
	GUICtrlSetBkColor($hGUI_StatusBar, $iGreen)
	;set up timer
	$hTimer = TimerInit()
	$aA = StringRegExp(GUICtrlRead($hGui_StringToTest), GUICtrlRead($hGui_Pattern), getReturnFlag(), getOffset())
	$iErr = @error
	$iExt = @extended
	$t = TimerDiff($hTimer)
	GUICtrlSetData($hGui_timerDisplay, $t & "  ms")
	GUICtrlSetData($hGui_err, $iErr)
	GUICtrlSetData($hGui_ext, $iExt)
	Select
		Case $iErr = 0
			GUICtrlSetData($hGUI_StatusBar, "Valid pattern - updating display.   Please wait....")
			GUICtrlSetBkColor($hGUI_StatusBar, $iGreen)
		Case $iErr = 1
			GUICtrlSetData($hGUI_StatusBar, "Array is invalid. No matches")
			GUICtrlSetBkColor($hGUI_StatusBar, $iRed)
		Case $iErr = 2
			GUICtrlSetData($hGUI_StatusBar, "Bad pattern, (array is invalid). @Extended = offset of error in pattern.")
			GUICtrlSetBkColor($hGUI_StatusBar, $iRed)
	EndSelect
	If $iErr = 0 Then
		$x = UBound($aA)
		If $bArrayOfArraysExpected Then
			$y = UBound($aA[0])
			$x *= $y
		EndIf
		$iStrLgth = StringLen(String($x - 1))
		If $bArrayOfArraysExpected Then ; results -> array of arrays expected
			$c = 0 ; use $c as a counter to help display the results
			If UBound($aA) Then
				For $i = 0 To UBound($aA) - 1
					$aB = $aA[$i]
					For $j = 0 To UBound($aB) - 1
						$sResult &= StringFormat("%0" & $iStrLgth & "i", $c) & ' => ' & $aB[$j] & @CRLF
						$c += 1
					Next
					$sResult &= @CRLF
				Next
				GUICtrlSetData($hGui_Out, $sResult)
				GUICtrlSetData($hGUI_StatusBar, "Complete")
			EndIf
		ElseIf $bResultTrueFalseExpected Then ; result string expected
			If $aA = 1 Then
				$sResult &= "1   <-- SUCCESS, matches found" & @CRLF
			Else
				$sResult &= "0   <-- FAIL, no matches found" & @CRLF
			EndIf
			GUICtrlSetData($hGui_Out, $sResult)
			GUICtrlSetData($hGUI_StatusBar, "Complete")
		Else ; a single array expected
			If UBound($aA) Then
				For $i = 0 To UBound($aA) - 1
					$sResult &= StringFormat("%0" & $iStrLgth & "i", $i) & ' => ' & $aA[$i] & @CRLF
				Next
				GUICtrlSetData($hGui_Out, $sResult)
				GUICtrlSetData($hGUI_StatusBar, "Complete")
			EndIf
		EndIf
	EndIf
EndFunc   ;==>doStringRegExpTest
Func getReturnFlag()
	$bArrayOfArraysExpected = False
	$bResultTrueFalseExpected = False
	Switch $GUI_CHECKED
		Case GUICtrlRead($hGui_Radio_0)
			$bResultTrueFalseExpected = True
			Return 0
		Case GUICtrlRead($hGui_Radio_1)
			Return 1
		Case GUICtrlRead($hGui_Radio_2)
			Return 2
		Case GUICtrlRead($hGui_Radio_3)
			Return 3
		Case GUICtrlRead($hGui_Radio_4)
			$bArrayOfArraysExpected = True
			Return 4
	EndSwitch
EndFunc   ;==>getReturnFlag
Func getOffset()
	Local $x
	$x = Int(GUICtrlRead($hGui_Offset))
	If @error Then
		Return 1
	Else
		Return $x
	EndIf
EndFunc   ;==>getOffset
Func doBrowseForFile()
	Local $sFilePath, $sFileTxt
	$sFilePath = FileOpenDialog("Select text file to test", $sInitialDir, "Text files (*.*)", 1)
	$sInitialDir = StringTrimRight($sFilePath, StringInStr($sFilePath, "\", "-1"))
	GUICtrlSetData($hGUI_StatusBar, "Loading file..")
	GUICtrlSetBkColor($hGUI_StatusBar, $iGreen)
	GUICtrlSetData($hGui_pathToInputFile, $sFilePath)
	$sFileTxt = FileRead($sFilePath)
	GUICtrlSetData($hGUI_StatusBar, "File loaded... updating display")
	GUICtrlSetData($hGui_inputFromFile, $sFileTxt)
	GUICtrlSetData($hGUI_StatusBar, "")
	GUICtrlSetBkColor($hGUI_StatusBar, $iGrey)
EndFunc   ;==>doBrowseForFile
Func readDatFile()
	Local $sDat, $sOut = ""
	Local $sHeader = "[do not delete this file - Patterns are listed below]" & @CRLF
	Local $sDatFile = @AppDataDir & "\StringRegExpGUIPattern.dat"
	If FileExists($sDatFile) = 0 Then
		$sDat = $sHeader & "(.*)"
		FileWrite($sDatFile, $sDat)
		$sOut = "(.*)"
	Else
		$sDat = FileRead($sDatFile)
		$sDat = StringReplace($sDat, $sHeader, "") ; strip out header line
		;Strip out any leading or trailing @CRLF
		If StringLeft($sDat, 2) = @CRLF Then $sDat = StringTrimLeft($sDat, 2)
		If StringRight($sDat, 2) = @CRLF Then $sDat = StringTrimRight($sDat, 2)
		If $sDat <> "" Then ; we should have 1 or more patterns
			$sOut = StringReplace($sDat, @CRLF, $sSep)
		Else
			FileWrite($sDatFile, $sHeader & "(.*)")
			$sOut = "(.*)"
		EndIf
	EndIf
	Return $sOut
EndFunc   ;==>readDatFile
Func doPtnDel($x)
	Local $sDat
	Local $sDatFile = @AppDataDir & "\StringRegExpGUIPattern.dat"
	$sDat = FileRead($sDatFile)
	;now find and remove this entry from the dat file
	$sDat = StringReplace($sDat, $x, "")
	;If $x was in the middle of the dat file - we will now need to find and remove any double @CRLF entries
	$sDat = StringReplace($sDat, @CRLF & @CRLF, @CRLF)
	;If $x was at the begining or the end of the file - we now need to stop off a leading or trailing @CRLF entry
	If StringLeft($sDat, 2) = @CRLF Then $sDat = StringTrimLeft($sDat, 2)
	If StringRight($sDat, 2) = @CRLF Then $sDat = StringTrimRight($sDat, 2)
	;Now delete the previous dat file and create a new one
	If FileDelete($sDatFile) Then
		FileWrite($sDatFile, $sDat)
	Else
		MsgBox(0, "***ERROR**", "Failed to delete  entry from the dat file" & @CRLF & _
				"Reason: Failed to delete old file.")
	EndIf
	;Now read in new dat file
	$sPatterns = readDatFile()
	GUICtrlSetData($hGui_Pattern, $sSep & $sPatterns, "(.*)")
EndFunc   ;==>doPtnDel
Func doPtnAdd($x)
	Local $sDat
	Local $sDatFile = @AppDataDir & "\StringRegExpGUIPattern.dat"
	$sDat = FileRead($sDatFile)
	;now add this entry fto the end of the dat file
	$sDat &= @CRLF & $x
	;Now delete the previous dat file and create a new one
	If FileDelete($sDatFile) Then
		FileWrite($sDatFile, $sDat)
	Else
		MsgBox(0, "***ERROR**", "Failed to delete  entry from the dat file" & @CRLF & _
				"Reason: Failed to delete old file.")
	EndIf
	;Now read in new dat file
	$sPatterns = readDatFile()
	GUICtrlSetData($hGui_Pattern, $sSep & $sPatterns, $x)
EndFunc   ;==>doPtnAdd
Func doDisplayHelp()
	Local $sPathToHelpFile
	Local $sPathToAutoIt
	Local $iErr = 0
	If @Compiled = 0 Then
		$sPathToHelpFile = StringLeft(@AutoItExe, StringInStr(@AutoItExe, "\", 0, -1))
		Run($sPathToHelpFile & "Autoit3Help.exe StringRegExp")
		If @error = 1 Then $iErr = 1
	Else
		;Try and file to help file (if available at all)
		If @OSArch = "X86" Then
			$sPathToAutoIt = RegRead('HKLM\Software\AutoIt v3\AutoIt', 'InstallDir')
			Run($sPathToAutoIt & "\Autoit3Help.exe StringRegExp")
			If @error Then $iErr = 1
		Else
			$sPathToAutoIt = RegRead('HKLM64\Software\AutoIt v3\AutoIt', 'InstallDir')
			If $sPathToAutoIt = "" Then
				$sPathToAutoIt = RegRead('HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\AutoIt v3\AutoIt', 'InstallDir')
				If $sPathToAutoIt = "" Then
					$iErr = 1
				EndIf
			EndIf
			If $iErr = 0 Then ; we picked up a location from the registry queires above
				Run($sPathToAutoIt & "\Autoit3Help.exe StringRegExp")
				If @error Then $iErr = 1
			EndIf
		EndIf
	EndIf
	If $iErr = 1 Then MsgBox(0, "error", "Cannot find help file - sorry")
EndFunc   ;==>doDisplayHelp