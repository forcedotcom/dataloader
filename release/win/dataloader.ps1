$wshell = New-Object -ComObject Wscript.Shell

#Locate JavaHome in Registry Path in case JDK is not the default Java Home on the PC
$javaReg     = 'HKLM:\SOFTWARE\JavaSoft\JDK'
$jdkVer      = (Get-ItemProperty -Path $javaReg).CurrentVersion
$javaHome    =  (Get-ItemProperty -Path $javaReg\$jdkVer).JavaHome

#Run Shortcut
If ($javaHome -ne $null){
$return = (Start-Process $javaHome\bin\java.exe -ArgumentList "-jar dataloader-47.0.0-uber.jar salesforce.config.dir=configs" -PassThru)
    
}Else {
    $wshell.Popup("No Version of Oracle JDK could be found on this PC.",0,"SalesForce Runtime Error",0x0 + 0x10)
    }