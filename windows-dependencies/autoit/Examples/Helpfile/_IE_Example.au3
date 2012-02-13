; *******************************************************
; Example 1 - Create browser windows with each of the example pages displayed.
;				The object variable returned can be used just as the object
;				variables returned by _IECreate or _IEAttach
; *******************************************************

#include <IE.au3>

Local $oIE_basic = _IE_Example("basic")
Local $oIE_form = _IE_Example("form")
Local $oIE_table = _IE_Example("table")
Local $oIE_frameset = _IE_Example("frameset")
Local $oIE_iframe = _IE_Example("iframe")
