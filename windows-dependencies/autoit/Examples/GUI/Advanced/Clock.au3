#include <GDIPlus.au3>
#include <WinAPI.au3>
#include <GuiConstantsEx.au3>
#include <WindowsConstants.au3>

; ===============================================================================================================================
; Description ...: Simple clock using layered windows
; Author ........: Paul Campbell (PaulIA)
; Notes .........:
; ===============================================================================================================================

; ===============================================================================================================================
; Global constants
; ===============================================================================================================================

Global Const $iCenter = 200
Global Const $iDotOpacity = 250
Global Const $iHourRad = 140
Global Const $iMinRad = 200
Global Const $iOpacity = 128
Global Const $nPI = 3.1415926535897932384626433832795
Global Const $iRadius = 200
Global Const $iSecRad = 200
Global Const $iTickLen = 0.02

Global Const $AC_SRC_ALPHA = 1

Global Enum $eScrDC = 0, $eMemDC, $eBitmap, $eWidth, $eHeight, $eGraphic, $ePen, $eCap, $eBrush, $eFormat, $eFamily, $eFont, $eLayout, $eLast

; ===============================================================================================================================
; Global variables
; ===============================================================================================================================

Global $hDial, $hTime, $hHour, $hMin, $hSec, $hDot, $aTime, $aHour, $aMin, $aSec, $aCurr[3][2], $aLast[3][2]

; ===============================================================================================================================
; Main
; ===============================================================================================================================

ClockInit()
DialDraw()
Draw()
DotDraw()
ClockLoop()
ClockDone()

; ===============================================================================================================================
; Finalize clock
; ===============================================================================================================================
Func ClockDone()
	; Finalize GDI+ resources
	TimeDone()
	HourDone()
	MinDone()
	SecDone()

	; Finalize GDI+ library
	_GDIPlus_Shutdown()
EndFunc   ;==>ClockDone

; ===============================================================================================================================
; Initialize clock
; ===============================================================================================================================
Func ClockInit()
	Local $iX, $iY

	; Calculate the dial frame caption size
	$iX = -(_WinAPI_GetSystemMetrics($SM_CXFRAME))
	$iY = -(_WinAPI_GetSystemMetrics($SM_CYCAPTION) + _WinAPI_GetSystemMetrics($SM_CYFRAME))

	; Allocate the window resources
	$hDial = GUICreate("Clock", $iRadius * 2, $iRadius * 2, -1, -1, 0, BitOR($WS_EX_LAYERED, $WS_EX_TOPMOST))
	GUISetState()
	$hTime = GUICreate("Time", $iRadius * 2, $iRadius * 2, $iX, $iY, 0, BitOR($WS_EX_LAYERED, $WS_EX_MDICHILD), $hDial)
	GUISetState()
	$hHour = GUICreate("Hour", $iRadius * 2, $iRadius * 2, $iX, $iY, 0, BitOR($WS_EX_LAYERED, $WS_EX_MDICHILD), $hDial)
	GUISetState()
	$hMin = GUICreate("Min", $iRadius * 2, $iRadius * 2, $iX, $iY, 0, BitOR($WS_EX_LAYERED, $WS_EX_MDICHILD), $hDial)
	GUISetState()
	$hSec = GUICreate("Sec", $iRadius * 2, $iRadius * 2, $iX, $iY, 0, BitOR($WS_EX_LAYERED, $WS_EX_MDICHILD), $hDial)
	GUISetState()
	$hDot = GUICreate("Dot", $iRadius * 2, $iRadius * 2, $iX, $iY, 0, BitOR($WS_EX_LAYERED, $WS_EX_MDICHILD), $hDial)
	GUISetState()

	; Initialize GDI+ library
	_GDIPlus_Startup()

	; Initialize GDI+ resources
	TimeInit()
	HourInit()
	MinInit()
	SecInit()

	; Hook non client hit test message so we can move the clock
	GUIRegisterMsg($WM_NCHITTEST, "WM_NCHITTEST")
EndFunc   ;==>ClockInit

; ===============================================================================================================================
; Loop until user exits
; ===============================================================================================================================
Func ClockLoop()
	Do
		Draw()
	Until GUIGetMsg() = $GUI_EVENT_CLOSE
EndFunc   ;==>ClockLoop

