;Places the input box in the top left corner displaying the characters as they
;are typed.
Local $answer = InputBox("Question", "Where were you born?", "Planet Earth", "", _
		 - 1, -1, 0, 0)

;Asks the user to enter a password.  Don't forget to validate it!
Local $passwd = InputBox("Security Check", "Enter your password.", "", "*")

;Asks the user to enter a 1 or 2 character response.  The M in the password
;field indicates that blank string are not accepted and the 2 indicates that the
;responce will be at most 2 characters long.
Local $value = InputBox("Testing", "Enter the 1 or 2 character code.", "", " M2")
