#ignoreFunc PluginFunc1
Local $handle = PluginOpen("example.dll")

PluginFunc1(0.1, 0.2) ; will call the plugin function with  2 parameters

PluginClose($handle)
