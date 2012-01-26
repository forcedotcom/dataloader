;- This assumes that you have instrument set to GPIB address 1
; It shows how to use the _viExecCommand function in stand alone mode and combined
; with _viOpen and _viClose.
; It also shows the _viGTL function

#include <Visa.au3>

Local $h_session = 0

; Query the ID of the instrument in GPIB address 3
MsgBox(0, "Step 1", "Open the instrument connection with _viOpen")
Local $h_instr = _viOpen("GPIB::3::0")
MsgBox(0, "Instrument Handle obtained", "$h_instr = " & $h_instr) ; Show the Session Handle
; Query the instrument

MsgBox(0, "Step 2", "Query the instrument using the VISA instrument handle")
Local $s_answer = _viExecCommand($h_instr, "*IDN?") ; $h_instr is NOT A STRING now!
MsgBox(0, "GPIB QUERY result", $s_answer) ; Show the answer
; Query again. There is no need to OPEN the link again

MsgBox(0, "Step 3", "Query again. There is no need to OPEN the link again")
$s_answer = _viExecCommand($h_instr, "*IDN?")
MsgBox(0, "GPIB QUERY result", $s_answer) ; Show the answer

MsgBox(0, "Step 4", "Close the instrument connection using _viClose")
_viClose($h_instr) ; Close the instrument connection

MsgBox(0, "Step 5", "Open the Instrument connection using only the address number")
$h_instr = _viOpen(3)
MsgBox(0, "Instrument Handle obtained", "$h_instr = " & $h_instr) ; Show the Session Handle
; Query the instrument

MsgBox(0, "Step 6", "Query the instrument using the VISA instrument handle")
$s_answer = _viExecCommand($h_instr, "*IDN?") ; $h_instr is NOT A STRING now!
MsgBox(0, "GPIB QUERY result", $s_answer) ; Show the answer
; Query again. There is no need to OPEN the link again

MsgBox(0, "Step 7", "Query again. There is no need to OPEN the link again")
$s_answer = _viExecCommand($h_instr, "*IDN?")
MsgBox(0, "GPIB QUERY result", $s_answer) ; Show the answer

MsgBox(0, "Step 8", "Close the instrument connection using _viClose")
_viClose($h_instr) ; Close the instrument connection

