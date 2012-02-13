; AutoIt 3.1.1.x beta version
;
; COM Test file
;
; Test usage of AutoItX from within AutoItCOM

$oAutoIt = ObjCreate("AutoItX3.Control")
if @error then
	Msgbox(0,"AutoItX Test","Failed to open AutoItX. Error code: " & hex(@error,8))
	exit
endif


$oAutoIt.ClipPut("I am copied to the clipboard")

$text = $oAutoIt.ClipGet()

Msgbox(0,"Clipboard test","Clipboard contains: " & $text)

; This will create a tooltip in the upper left of the screen

Msgbox(0,"Tooltip test","Press OK to create a tooltip in the upper left corner.")

$oAutoIt.ToolTip("This is a tooltip", 0, 0)
$oAutoIt.Sleep(1000)     ; Sleep to give tooltip time to display

$var = $oAutoIt.RegRead("HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion", "ProgramFilesDir")

Msgbox(0,"RegRead Test","Program files are in:" & $var)

