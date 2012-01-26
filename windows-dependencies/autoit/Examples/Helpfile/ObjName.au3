Local $oInternet = ObjCreate("InternetExplorer.Application")
$oInternet.Navigate("http://www.google.com") ; Opening a web page that contains a form
Sleep(4000) ; Give the page time to load

Local $oDoc = $oInternet.document ; Example object to test
Local $oForm = $oDoc.forms(0) ; Example object to test

MsgBox(0, "", "Interface name of $oInternet is: " & ObjName($oInternet) & @CRLF & _
		"Object name of $oInternet is:    " & ObjName($oInternet, 2) & @CRLF & _
		"Interface name of $oDoc is:      " & ObjName($oDoc) & @CRLF & _
		"Object name of $oDoc is:         " & ObjName($oDoc, 2) & @CRLF & _
		"Interface name of $oForm is:     " & ObjName($oForm) & @CRLF & _
		"Object name of $oForm is:        " & ObjName($oForm, 2))

