; Excel Automation Example
;
; Based on AutoIt version 3.1.0
;
;
; Beta version 06-02-2005


; NOTE: This will open an existing instance of Excel

; So Excel must be started first!!

$oExcel = ObjGet("","Excel.Application")	; Get an existing Excel Object

if @error then 
  Msgbox (0,"ExcelFileTest","You don't have Excel running at this moment. Error code: " & hex(@error,8))
  exit
endif

if IsObj($oExcel) then Msgbox (0,"","You successfully attached to the existing Excel Application.")


$oExcel.Visible = 1		; Let the guy show himself

$oExcel.workbooks.add		; Add a new workbook

				; Example: Fill some cells

Msgbox (0,"","Click 'ok' to fill some cells")

dim $i
dim $j

WITH $oExcel.activesheet
  for $i = 1 to 15
    for $j = 1 to 15
	.cells($i,$j).value = $i
    next
  next   
  
  Msgbox (0,"","Click 'ok' to clear the cells")
  .range("A1:O15").clear

ENDWITH

sleep (2000)

$oExcel.activeworkbook.saved = 1	; To prevent 'yes/no' questions from Excel

$oExcel.quit				; Get rid of him.

Msgbox (0,"","Is Excel gone now??")	; Nope, should be still in memory.

$oExcel = 0				; Loose the object

exit