; ===============================================================================================================================
; Draw the center dot
; ===============================================================================================================================
Func DotDraw()
	Local $aDot

	$aDot = ResourceInit($iRadius * 2, $iRadius * 2)
	_GDIPlus_GraphicsFillEllipse($aDot[$eGraphic], $iRadius - 10, $iRadius - 10, 20, 20)
	ResourceSet($hDot, $aDot, $iDotOpacity)
	ResourceDone($aDot)
EndFunc   ;==>DotDraw

; ===============================================================================================================================
; Draw the clock elements
; ===============================================================================================================================
Func Draw()
	; Calculate current time element position
	$aLast = $aCurr
	$aCurr[0][0] = $iCenter + Cos(TimeToRadians("sec")) * $iSecRad
	$aCurr[0][1] = $iCenter - Sin(TimeToRadians("sec")) * $iSecRad
	$aCurr[1][0] = $iCenter + Cos(TimeToRadians("min")) * $iMinRad
	$aCurr[1][1] = $iCenter - Sin(TimeToRadians("min")) * $iMinRad
	$aCurr[2][0] = $iCenter + Cos(TimeToRadians("hour")) * $iHourRad
	$aCurr[2][1] = $iCenter - Sin(TimeToRadians("hour")) * $iHourRad

	; Draw time elements
	TimeDraw()
	HourDraw()
	MinDraw()
	SecDraw()
EndFunc   ;==>Draw

; ===============================================================================================================================
; Draw the clock dial
; ===============================================================================================================================
Func DialDraw()
	Local $aDial, $hPen1, $hPen2, $iI, $iN, $iX1, $iY1, $iX2, $iY2

	$aDial = ResourceInit($iRadius * 2, $iRadius * 2)
	$hPen1 = _GDIPlus_PenCreate()
	$hPen2 = _GDIPlus_PenCreate(0xFF0000FF, 4)
	For $iI = 0 To 2 * $nPI Step $nPI / 30
		$iX1 = $iCenter + Cos($iI) * ($iRadius * (1.00 - $iTickLen))
		$iY1 = $iCenter - Sin($iI) * ($iRadius * (1.00 - $iTickLen))
		$iX2 = $iCenter + Cos($iI) * $iRadius
		$iY2 = $iCenter - Sin($iI) * $iRadius
		If Mod($iN, 5) = 0 Then
			_GDIPlus_GraphicsDrawLine($aDial[$eGraphic], $iX1, $iY1, $iX2, $iY2, $hPen2)
		Else
			_GDIPlus_GraphicsDrawLine($aDial[$eGraphic], $iX1, $iY1, $iX2, $iY2, $hPen1)
		EndIf
		$iN += 1
	Next
	_GDIPlus_PenDispose($hPen2)
	_GDIPlus_PenDispose($hPen1)

	ResourceSet($hDial, $aDial)
	ResourceDone($aDial)
EndFunc   ;==>DialDraw

; ===============================================================================================================================
; Finalize resources for the hour hand
; ===============================================================================================================================
Func HourDone()
	_GDIPlus_PenDispose($aHour[$ePen])
	_GDIPlus_ArrowCapDispose($aHour[$eCap])
	ResourceDone($aHour)
EndFunc   ;==>HourDone

; ===============================================================================================================================
; Draw the hour hand
; ===============================================================================================================================
Func HourDraw()
	If ($aLast[2][0] = $aCurr[2][0]) And ($aLast[2][1] = $aCurr[2][1]) Then Return
	_GDIPlus_GraphicsDrawLine($aHour[$eGraphic], $iCenter, $iCenter, $aCurr[2][0], $aCurr[2][1], $aHour[$ePen])
	ResourceSet($hHour, $aHour)
EndFunc   ;==>HourDraw

; ===============================================================================================================================
; Initialize resources for the hour hand
; ===============================================================================================================================
Func HourInit()
	$aHour = ResourceInit($iRadius * 2, $iRadius * 2)
	$aHour[$ePen] = _GDIPlus_PenCreate(0xFFFF00FF)
	$aHour[$eCap] = _GDIPlus_ArrowCapCreate($iHourRad / 2, 8)
	_GDIPlus_PenSetCustomEndCap($aHour[$ePen], $aHour[$eCap])
EndFunc   ;==>HourInit

; ===============================================================================================================================
; Finalize resources for the minute hand
; ===============================================================================================================================
Func MinDone()
	_GDIPlus_PenDispose($aMin[$ePen])
	_GDIPlus_ArrowCapDispose($aMin[$eCap])
	ResourceDone($aMin)
