; *******************************************************
; Example 1 - Open frameset example, get collection of frames
;				and loop through them displaying their source URL's
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("frameset")
Local $oFrames = _IEFrameGetCollection($oIE)
Local $iNumFrames = @extended
For $i = 0 To ($iNumFrames - 1)
	Local $oFrame = _IEFrameGetCollection($oIE, $i)
	MsgBox(0, "Frame Info", _IEPropertyGet($oFrame, "locationurl"))
Next
