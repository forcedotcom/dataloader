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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.dataloader.client.SimplePost;
import com.salesforce.dataloader.client.SimplePostFactory;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.exception.ParameterLoadException;

import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * the oauth token flow. this is normally used for client to server where the client is not a secured environment
 * as it does not involve a secret. We use this as our standard login for SF oauth. The disadvantage to this flow is
 * it prompts for authentication and authorization everytime.
 */
public class OAuthLoginFromBrowserFlow extends Dialog {
	enum LoginStatus { WAIT, FAIL, SUCCESS };
    protected static Logger logger = LogManager.getLogger(OAuthLoginFromBrowserFlow.class);
    protected final Config config;
    private static LoginStatus loginResult = LoginStatus.WAIT;
    final AtomicBoolean isDialogOpen = new AtomicBoolean(true);

    public OAuthLoginFromBrowserFlow(Shell parent, Config config) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.FILL);
        this.config = config;
    }
    
    public boolean open() throws UnsupportedEncodingException {
    	setLoginStatus(LoginStatus.WAIT);
 
        String oAuthTokenURLStr = config.getString(Config.OAUTH_SERVER) + "/services/oauth2/token";
        SimplePost client = SimplePostFactory.getInstance(config, oAuthTokenURLStr,
                new BasicNameValuePair("response_type", "device_code"),
                new BasicNameValuePair("client_id", config.getString(Config.OAUTH_CLIENTID)),
                new BasicNameValuePair("scope", "api")
        );
        try {
            client.post();
	        InputStream in = client.getInput();
	        if (!client.isSuccessful()) {
	        	ByteArrayOutputStream result = new ByteArrayOutputStream();
	        	 byte[] buffer = new byte[1024];
	        	 for (int length; (length = in.read(buffer)) != -1; ) {
	        	     result.write(buffer, 0, length);
	        	 }
	        	 String response = result.toString("UTF-8");
	        	 System.out.println(response);
	        	 return false;
	        }
		    ObjectMapper mapper = new ObjectMapper();
		    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		    final Map responseMap = mapper.readValue(in, Map.class);
		    final String deviceCode = (String)responseMap.get("device_code");
		    final String userCodeStr = (String) responseMap.get("user_code");
		    logger.debug("User Code: " + userCodeStr);
		    final String verificationURLStr = (String) responseMap.get("verification_uri")
		    		+ "?user_code=" + userCodeStr;
		    logger.debug("Verification URL: " + verificationURLStr);
		    
		    // Create the dialog window
	        Display display = getParent().getDisplay();
	        int style = getParent().getStyle();
	        Shell shell = new Shell(getParent(), style | SWT.APPLICATION_MODAL);
	        Font f = shell.getFont();
	        FontData[] farr = f.getFontData();
	        FontData fd = farr[0];
	        fd.setStyle(SWT.BOLD);
	        Font boldFont = new Font(Display.getCurrent(), fd);
	        shell.setText(Labels.getString("OAuthMFAWithUserCodeFlow.title"));

	        Composite container = new Composite(shell, SWT.NONE);
	        GridLayout containerLayout = new GridLayout(1, false);
	        container.setLayout(containerLayout);
	        shell.setLayout(new FillLayout());

	        Composite infoComp = new Composite(container, SWT.NONE);
	        GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_CENTER);
	        data.heightHint = 100;
	        data.widthHint = 600;
	        GridLayout layout = new GridLayout(2, false);
	        layout.marginHeight = 0;
	        layout.marginWidth = 0;
	        layout.verticalSpacing = 0;
	        infoComp.setLayout(layout);
	        infoComp.setLayoutData(data);
	        infoComp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
	        
	        Label label = new Label(infoComp, SWT.RIGHT);
	        label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
	        label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
	        label.setText(Labels.getString("OAuthMFAWithUserCodeFlow.authStep1Title"));
	        label.setFont(boldFont);

	        label = new Label(infoComp, SWT.WRAP);
	        label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
	        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        label.setText(Labels.getString("OAuthMFAWithUserCodeFlow.authStep1Content"));

	        label = new Label(infoComp, SWT.WRAP);
	        label = new Label(infoComp, SWT.WRAP);

	        label = new Label(infoComp, SWT.RIGHT);
	        label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
	        label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
	        label.setText(Labels.getString("OAuthMFAWithUserCodeFlow.authStep2Title"));
	        label.setFont(boldFont);

	        label = new Label(infoComp, SWT.WRAP);
	        label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
	        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        label.setText(Labels.getString("OAuthMFAWithUserCodeFlow.authStep2Content"));

	        label = new Label(infoComp, SWT.WRAP);
	        label = new Label(infoComp, SWT.WRAP);

	        label = new Label(infoComp, SWT.RIGHT);
	        label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
	        label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
	        label.setText(Labels.getString("OAuthMFAWithUserCodeFlow.authStep3Title"));
	        label.setFont(boldFont);

	        label = new Label(infoComp, SWT.WRAP);
	        label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
	        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        label.setText(Labels.getString("OAuthMFAWithUserCodeFlow.authStep3Content"));

	        
	        Composite contentComp = new Composite(container, SWT.NONE);
	        data = new GridData(GridData.FILL_BOTH);
	        contentComp.setLayoutData(data);
	        GridLayout contentLayout = new GridLayout(3, false);
	        contentLayout.verticalSpacing = 10;
	        contentComp.setLayout(contentLayout);

	        label = new Label(contentComp, SWT.RIGHT);
	        label.setFont(new Font(Display.getCurrent(), fd));
	        label.setText(Labels.getString("OAuthMFAWithUserCodeFlow.verificationURL"));
	        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
	        label.setLayoutData(data);
	        Link link = new Link(contentComp, SWT.END);
	        link.setText("<a href=\""+verificationURLStr+"\">"+verificationURLStr+"</a>");
	        link.addSelectionListener(new SelectionAdapter() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	openURL(e.text);
	            }
	          });
	        final Clipboard clipboard = new Clipboard(display);
	        Button copy = new Button(contentComp, SWT.PUSH);
	        copy.setText(Labels.getString("OAuthMFAWithUserCodeFlow.copyToClipboardButton"));
	        copy.addListener(SWT.Selection, new Listener() {
	          public void handleEvent(Event e) {
	            String textData = verificationURLStr;
	            TextTransfer textTransfer = TextTransfer.getInstance();
	            clipboard.setContents(new Object[] { textData },
	                new Transfer[] { textTransfer });
	          }
	        });
	        
	        int pollingIntervalInSec = 5;	        
	        try {
	        	pollingIntervalInSec = ((Integer)responseMap.get("interval")).intValue();
	        } catch (NumberFormatException e) {
	        	// fail silently
	        }

	        shell.pack();
	        shell.open();
	        
	        Thread checkLoginThread = checkLoginStatus(pollingIntervalInSec, oAuthTokenURLStr, deviceCode);

	        while (!shell.isDisposed()) {
	            if (!display.readAndDispatch()) {
	                display.sleep();
	            }
	            if (getLoginStatus() != LoginStatus.WAIT || !checkLoginThread.isAlive()) {
	            	shell.close();
	            	break;
	            }
	        }
	        this.isDialogOpen.set(false);
	    } catch (IOException | ParameterLoadException e) {
	    	logger.error(e.getMessage());
			return false;
		}
        return getLoginStatus() == LoginStatus.SUCCESS;
    }
    
    private Thread checkLoginStatus(final int pollingIntervalInSec, final String oAuthTokenURLStr, final String deviceCode) {
        Thread successfulLogincheckerThread = new Thread() {
        	public void run() {
        		// Poll for 20 minutes.
        		// Expiry of user code is detected by server returning an error 
        		// other than 'authorization_pending'.
		        int maxPollingTimeInSec = 1200; 
		        int elapsedTimeInSec = 0;
		        SimplePost client;
		        InputStream in;
		        while (elapsedTimeInSec <= maxPollingTimeInSec && isDialogOpen.get()) {
		        	try {
						Thread.sleep(pollingIntervalInSec * 1000);
					} catch (InterruptedException e) {
						// do nothing
					}
		        	elapsedTimeInSec += pollingIntervalInSec;
		            client = SimplePostFactory.getInstance(config, oAuthTokenURLStr,
		                    new BasicNameValuePair("grant_type", "device"),
		                    new BasicNameValuePair("client_id", config.getString(Config.OAUTH_CLIENTID)),
		                    new BasicNameValuePair("code", deviceCode)
		            );
		            try {
						client.post();
					} catch (ParameterLoadException e) {
				    	logger.error(e.getMessage());;
						setLoginStatus(LoginStatus.FAIL);
						return;
					} catch (IOException e) {
				    	logger.error(e.getMessage());
						setLoginStatus(LoginStatus.FAIL);
						return;
					}
			        in = client.getInput();
	    	        if (client.isSuccessful()) {
	        	        try {
							OAuthFlow.processSuccessfulLogin(client.getInput(), config);
						} catch (IOException e) {
					    	logger.error(e.getMessage());
							setLoginStatus(LoginStatus.FAIL);
							return;
						}
	        	        // got the session id => SUCCESSful login
						setLoginStatus(LoginStatus.SUCCESS);
	        	        return; 
	    	        } else { // read the error message and log it
	    			    ObjectMapper mapper = new ObjectMapper();
	    			    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	    			    try {
							Map responseMap = mapper.readValue(in, Map.class);
							String errorStr = (String)responseMap.get("error");
							String errorDesc = (String)responseMap.get("error_description");
							if ("authorization_pending".equalsIgnoreCase(errorStr)) {
								// waiting for the user to login
								logger.debug(errorStr + " - " + errorDesc);
							} else {
								// a failure occurred. Exit.
								logger.error(errorStr + " - " + errorDesc);
								setLoginStatus(LoginStatus.FAIL);
								break;
							}
						} catch (IOException e) {
					    	logger.debug(e.getMessage());
					    	continue;
					    }
	    	        }
		        } // while loop
		        logger.error("User closed the dialog or timed out waiting for login");
		        setLoginStatus(LoginStatus.FAIL);
        	}
        };
        successfulLogincheckerThread.start();
        return successfulLogincheckerThread;
    }
    
    static private synchronized void setLoginStatus(LoginStatus value) {
    	loginResult = value;
    }
    
    static private LoginStatus getLoginStatus() {
    	return loginResult;
    }
    
    private void openURL(String url) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                logger.error(e.getMessage());
                openURLUsingNativeCommand(url);
            }
        } else {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException e) {
                logger.error(e.getMessage());
                openURLUsingNativeCommand(url);
            }
        }
    }
    
    private void openURLUsingNativeCommand(String url) {
        Runtime runtime = Runtime.getRuntime();
        String osName = System.getProperty("os.name");
        try {
            if (osName.toLowerCase().indexOf("mac") >= 0) {
                runtime.exec("open " + url);
            }
            else if (osName.toLowerCase().indexOf("win") >= 0) {
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else { //assume Unix or Linux
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
        catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}
