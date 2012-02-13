; Double click at the current mouse pos
MouseClick("left")
MouseClick("left")

; Double click at 0,500
MouseClick("left", 0, 500, 2)


; SAFER VERSION of Double click at 0,500 - takes into account user's control panel settings
MouseClick("primary", 0, 500, 2)
