; This example performs a search on the GPIB bus and shows the results in a MsgBox

#include <Visa.au3>

Local $a_descriptor_list[1], $a_idn_list[1]
_viFindGpib($a_descriptor_list, $a_idn_list, 1)
