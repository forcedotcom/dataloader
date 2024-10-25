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
package com.salesforce.dataloader.process;
/*
 * Copyright (c) 2005, salesforce.com, inc.
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


/**
 * @author Lexi Viripaeff
 */

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.action.progress.NihilistProgressAdapter;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.LastRunProperties;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.ControllerInitializationException;
import com.salesforce.dataloader.exception.OAuthBrowserLoginRunnerException;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.exception.ProcessInitializationException;
import com.salesforce.dataloader.ui.Labels;
import com.salesforce.dataloader.util.AppUtil;
import com.salesforce.dataloader.util.ExitException;
import com.salesforce.dataloader.util.OAuthBrowserLoginRunner;
import com.sforce.soap.partner.fault.ApiFault;

import com.salesforce.dataloader.util.DLLogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.InitializingBean;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProcessRunner implements InitializingBean, IProcess {

    /**
     * Comment for <code>DYNABEAN_ID</code>
     */

    //logger
    private static Logger logger = DLLogManager.getLogger(ProcessRunner.class);
    
    private String name = null; // name of the loaded process DynaBean

    // config override parameters
    private final Map<String, String> configOverrideMap = new HashMap<String, String>();

    private Controller controller;
    
    private ILoaderProgress monitor;
    
    private static final String PROP_NAME_ARRAY[] = {
            AppConfig.PROP_OPERATION,
            AppConfig.PROP_USERNAME,
            AppConfig.PROP_PASSWORD,
            AppConfig.PROP_DAO_TYPE,
            AppConfig.PROP_DAO_NAME,
            AppConfig.PROP_ENTITY,
    };
    
    /**
     * Enforce use of factory method - getInstance() by hiding the constructor
     */
    protected ProcessRunner() {
    }

    public synchronized void run(ILoaderProgress monitor) throws Exception {
        if (monitor == null) {
            monitor = new NihilistProgressAdapter();
        }
        this.monitor = monitor;
        final String oldName = Thread.currentThread().getName();
        String name = getName();

        if (name != null && !name.isBlank()) {
            setThreadName(name);
        }

        try {
            controller = Controller.getInstance(getConfigOverrideMap());
        } catch (ControllerInitializationException e) {
            throw new RuntimeException(e);
        }

        try {
            logger.info(Messages.getString("Process.initializingEngine")); //$NON-NLS-1$
            AppConfig appConfig = controller.getAppConfig();
            if (!(appConfig.contains(AppConfig.PROP_USERNAME) && appConfig.contains(AppConfig.PROP_PASSWORD))
                    && appConfig.getBoolean(AppConfig.PROP_OAUTH_LOGIN_FROM_BROWSER)) {
                doLoginFromBrowser(appConfig);
            }
            // Make sure that the required properties are specified.
            validateConfigProperties(appConfig);
            if (name == null || name.isBlank()) {
                // this can occur only if "process.name" is not specified as a  command line option
                name = appConfig.getString(AppConfig.PROP_OPERATION);
                this.setName(name);
                setThreadName(name);
            };

            // create files for status output unless it's an extract and status output is disabled
            if (!appConfig.getOperationInfo().isExtraction() || appConfig.getBoolean(AppConfig.PROP_ENABLE_EXTRACT_STATUS_OUTPUT)) {
                controller.setStatusFiles(appConfig.getString(AppConfig.PROP_OUTPUT_STATUS_DIR), true, false);
            }

            logger.info(Messages.getFormattedString("Process.loggingIn", appConfig.getAuthEndpointForCurrentEnv())); //$NON-NLS-1$
            if (controller.login()) {
                // get the field info (using the describe call)
                logger.info(Messages.getString("Process.settingFieldTypes")); //$NON-NLS-1$
                controller.setFieldTypes();

                // instantiate the map
                logger.info(Messages.getString("Process.creatingMap")); //$NON-NLS-1$
                controller.initializeOperation(appConfig.getString(AppConfig.PROP_DAO_TYPE), 
                        appConfig.getString(AppConfig.PROP_DAO_NAME), appConfig.getString(AppConfig.PROP_ENTITY));

                // execute the requested operation
                controller.executeAction(monitor);

                // save last successful run date
                // FIXME look into a better place so that long runs don't skew this
                appConfig.setValue(LastRunProperties.LAST_RUN_DATE, Calendar.getInstance().getTime());
                appConfig.saveLastRun();
            } else {
                logger.fatal(Messages.getString("Process.loginError")); //$NON-NLS-1$
            }
        } catch (ApiFault e) {
            // this is necessary, because the ConnectionException doesn't display the login fault message
            throw new RuntimeException(e.getExceptionMessage(), e);
        } finally {
            // make sure all is closed and saved
            if (controller.getDao() != null) controller.getDao().close();

            // restore original thread name
            setThreadName(oldName);
        }
    }
    
    public ILoaderProgress getMonitor() {
        return this.monitor;
    }

    private void setThreadName(final String name) {
        if (name != null && name.length() > 0) {
            try {
                Thread.currentThread().setName(name);
            } catch (Exception e) {
                // ignore, just leave the default thread name intact
                logger.warn("Error setting thread name", e);
            }
        }
    }

    public synchronized Map<String, String> getConfigOverrideMap() {
        return configOverrideMap;
    }

    public synchronized void setConfigOverrideMap(Map<String, String> configOverrideMap) {
        if (this.configOverrideMap.isEmpty()) {
            this.configOverrideMap.putAll(configOverrideMap);
            if (getName() != null && !getName().isBlank()) {
                this.configOverrideMap.put(AppConfig.PROP_PROCESS_NAME, getName());
            }
        } else {
            throw new IllegalStateException("Attempting to set configOverrideMap but there are already "
                    + this.configOverrideMap.size() + " entries");
        }
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        final String name = getName();
        if(name == null || name.length() == 0) {
            logger.fatal(Messages.getFormattedString("Process.missingRequiredArg", "name"));
            throw new ParameterLoadException(Messages.getFormattedString("Process.missingRequiredArg", "name"));
        }
    }

    public static void logErrorAndExitProcess(String message, Throwable throwable, int exitCode) {
        if (throwable == null) {
            logger.fatal(message);
        } else { // throwable != null
            logger.fatal(message, throwable);
        }
        throw new ExitException(throwable, exitCode);
    }
    
    public static ProcessRunner runBatchMode(Map<String, String>argMap, ILoaderProgress progressMonitor) throws UnsupportedOperationException {
        ProcessRunner runner = null;
        try {
            // create the process
            runner = ProcessRunner.getInstance(argMap);
            if (runner == null) {
                logErrorAndExitProcess("Process runner is null",
                        new NullPointerException(), AppUtil.EXIT_CODE_CLIENT_ERROR);
            }
            // run the process
            runner.run(progressMonitor);
            progressMonitor = runner.getMonitor();
            if (progressMonitor != null) {
                if (progressMonitor.isCanceled()) {
                    logErrorAndExitProcess(progressMonitor.getMessage(), null, AppUtil.EXIT_CODE_CLIENT_ERROR);
                } else if (!progressMonitor.isSuccess()) {
                    logErrorAndExitProcess(progressMonitor.getMessage(), null, AppUtil.EXIT_CODE_SERVER_ERROR);
                } else if (AppConfig.getCurrentConfig() != null
                        && AppConfig.getCurrentConfig().getBoolean(AppConfig.PROP_PROCESS_EXIT_WITH_ERROR_ON_FAILED_ROWS_BATCH_MODE)
                        && progressMonitor.getNumberRowsWithError() > 0) {
                    DataLoaderRunner.setExitCode(AppUtil.EXIT_CODE_RESULTS_ERROR);
                }
            }
        } catch (Throwable t) {
            if (t.getClass().equals(UnsupportedOperationException.class)) {
                // this is done to allow integration tests to continue
                // after a negative test of an operation results in an exception
                throw (UnsupportedOperationException)t;
            }
            if (t.getClass().equals(ExitException.class)) {
                throw (ExitException)t;
            }
            logErrorAndExitProcess("Unable to run process", t, AppUtil.EXIT_CODE_OPERATION_ERROR);
        }
        return runner;
    }

    /**
     * Get an instance of the engine runner that can be scheduled in it's own thread
     *
     * @param args String set of name=value pairs of arguments for the runner
     * @throws ProcessInitializationException
     */

    /**
     * @param argMap
     * @return instance of ProcessRunner
     * @throws ProcessInitializationException
     */
    private static synchronized ProcessRunner getInstance(Map<String, String> argMap) throws ProcessInitializationException {
        logger.info(Messages.getString("Process.initializingEngine")); //$NON-NLS-1$
        String dynaBeanID = argMap.get(AppConfig.PROP_PROCESS_NAME);
        ProcessRunner runner;
        if (dynaBeanID == null || dynaBeanID.isEmpty()) {
            // operation and other process params are specified in config.properties
            logger.info(AppConfig.PROP_PROCESS_NAME 
                    + "is not specified in the command line. Loading the process properties from config.properties.");
            runner = new ProcessRunner();
            
            if (argMap.containsKey(AppConfig.PROP_PROCESS_THREAD_NAME)) {
                runner.setName(argMap.get(AppConfig.PROP_PROCESS_THREAD_NAME));
            }
        } else {
            // process name specified in the command line arg. 
            // Load its DynaBean through process-conf.xml
            logger.info(AppConfig.PROP_PROCESS_NAME 
                        + "is specified in the command line. Loading DynaBean with id " 
                        + dynaBeanID 
                        + " from process-conf.xml located in folder "
                        + AppConfig.getConfigurationsDir());
            runner = ProcessConfig.getProcessInstance(dynaBeanID);
        }
        runner.getConfigOverrideMap().putAll(argMap);
        return runner;
    }

    /**
     * Get process runner based on the name by reading the bean from configuration file
     * @param processName
     * @return A default instance of ProcessRunner (based on config)
     * @throws ProcessInitializationException
     */
    public static ProcessRunner getInstance(String processName) throws ProcessInitializationException {
        return ProcessConfig.getProcessInstance(processName);
    }

    public Controller getController() {
        return controller;
    }
    
    private static void validateConfigProperties(AppConfig appConfig) throws ProcessInitializationException {
        if (appConfig == null) {
            throw new ProcessInitializationException("Configuration not initialized");
        }

        for (String propName : PROP_NAME_ARRAY) {
            String propVal = appConfig.getString(propName);
            if (propName.equals(AppConfig.PROP_PASSWORD) && (propVal == null || propVal.isBlank())) {
                // OAuth access token must be specified if password is not specified
                propVal = appConfig.getString(AppConfig.PROP_OAUTH_ACCESSTOKEN);
            }
            if (propVal == null || propVal.isBlank()) {
                logger.fatal(Messages.getFormattedString("Config.errorNoRequiredParameter", propName));
                throw new ParameterLoadException(Messages.getFormattedString("Config.errorNoRequiredParameter", propName));
            }
        }
    }
    
    private void doLoginFromBrowser(AppConfig appConfig) throws OAuthBrowserLoginRunnerException {
        final String verificationURLStr;
        final OAuthBrowserLoginRunner loginRunner;
        try {
            loginRunner = new OAuthBrowserLoginRunner(appConfig, true);
            verificationURLStr = loginRunner.getVerificationURLStr();
            System.out.println(Labels.getString("OAuthInBrowser.batchModeMessage1"));
            System.out.println(Labels.getString("OAuthInBrowser.batchModeURL") + verificationURLStr);
            System.out.println(Labels.getFormattedString("OAuthInBrowser.batchModeMessage2", loginRunner.getUserCode()));
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new OAuthBrowserLoginRunnerException(ex.getMessage());
        }
        while (!loginRunner.isLoginProcessCompleted()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // fail silently
            }
        }
    }
}
