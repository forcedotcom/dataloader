StringIsInt("+42") ;returns 1
StringIsInt("-00") ;returns 1
StringIsInt("1.0") ;returns 0 due to the decimal point
StringIsInt(1.0) ;returns 1 due to number-string conversion
StringIsInt("1+2") ;returns 0 due to plus sign
