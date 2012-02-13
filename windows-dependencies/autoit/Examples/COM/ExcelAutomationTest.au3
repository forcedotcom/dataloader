; Excel Automation Example
;
; Testfile for AutoIt 3.1.1.x
;

$MyExcel = ObjCreate("Excel.Application")	; Create an Excel Object

if @error then 
  Msgbox (0,"ExcelTest","Error creating the Excel Object. Error code: " & @error)
  exit
endif

if not IsObj($MyExcel) then 
  Msgbox (0,"ExcelTest","I'm sorry, but creation of the Excel object failed.")
  exit
endif


$MyExcel.Visible = 1		; Let the guy show himself

$MyExcel.workbooks.add		; Add a new workbook

				; Example: Fill some cells

Msgbox (0,"","Click 'ok' to fill some cells")

dim $i
dim $j

WITH $MyExcel.activesheet
  for $i = 1 to 15
    for $j = 1 to 15
	.cells($i,$j).value = $i
    next
  next   
  
  Msgbox (0,"","Click 'ok' to clear the cells")
  .range("A1:O15").clear
 
ENDWITH

Sleep(2000)

$MyExcel.activeworkbook.saved = 1	; To prevent 'yes/no' questions from Excel

$MyExcel.quit				; Get rid of him.

Msgbox (0,"ExcelTest","Is Excel gone now ?")
					; Nope, only invisible, 
					; but should be still in memory.

$MyExcel=""			; Only now Excel is removed from memory.

exit

