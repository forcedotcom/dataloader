; This example tries to "reset" the GPIB bus after a BUS "lock". This is rare,
; but it might happen if one of the instruments connected to the BUS has crashed

#include <Visa.au3>

_viGpibBusReset()
