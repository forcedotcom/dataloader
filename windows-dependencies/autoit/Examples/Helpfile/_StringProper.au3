#include <String.au3>
;Will return : Somebody Lastnames
MsgBox(0, '', _StringProper("somebody lastnames"))
;Will return : Some.Body Last(Name)
MsgBox(0, '', _StringProper("SOME.BODY LAST(NAME)"))
Exit
