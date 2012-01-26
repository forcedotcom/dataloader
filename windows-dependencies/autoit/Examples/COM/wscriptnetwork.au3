; Wscript.Network Example
;
; Based on AutoItCOM version 3.1.0
;
; Beta version 06-02-2005

$objNetwork = ObjCreate("WScript.Network")

if @error then 
	Msgbox (0,"Wscript.network Test","I'm sorry, but creation of object $objNetwork failed. Error code: " & @error)
	exit
endif

$colDrives = $objNetwork.EnumNetworkDrives

if not IsObj($colDrives) then 
	Msgbox (0,"Wscript.network Test","I'm sorry, but creation of object $coldrives failed.")
	exit
endif

$NumDrives =  $colDrives.Count

if $NumDrives = 0 then
	Msgbox(0,"wscript.network", "You have currently no network drives")
else
 	For $i = 0 to $colDrives.Count-1 Step 2
	  Msgbox(0,"Wscript.network", "Drive letter: " & $colDrives.Item($i) & @TAB & " is mapped to: " & $colDrives.Item($i + 1))
	 Next
endif


exit
