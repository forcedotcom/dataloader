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

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.action.progress.NihilistProgressAdapter;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.LastRunProperties;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.ConfigInitializationException;
import com.salesforce.dataloader.exception.ControllerInitializationException;
import com.salesforce.dataloader.exception.OAuthBrowserLoginRunnerException;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.exception.ProcessInitializationException;
import com.salesforce.dataloader.ui.Labels;
import com.salesforce.dataloader.util.AppUtil;
import com.salesforce.dataloader.util.ExitException;
import com.salesforce.dataloader.util.OAuthBrowserDeviceLoginRunner;
import com.salesforce.dataloader.util.OAuthBrowserFlow;
import com.sforce.soap.partner.fault.ApiFault;
import com.salesforce.dataloader.util.DLLogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProcessRunner implements InitializingBean, IProcess {

    private static final Logger logger = DLLogManager.getLogger(ProcessRunner.class);
    private static final String[] PROP_NAME_ARRAY = {
            AppConfig.PROP_OPERATION,
            AppConfig.PROP_USERNAME,
            AppConfig.PROP_PASSWORD,
            AppConfig.PROP_DAO_TYPE,
            AppConfig.PROP_DAO_NAME,
            AppConfig.PROP_ENTITY,
    };

    private String name;
    private final Map<String, String> configOverrideMap = new HashMap<>();
    private Controller controller;
    private ILoaderProgress monitor;

    protected ProcessRunner() {
    }

    public synchronized void run(ILoaderProgress monitor) throws Exception {
        this.monitor = (monitor == null) ? new NihilistProgressAdapter() : monitor;
        final String oldName = Thread.currentThread().getName();
        setThreadName(getName());

        try {
            initializeController();
            AppConfig appConfig = controller.getAppConfig();
            handleOAuthLogin(appConfig);
            validateConfigProperties(appConfig);
            setThreadNameIfNeeded(appConfig);
            createStatusFilesIfNeeded(appConfig);
            executeProcess(appConfig);
        } catch (ApiFault e) {
            throw new RuntimeException(e.getExceptionMessage(), e);
        } finally {
            closeResources();
            setThreadName(oldName);
        }
    }

    private void initializeController() throws ControllerInitializationException, ParameterLoadException, ConfigInitializationException {
        controller = Controller.getInstance(getConfigOverrideMap());
    }

    private void handleOAuthLogin(AppConfig appConfig) throws OAuthBrowserLoginRunnerException {
        if (requiresOAuthLogin(appConfig)) {
        	if (appConfig.getBoolean(AppConfig.PROP_OAUTH_LOGIN_FROM_BROWSER_DEVICE_OAUTH)) {
        		doDeviceLoginFromBrowser(appConfig);
			} else {
	            doBrowserLogin(appConfig);
			}
        }
    }

    private boolean requiresOAuthLogin(AppConfig appConfig) {
        return !(appConfig.contains(AppConfig.PROP_USERNAME) && appConfig.contains(AppConfig.PROP_PASSWORD))
                && appConfig.getBoolean(AppConfig.PROP_OAUTH_LOGIN_FROM_BROWSER);
    }

    private void setThreadNameIfNeeded(AppConfig appConfig) {
        if (name == null || name.isBlank()) {
            name = appConfig.getString(AppConfig.PROP_OPERATION);
            setName(name);
            setThreadName(name);
        }
    }

    private void createStatusFilesIfNeeded(AppConfig appConfig) throws ProcessInitializationException {
        if (!appConfig.getOperationInfo().isExtraction() || appConfig.getBoolean(AppConfig.PROP_ENABLE_EXTRACT_STATUS_OUTPUT)) {
            controller.setStatusFiles(appConfig.getString(AppConfig.PROP_OUTPUT_STATUS_DIR), true, false);
        }
    }

    private void executeProcess(AppConfig appConfig) throws Exception {
        if (controller.login()) {
            controller.setFieldTypes();
            controller.initializeOperation(appConfig.getString(AppConfig.PROP_DAO_TYPE),
                    appConfig.getString(AppConfig.PROP_DAO_NAME), appConfig.getString(AppConfig.PROP_ENTITY));
            controller.executeAction(monitor);
            saveLastRunDate(appConfig);
        } else {
            logger.fatal(Messages.getString("Process.loginError"));
        }
    }

    private void saveLastRunDate(AppConfig appConfig) throws IOException {
        appConfig.setValue(LastRunProperties.LAST_RUN_DATE, Calendar.getInstance().getTime());
        appConfig.saveLastRun();
    }

    private void closeResources() {
        if (controller.getDao() != null) {
            controller.getDao().close();
        }
    }

    public ILoaderProgress getMonitor() {
        return this.monitor;
    }

    private void setThreadName(final String name) {
        if (name != null && !name.isBlank()) {
            try {
                Thread.currentThread().setName(name);
            } catch (Exception e) {
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

    @Override
    public void afterPropertiesSet() throws Exception {
        if (name == null || name.isBlank()) {
            logger.fatal(Messages.getFormattedString("Process.missingRequiredArg", "name"));
            throw new ParameterLoadException(Messages.getFormattedString("Process.missingRequiredArg", "name"));
        }
    }

    public static void logErrorAndExitProcess(String message, Throwable throwable, int exitCode) {
        if (throwable == null) {
            logger.fatal(message);
        } else {
            logger.fatal(message, throwable);
        }
        throw new ExitException(throwable, exitCode);
    }

    public static ProcessRunner runBatchMode(Map<String, String> commandLineOptionsMap, ILoaderProgress progressMonitor) throws UnsupportedOperationException {
        ProcessRunner runner = null;
        String errorMessage = "";
        int exitCode = AppUtil.EXIT_CODE_NO_ERRORS;
        Throwable throwable = null;
        try {
            runner = getInstance(commandLineOptionsMap);
            if (runner == null) {
                logErrorAndExitProcess("Process runner is null", new NullPointerException(), AppUtil.EXIT_CODE_CLIENT_ERROR);
            }
            runner.run(progressMonitor);
            progressMonitor = runner.getMonitor();
            handleProgressMonitor(progressMonitor);
        } catch (Throwable t) {
            handleThrowable(t);
        } finally {
            if (exitCode != AppUtil.EXIT_CODE_NO_ERRORS) {
                logErrorAndExitProcess(errorMessage, throwable, exitCode);
            }
        }
        return runner;
    }

    private static void handleProgressMonitor(ILoaderProgress progressMonitor) {
        if (progressMonitor != null) {
            if (progressMonitor.isCanceled()) {
                logErrorAndExitProcess(progressMonitor.getMessage(), null, AppUtil.EXIT_CODE_CLIENT_ERROR);
            } else if (!progressMonitor.isSuccess()) {
                logErrorAndExitProcess(progressMonitor.getMessage(), null, AppUtil.EXIT_CODE_SERVER_ERROR);
            } else if (AppConfig.getCurrentConfig() != null
                    && AppConfig.getCurrentConfig().getBoolean(AppConfig.PROP_PROCESS_EXIT_WITH_ERROR_ON_FAILED_ROWS_BATCH_MODE)
                    && progressMonitor.getNumberRowsWithError() > 0) {
                logErrorAndExitProcess(Messages.getFormattedString("Process.operationSuccessWithErrorRows", progressMonitor.getNumberRowsWithError()), null, AppUtil.EXIT_CODE_RESULTS_ERROR);
            }
        }
    }

    private static void handleThrowable(Throwable t) throws UnsupportedOperationException, ExitException {
        if (t instanceof UnsupportedOperationException) {
            throw (UnsupportedOperationException) t;
        }
        if (t instanceof ExitException) {
            throw (ExitException) t;
        }
        logErrorAndExitProcess("Unable to run process", t, AppUtil.EXIT_CODE_OPERATION_ERROR);
    }

    private static synchronized ProcessRunner getInstance(Map<String, String> commandLineOptionsMap) throws ProcessInitializationException {
        logger.info(Messages.getString("Process.initializingEngine"));
        String dynaBeanID = commandLineOptionsMap.get(AppConfig.PROP_PROCESS_NAME);
        ProcessRunner runner;
        if (dynaBeanID == null || dynaBeanID.isEmpty()) {
            logger.info(AppConfig.PROP_PROCESS_NAME + " is not specified in the command line. Loading the process properties from config.properties.");
            runner = new ProcessRunner();
            if (commandLineOptionsMap.containsKey(AppConfig.PROP_PROCESS_THREAD_NAME)) {
                runner.setName(commandLineOptionsMap.get(AppConfig.PROP_PROCESS_THREAD_NAME));
            }
        } else {
            logger.info(AppConfig.PROP_PROCESS_NAME + " is specified in the command line. Loading DynaBean with id " + dynaBeanID + " from process-conf.xml located in folder " + AppConfig.getConfigurationsDir());
            runner = ProcessConfig.getProcessInstance(dynaBeanID);
        }
        runner.getConfigOverrideMap().putAll(commandLineOptionsMap);
        return runner;
    }

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
                propVal = appConfig.getString(AppConfig.PROP_OAUTH_ACCESSTOKEN);
            }
            if (propVal == null || propVal.isBlank()) {
                logger.fatal(Messages.getFormattedString(AppConfig.class.getSimpleName() + ".errorNoRequiredParameter", propName));
                throw new ParameterLoadException(Messages.getFormattedString(AppConfig.class.getSimpleName() + ".errorNoRequiredParameter", propName));
            }
        }
    }

    void doDeviceLoginFromBrowser(AppConfig appConfig) throws OAuthBrowserLoginRunnerException {
        try {
            OAuthBrowserDeviceLoginRunner loginRunner = new OAuthBrowserDeviceLoginRunner(appConfig, true);
            String verificationURLStr = loginRunner.getVerificationURLStr();
            System.out.println(Labels.getString("OAuthInBrowser.batchModeMessage1"));
            System.out.println(Labels.getString("OAuthInBrowser.batchModeURL") + verificationURLStr);
            System.out.println(Labels.getFormattedString("OAuthInBrowser.batchModeMessage2", loginRunner.getUserCode()));
            waitForLoginCompletion(loginRunner);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new OAuthBrowserLoginRunnerException(ex.getMessage());
        }
    }

    void doBrowserLogin(AppConfig appConfig) throws OAuthBrowserLoginRunnerException {
        try {
            logger.debug("Starting OAuth browser login...");
            logger.debug("A browser window will open for you to log in to Salesforce.");
            logger.debug("Please complete your login in the browser window.");
            
            OAuthBrowserFlow oauthFlow = new OAuthBrowserFlow(appConfig);
            boolean success = oauthFlow.performOAuthFlow();
            
            if (success) {
            	logger.debug("OAuth browser login completed successfully!");
            } else {
                throw new OAuthBrowserLoginRunnerException("OAuth browser login failed - authentication could not be completed");
            }
        } catch (Exception ex) {
            logger.error("OAuth browser login failed: " + ex.getMessage());
            throw new OAuthBrowserLoginRunnerException("OAuth browser login failed: " + ex.getMessage());
        }
    }

    private void waitForLoginCompletion(OAuthBrowserDeviceLoginRunner loginRunner) {
        while (!loginRunner.isLoginProcessCompleted()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // fail silently
            }
        }
    }
}
