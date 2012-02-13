StringIsFloat("1.5") ;returns 1
StringIsFloat("7.") ;returns 1 since contains decimal
StringIsFloat("-.0") ;returns 1
StringIsFloat("3/4") ;returns 0 since '3' slash '4' is not a float
StringIsFloat("2") ; returns 0 since '2' is an interger, not a float

StringIsFloat(1.5) ;returns 1 since 1.5 converted to string contain .
StringIsFloat(1.0) ;returns 0 since 1.0 converted to string does not contain .
