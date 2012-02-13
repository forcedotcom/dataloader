#include <GUIConstantsEx.au3>
#include <GDIPlus.au3>

; Create GUI
Local $hWnd = GUICreate("GDI+ Example", 400, 300)
GUISetState()

; Start GDI+
_GDIPlus_Startup()
Local $hGraphics = _GDIPlus_GraphicsCreateFromHWND($hWnd)

Local $hMatrix = _GDIPlus_MatrixCreate()
; Move the matrix half the width and helf the height towards the center
_GDIPlus_MatrixTranslate($hMatrix, 200, 150)
_GDIPlus_MatrixRotate($hMatrix, 45)
_GDIPlus_GraphicsSetTransform($hGraphics, $hMatrix)

Local $hPen = _GDIPlus_PenCreate(0xFF00FF00, 10)

_GDIPlus_GraphicsClear($hGraphics)
; Draw around upper left corner of the GUI, but since we have translated the matrix the object will appear at the center of GUI
_GDIPlus_GraphicsDrawRect($hGraphics, -50, -50, 100, 100, $hPen)


Do
Until GUIGetMsg() = $GUI_EVENT_CLOSE

; Clean up resources
_GDIPlus_PenDispose($hPen)
_GDIPlus_MatrixDispose($hMatrix)
_GDIPlus_GraphicsDispose($hGraphics)
_GDIPlus_Shutdown()
