SplashTextOn("Title", "Message goes here.", -1, -1, -1, -1, 4, "", 24)
Sleep(3000)
SplashOff()

;; FLICKER
Local $message = ""
SplashTextOn("TitleFoo", $message, -1, -1, -1, -1, 4, "")
For $x = 1 To 20
	$message = $message & $x & @LF
	SplashTextOn("TitleFoo", $message, -1, -1, -1, -1, 4, "")
	Sleep(100)
Next

;; SMOOTH
$message = ""
SplashTextOn("TitleFoo", $message, -1, -1, -1, -1, 4, "")
For $x = 1 To 20
	$message = $message & $x & @LF
	ControlSetText("TitleFoo", "", "Static1", $message)
	Sleep(100)
Next
