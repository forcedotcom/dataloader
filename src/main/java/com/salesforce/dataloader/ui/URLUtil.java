/*
 * Copyright (c) 2015, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.dataloader.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
   
public class URLUtil {
    private static Logger logger = LogManager.getLogger(URLUtil.class);
    public static void openURL(String url) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                logger.debug("trying to use desktop.browse() method");
                desktop.browse(new URI(url));
            } catch (Exception e) {
                logger.debug(e.getMessage());
                openURLUsingNativeCommand(url);
            }
        } else {
            logger.debug("trying to use native command");
            openURLUsingNativeCommand(url);
        }
    }

    private static void openURLUsingNativeCommand(String url) {
        Runtime runtime = Runtime.getRuntime();
        String osName = System.getProperty("os.name");
        try {
            if (osName.toLowerCase().indexOf("mac") >= 0) {
                logger.debug("trying to use open command on mac");
                runtime.exec("open " + url);
            }
            else if (osName.toLowerCase().indexOf("win") >= 0) {
                logger.debug("trying to use rundll32 command on windows");
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else { //assume Unix or Linux
                try {
                    logger.debug("trying to use xdg-open command on linux");
                    runtime.exec("xdg-open " + url);
                } catch (IOException e) {
                    logger.debug(e.getMessage());
                    logger.debug("trying to browser-specific command on linux");
                    String[] browsers = {
                            "firefox", "chrome", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
                    String browser = null;
                    for (int count = 0; count < browsers.length && browser == null; count++)
                        if (runtime.exec(
                                new String[] {"which", browsers[count]}).waitFor() == 0) {
                            browser = browsers[count];
                        }
                    if (browser == null) {
                        throw new Exception("Could not find web browser");
                    } else {
                        runtime.exec(new String[] {browser, url});
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
