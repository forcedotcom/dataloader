;=========================================================
;	Create the struct
;	struct {
;		int				var1;
;		unsigned char	var2;
;		unsigned int	var3;
;		char			var4[128];
;	}
;=========================================================
Local $str = "int;byte;uint;char[128]"
Local $a = DllStructCreate($str)
If @error Then
	MsgBox(0, "", "Error in DllStructCreate " & @error);
	Exit
EndIf

;=========================================================
;	Set data in the struct
;	struct.var1	= -1;
;	struct.var2	= 255;
;	struct.var3	= INT_MAX; -1 will be typecasted to (unsigned int)
;	strcpy(struct.var4,"Hello");
;	struct.var4[0]	= 'h';
;=========================================================
DllStructSetData($a, 1, -1)
DllStructSetData($a, 2, 255)
DllStructSetData($a, 3, -1)
DllStructSetData($a, 4, "Hello")
DllStructSetData($a, 4, Asc("h"), 1)

;=========================================================
;	Display info in the struct
;=========================================================
MsgBox(0, "DllStruct", "Struct Size: " & DllStructGetSize($a) & @CRLF & _
		"Struct pointer: " & DllStructGetPtr($a) & @CRLF & _
		"Data:" & @CRLF & _
		DllStructGetData($a, 1) & @CRLF & _
		DllStructGetData($a, 2) & @CRLF & _
		DllStructGetData($a, 3) & @CRLF & _
		DllStructGetData($a, 4))

;=========================================================
;	Free the memory allocated for the struct
;=========================================================
$a = 0
