;- This assumes that you have instrument set to GPIB address 3.
; If you have an instrument in a different address change "GPIB::3::0" to the
; corresponding descriptor. Do the same for the call to _viOpen
; It shows how to use the _viSetAttribute. In this example we use _viSetAttribute
; instead of _viSetTimeout to set the GPIB timeout of a _viExecCommand operation.

#include <Visa.au3>

Local $h_session = 0

; Query the ID of the instrument in GPIB address 3
MsgBox(0, "Step 1", "Simple GPIB query with explicit TIMEOUT set")
Local $s_answer = _viExecCommand("GPIB::3::0", "*IDN?", 10000) ; 10 secs timeout
MsgBox(0, "GPIB QUERY result", $s_answer) ; Show the answer

; This is the same as using the _viSetAttribute function first:
MsgBox(0, "Step 2", "_vOpen + timeout using _viSetAttribute + GPIB query")
Local $h_instr = _viOpen(3)
; NOTE - This is the same as: _viSetTimeout($h_instr, 10000)
_viSetAttribute($h_instr, $VI_ATTR_TMO_VALUE, 10000) ; 10000 ms = 10 secs

$s_answer = _viExecCommand($h_instr, "*IDN?") ; No need to set the timeout now
MsgBox(0, "GPIB QUERY result", $s_answer) ; Show the answer

MsgBox(0, "Step 3", "Close the Instrument connection using _viClose")
_viClose($h_instr) ; Close the instrument connection
