ProgressOn("Progress Meter", "Increments every second", "0 percent")
For $i = 10 To 100 Step 10
	Sleep(1000)
	ProgressSet($i, $i & " percent")
Next
ProgressSet(100, "Done", "Complete")
Sleep(500)
ProgressOff()