EndFunc   ;==>MinDone

; ===============================================================================================================================
; Draw the minute hand
; ===============================================================================================================================
Func MinDraw()
	If ($aLast[1][0] = $aCurr[1][0]) And ($aLast[1][1] = $aCurr[1][1]) Then Return
	_GDIPlus_GraphicsFillRect($aMin[$eGraphic], 0, 0, $iRadius * 2, $iRadius * 2)
	_GDIPlus_GraphicsDrawLine($aMin[$eGraphic], $iCenter, $iCenter, $aCurr[1][0], $aCurr[1][1], $aMin[$ePen])
	ResourceSet($hMin, $aMin)
EndFunc   ;==>MinDraw

; ===============================================================================================================================
; Initialize resources for the minute hand
; ===============================================================================================================================
Func MinInit()
	$aMin = ResourceInit($iRadius * 2, $iRadius * 2)
	$aMin[$ePen] = _GDIPlus_PenCreate(0xFFFF0000)
	$aMin[$eCap] = _GDIPlus_ArrowCapCreate($iMinRad / 2, 8)
	_GDIPlus_PenSetCustomEndCap($aMin[$ePen], $aMin[$eCap])
EndFunc   ;==>MinInit

; ===============================================================================================================================
; Finalize resources for the second hand
; ===============================================================================================================================
Func SecDone()
	_GDIPlus_PenDispose($aSec[$ePen])
	ResourceDone($aSec)
EndFunc   ;==>SecDone

; ===============================================================================================================================
; Draw the second hand
; ===============================================================================================================================
Func SecDraw()
	If ($aLast[0][0] = $aCurr[0][0]) And ($aLast[0][1] = $aCurr[0][1]) Then Return
	_GDIPlus_GraphicsFillRect($aSec[$eGraphic], 0, 0, $iRadius * 2, $iRadius * 2)
	_GDIPlus_GraphicsDrawLine($aSec[$eGraphic], $iCenter, $iCenter, $aCurr[0][0], $aCurr[0][1], $aSec[$ePen])
	ResourceSet($hSec, $aSec)
EndFunc   ;==>SecDraw

; ===============================================================================================================================
; Initialize resources for the second hand
; ===============================================================================================================================
Func SecInit()
	$aSec = ResourceInit($iRadius * 2, $iRadius * 2)
	$aSec[$ePen] = _GDIPlus_PenCreate(0xFF000000)
EndFunc   ;==>SecInit

; ===============================================================================================================================
; Finalize drawing resources
; ===============================================================================================================================
Func ResourceDone(ByRef $aInfo)
	_GDIPlus_GraphicsDispose($aInfo[$eGraphic])
	_WinAPI_ReleaseDC(0, $aInfo[$eScrDC])
	_WinAPI_DeleteObject($aInfo[$eBitmap])
	_WinAPI_DeleteDC($aInfo[$eMemDC])
EndFunc   ;==>ResourceDone

; ===============================================================================================================================
; Initialize bitmap resources
; ===============================================================================================================================
Func ResourceInit($iWidth, $iHeight)
	Local $aInfo[$eLast + 1]

	$aInfo[$eScrDC] = _WinAPI_GetDC(0)
	$aInfo[$eMemDC] = _WinAPI_CreateCompatibleDC($aInfo[$eScrDC])
	$aInfo[$eBitmap] = _WinAPI_CreateCompatibleBitmap($aInfo[$eScrDC], $iWidth, $iHeight)
	_WinAPI_SelectObject($aInfo[$eMemDC], $aInfo[$eBitmap])
	$aInfo[$eWidth] = $iWidth
	$aInfo[$eHeight] = $iHeight
	$aInfo[$eGraphic] = _GDIPlus_GraphicsCreateFromHDC($aInfo[$eMemDC])
	_GDIPlus_GraphicsFillRect($aInfo[$eGraphic], 0, 0, $iRadius * 2, $iRadius * 2)
	Return $aInfo
EndFunc   ;==>ResourceInit

