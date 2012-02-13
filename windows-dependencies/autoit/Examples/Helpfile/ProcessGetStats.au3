; retrieve memory infos of the current running process
Local $mem = ProcessGetStats()

; retrieve IO infos of the current running process
Local $IO = ProcessGetStats(-1, 1)
