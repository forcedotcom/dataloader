; *******************************************************
; Example 1 - Create browser at AutoIt homepage, get a reference to
;				the 6th Image on the page (note: the first image is index 0)
;				and display information about it
; *******************************************************

#include <IE.au3>

Local $oIE = _IECreate("http://www.autoitscript.com/")
Local $oImg = _IEImgGetCollection($oIE, 5)
Local $sInfo = "Src: " & $oImg.src & @CR
$sInfo &= "FileName: " & $oImg.nameProp & @CR
$sInfo &= "Height: " & $oImg.height & @CR
$sInfo &= "Width: " & $oImg.width & @CR
$sInfo &= "Border: " & $oImg.border
MsgBox(0, "4th Image Info", $sInfo)

; *******************************************************
; Example 2 - Create browser at AutoIt homepage, get Img collection
;				and display src URL for each
; *******************************************************

#include <IE.au3>

$oIE = _IECreate("http://www.autoitscript.com/")
Local $oImgs = _IEImgGetCollection($oIE)
Local $iNumImg = @extended
MsgBox(0, "Img Info", "There are " & $iNumImg & " images on the page")
For $oImg In $oImgs
	MsgBox(0, "Img Info", "src=" & $oImg.src)
Next