; ===============================================================================================================================
; Update layered window with resource information
; ===============================================================================================================================
Func ResourceSet($hGUI, ByRef $aInfo, $iAlpha = -1)
	Local $pSize, $tSize, $pSource, $tSource, $pBlend, $tBlend

	If $iAlpha = -1 Then $iAlpha = $iOpacity
	$tSize = DllStructCreate($tagSIZE)
	$pSize = DllStructGetPtr($tSize)
	DllStructSetData($tSize, "X", $aInfo[$eWidth])
	DllStructSetData($tSize, "Y", $aInfo[$eHeight])
	$tSource = DllStructCreate($tagPOINT)
	$pSource = DllStructGetPtr($tSource)
	$tBlend = DllStructCreate($tagBLENDFUNCTION)
	$pBlend = DllStructGetPtr($tBlend)
	DllStructSetData($tBlend, "Alpha", $iAlpha)
	DllStructSetData($tBlend, "Format", $AC_SRC_ALPHA)
	_WinAPI_UpdateLayeredWindow($hGUI, $aInfo[$eScrDC], 0, $pSize, $aInfo[$eMemDC], $pSource, 0, $pBlend, $ULW_ALPHA)
EndFunc   ;==>ResourceSet

; ===============================================================================================================================
; Finalize resources for the digital time
; ===============================================================================================================================
Func TimeDone()
	_GDIPlus_FontDispose($aTime[$eFont])
	_GDIPlus_FontFamilyDispose($aTime[$eFamily])
	_GDIPlus_StringFormatDispose($aTime[$eFormat])
	_GDIPlus_BrushDispose($aTime[$eBrush])
	ResourceDone($aTime)
EndFunc   ;==>TimeDone

; ===============================================================================================================================
; Draw the digital time
; ===============================================================================================================================
Func TimeDraw()
	Local $sString, $aSize

	If ($aLast[0][0] = $aCurr[0][0]) And ($aLast[0][1] = $aCurr[0][1]) Then Return
	$sString = StringFormat("%02d:%02d:%02d", @HOUR, @MIN, @SEC)
	$aSize = _GDIPlus_GraphicsMeasureString($aTime[$eGraphic], $sString, $aTime[$eFont], $aTime[$eLayout], $aTime[$eFormat])
	DllStructSetData($aTime[$eLayout], "X", $iRadius - (DllStructGetData($aSize[0], "Width") / 2))
	DllStructSetData($aTime[$eLayout], "Y", $iRadius / 3)
	_GDIPlus_GraphicsFillRect($aTime[$eGraphic], 0, 0, $iRadius * 2, $iRadius * 2)
	_GDIPlus_GraphicsDrawStringEx($aTime[$eGraphic], $sString, $aTime[$eFont], $aTime[$eLayout], $aTime[$eFormat], $aTime[$eBrush])
	ResourceSet($hTime, $aTime)
EndFunc   ;==>TimeDraw

; ===============================================================================================================================
; Initialize resources for the digital time
; ===============================================================================================================================
Func TimeInit()
	$aTime = ResourceInit($iRadius * 2, $iRadius * 2)
	$aTime[$eBrush] = _GDIPlus_BrushCreateSolid(0xFF008080)
	$aTime[$eFormat] = _GDIPlus_StringFormatCreate()
	$aTime[$eFamily] = _GDIPlus_FontFamilyCreate("Arial")
	$aTime[$eFont] = _GDIPlus_FontCreate($aTime[$eFamily], 24, 1)
	$aTime[$eLayout] = _GDIPlus_RectFCreate(0, 0, $iRadius * 2, 40)
EndFunc   ;==>TimeInit

; ===============================================================================================================================
; Convert time value to radians
; ===============================================================================================================================
Func TimeToRadians($sTimeType)
	Switch $sTimeType
		Case "sec"
			Return ($nPI / 2) - (@SEC * ($nPI / 30))
		Case "min"
			Return ($nPI / 2) - (@MIN * ($nPI / 30)) - (Int(@SEC / 10) * ($nPI / 180))
		Case "hour"
			Return ($nPI / 2) - (@HOUR * ($nPI / 6)) - (@MIN / 12) * ($nPI / 30)
	EndSwitch
EndFunc   ;==>TimeToRadians

; ===============================================================================================================================
; Handle the WM_NCHITTEST message so our window can be dragged
; ===============================================================================================================================
Func WM_NCHITTEST($hWnd, $iMsg, $iwParam, $ilParam)
	#forceref  $iMsg, $iwParam, $ilParam
	If $hWnd = $hDial Then Return $HTCAPTION
EndFunc   ;==>WM_NCHITTEST
