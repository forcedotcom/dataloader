; Excel Automation Example
;
; Using direct assigments of 2-dimensional array's
;
; Based on AutoItCOM version 3.1.1
;
; Beta version 06-02-2005

$MyExcel = ObjCreate("Excel.Application")	; Create an Excel Object

if @error then 
  Msgbox (0,"","Error creating Excel object. Error code: " & @error)
  exit
endif

if not IsObj($MyExcel) then 
  Msgbox (0,"ExcelTest","I'm sorry, but creation of an Excel object failed.")
  exit
endif


$MyExcel.Visible = 1		; Let the guy show himself

$MyExcel.workbooks.add		; Add a new workbook

				; Example: Fast Fill some cells

Msgbox (0,"","Click 'ok' to fastfill some cells")

dim $arr[16][16]

  for $i = 0 to 15
    for $j = 0 to 15
	$arr[$i][$j] = Chr($i+65)&($j+1)
    next
 next   

; Set all values in one shot!
$MyExcel.activesheet.range("A1:O16").value = $arr

 
Msgbox (0,"","Click 'ok' to clear the cells")

$MyExcel.activesheet.range("A1:O16").clear

Sleep(2000)

$MyExcel.activeworkbook.saved = 1	; To prevent 'yes/no' questions from Excel

$MyExcel.quit				; Get rid of him.

Msgbox (0,"ExcelTest","Is Excel gone now ?")
					; Nope, only invisible, 
					; but should be still in memory.

$MyExcel = 0		; Loose this object.
					; Object will also be automatically discarded when you exit the script

exit

