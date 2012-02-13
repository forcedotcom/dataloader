#include <GUIConstantsEx.au3>
#include <GuiMonthCal.au3>
#include <WindowsConstants.au3>

$Debug_MC = False ; Check ClassName being passed to MonthCal functions, set to True and use a handle to another control to see it work

Global $hMonthCal

_Main()

Func _Main()
	Local $hGUI

	; Create GUI
	$hGUI = GUICreate("Month Calendar Create", 400, 300)
	$hMonthCal = _GUICtrlMonthCal_Create($hGUI, 4, 4, $WS_BORDER)
	GUISetState()

	GUIRegisterMsg($WM_NOTIFY, "WM_NOTIFY")

	; Loop until user exits
	Do
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
	GUIDelete()
EndFunc   ;==>_Main

Func WM_NOTIFY($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref $hWnd, $iMsg, $iwParam
	Local $hWndFrom, $iIDFrom, $iCode, $tNMHDR, $tInfo

	$tNMHDR = DllStructCreate($tagNMHDR, $ilParam)
	$hWndFrom = HWnd(DllStructGetData($tNMHDR, "hWndFrom"))
	$iIDFrom = DllStructGetData($tNMHDR, "IDFrom")
	$iCode = DllStructGetData($tNMHDR, "Code")
	Switch $hWndFrom
		Case $hMonthCal
			Switch $iCode
				Case $MCN_GETDAYSTATE ; Sent by a month calendar control to request information about how individual days should be displayed
					$tInfo = DllStructCreate($tagNMDAYSTATE, $ilParam)
					_DebugPrint("$MCN_GETDAYSTATE" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode & @LF & _
							"-->Year:" & @TAB & DllStructGetData($tInfo, "Year") & @LF & _
							"-->Month:" & @TAB & DllStructGetData($tInfo, "Month") & @LF & _
							"-->DOW:" & @TAB & DllStructGetData($tInfo, "DOW") & @LF & _
							"-->Day:" & @TAB & DllStructGetData($tInfo, "Day") & @LF & _
							"-->Hour:" & @TAB & DllStructGetData($tInfo, "Hour") & @LF & _
							"-->Minute:" & @TAB & DllStructGetData($tInfo, "Minute") & @LF & _
							"-->Second:" & @TAB & DllStructGetData($tInfo, "Second") & @LF & _
							"-->MSecond:" & @TAB & DllStructGetData($tInfo, "MSecond") & @LF & _
							"-->DayState:" & @TAB & DllStructGetData($tInfo, "DayState") & @LF & _
							"-->pDayState:" & @TAB & DllStructGetData($tInfo, "pDayState"))
					; Address of an array of MONTHDAYSTATE (DWORD bit field that holds the state of each day in a month)
					; Each bit (1 through 31) represents the state of a day in a month. If a bit is on, the corresponding day will
					; be displayed in bold; otherwise it will be displayed with no emphasis.
					; No return value
				Case $MCN_SELCHANGE ; Sent by a month calendar control when the currently selected date or range of dates changes
					$tInfo = DllStructCreate($tagNMSELCHANGE, $ilParam)
					_DebugPrint("$MCN_SELCHANGE" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode & @LF & _
							"-->BegYear:" & @TAB & DllStructGetData($tInfo, "BegYear") & @LF & _
							"-->BegMonth:" & @TAB & DllStructGetData($tInfo, "BegMonth") & @LF & _
							"-->BegDOW:" & @TAB & DllStructGetData($tInfo, "BegDOW") & @LF & _
							"-->BegDay:" & @TAB & DllStructGetData($tInfo, "BegDay") & @LF & _
							"-->BegHour:" & @TAB & DllStructGetData($tInfo, "BegHour") & @LF & _
							"-->BegMinute:" & @TAB & DllStructGetData($tInfo, "BegMinute") & @LF & _
							"-->BegSecond:" & @TAB & DllStructGetData($tInfo, "BegSecond") & @LF & _
							"-->BegMSeconds:" & @TAB & DllStructGetData($tInfo, "BegMSeconds") & @LF & _
							"-->EndYear:" & @TAB & DllStructGetData($tInfo, "EndYear") & @LF & _
							"-->EndMonth:" & @TAB & DllStructGetData($tInfo, "EndMonth") & @LF & _
							"-->EndDOW:" & @TAB & DllStructGetData($tInfo, "EndDOW") & @LF & _
							"-->EndDay:" & @TAB & DllStructGetData($tInfo, "EndDay") & @LF & _
							"-->EndHour:" & @TAB & DllStructGetData($tInfo, "EndHour") & @LF & _
							"-->EndMinute:" & @TAB & DllStructGetData($tInfo, "EndMinute") & @LF & _
							"-->EndSecond:" & @TAB & DllStructGetData($tInfo, "EndSecond") & @LF & _
							"-->EndMSeconds:" & @TAB & DllStructGetData($tInfo, "EndMSeconds"))
					; No return value
				Case $MCN_SELECT ; Sent by a month calendar control when the user makes an explicit date selection within a month calendar control
					$tInfo = DllStructCreate($tagNMSELCHANGE, $ilParam)
					_DebugPrint("$MCN_SELECT" & @LF & "--> hWndFrom:" & @TAB & $hWndFrom & @LF & _
							"-->IDFrom:" & @TAB & $iIDFrom & @LF & _
							"-->Code:" & @TAB & $iCode & @LF & _
							"-->BegYear:" & @TAB & DllStructGetData($tInfo, "BegYear") & @LF & _
							"-->BegMonth:" & @TAB & DllStructGetData($tInfo, "BegMonth") & @LF & _
							"-->BegDOW:" & @TAB & DllStructGetData($tInfo, "BegDOW") & @LF & _
							"-->BegDay:" & @TAB & DllStructGetData($tInfo, "BegDay") & @LF & _
							"-->BegHour:" & @TAB & DllStructGetData($tInfo, "BegHour") & @LF & _
							"-->BegMinute:" & @TAB & DllStructGetData($tInfo, "BegMinute") & @LF & _
							"-->BegSecond:" & @TAB & DllStructGetData($tInfo, "BegSecond") & @LF & _
							"-->BegMSeconds:" & @TAB & DllStructGetData($tInfo, "BegMSeconds") & @LF & _
							"-->EndYear:" & @TAB & DllStructGetData($tInfo, "EndYear") & @LF & _
							"-->EndMonth:" & @TAB & DllStructGetData($tInfo, "EndMonth") & @LF & _
							"-->EndDOW:" & @TAB & DllStructGetData($tInfo, "EndDOW") & @LF & _
							"-->EndDay:" & @TAB & DllStructGetData($tInfo, "EndDay") & @LF & _
							"-->EndHour:" & @TAB & DllStructGetData($tInfo, "EndHour") & @LF & _
							"-->EndMinute:" & @TAB & DllStructGetData($tInfo, "EndMinute") & @LF & _
							"-->EndSecond:" & @TAB & DllStructGetData($tInfo, "EndSecond") & @LF & _
							"-->EndMSeconds:" & @TAB & DllStructGetData($tInfo, "EndMSeconds"))
					; No return value
			EndSwitch
	EndSwitch
	Return $GUI_RUNDEFMSG
EndFunc   ;==>WM_NOTIFY

Func _DebugPrint($s_text, $line = @ScriptLineNumber)
	ConsoleWrite( _
			"!===========================================================" & @LF & _
			"+======================================================" & @LF & _
			"-->Line(" & StringFormat("%04d", $line) & "):" & @TAB & $s_text & @LF & _
			"+======================================================" & @LF)
EndFunc   ;==>_DebugPrint
