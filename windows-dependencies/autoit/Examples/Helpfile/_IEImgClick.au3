; *******************************************************
; Example 1 - Click on IMG by Alt text
; *******************************************************

#include <IE.au3>

Local $oIE = _IE_Example("basic")
_IEImgClick($oIE, "AutoIt Homepage Image", "alt")

; *******************************************************
; Example 2 - Click on IMG by name
; *******************************************************

#include <IE.au3>

$oIE = _IE_Example("basic")
_IEImgClick($oIE, "AutoItImage", "name")

; *******************************************************
; Example 3 - Click on IMG by src sub-string
; *******************************************************

#include <IE.au3>

$oIE = _IE_Example("basic")
_IEImgClick($oIE, "autoit_6_240x100.jpg", "src")

; *******************************************************
; Example 4 - Click on IMG by full src string
; *******************************************************

#include <IE.au3>

$oIE = _IE_Example("basic")
_IEImgClick($oIE, "http://www.autoitscript.com/images/autoit_6_240x100.jpg")
