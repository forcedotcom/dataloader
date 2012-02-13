IsFloat(3.14159) ;returns 1
IsFloat(3.000) ;returns 0 since value is integer 3
IsFloat(1 / 2 - 5) ;returns 1
IsFloat(1.5e3) ;returns 0 since 1.5e3 = 1500
IsFloat("12.345") ;returns 0 since is a string
