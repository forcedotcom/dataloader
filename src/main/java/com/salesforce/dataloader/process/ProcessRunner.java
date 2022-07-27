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
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.LastRun;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.ControllerInitializationException;
import com.salesforce.dataloader.exception.OAuthBrowserLoginRunnerException;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.exception.ProcessInitializationException;
import com.salesforce.dataloader.ui.Labels;
import com.salesforce.dataloader.util.AppUtil;
import com.salesforce.dataloader.util.OAuthBrowserLoginRunner;
import com.sforce.soap.partner.fault.ApiFault;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.beans.factory.InitializingBean;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProcessRunner implements InitializingBean {

    /**
     * Comment for <code>DYNABEAN_ID</code>
     */
    public static final String DYNABEAN_ID = "process.name";
    public static final String PROCESS_THREAD_NAME = "process.thread.name";

    //logger
    private static Logger logger;
    
    private String name = null; // name of the loaded process DynaBean

    // config override parameters
    private final Map<String, String> configOverrideMap = new HashMap<String, String>();

    private Controller controller;
    
    private ILoaderProgress monitor;
    
    private static final String PROP_NAME_ARRAY[] = {
            Config.OPERATION,
            Config.ENDPOINT,
            Config.USERNAME,
            Config.PASSWORD,
            Config.DAO_TYPE,
            Config.DAO_NAME,
            Config.ENTITY,
    };
    
    /**
     * Enforce use of factory method - getInstance() by hiding the constructor
     */
    protected ProcessRunner() {
    }

    public synchronized void run(ILoaderProgress monitor) {
        if (monitor == null) {
            monitor = NihilistProgressAdapter.get();
        }
        this.monitor = monitor;
        final String oldName = Thread.currentThread().getName();
        String name = getName();

        if (name != null && !name.isBlank()) {
            setThreadName(name);
        }

        try {
            controller = Controller.getInstance(name, getConfigOverrideMap());
        } catch (ControllerInitializationException e) {
            throw new RuntimeException(e);
        }

        try {
            logger.info(Messages.getString("Process.initializingEngine")); //$NON-NLS-1$
            Config config = controller.getConfig();
            if (!(config.contains(Config.USERNAME) && config.contains(Config.PASSWORD))
                    && config.getBoolean(Config.OAUTH_LOGIN_FROM_BROWSER)) {
                doLoginFromBrowser(config);
            }
            // Make sure that the required properties are specified.
            validateConfigProperties(config);
            if (name == null || name.isBlank()) {
                // this can occur only if "process.name" is not specified as a  command line option
                name = config.getString(Config.OPERATION);
                this.setName(name);
                setThreadName(name);
            };

            // create files for status output unless it's an extract and status output is disabled
            if (!config.getOperationInfo().isExtraction() || config.getBoolean(Config.ENABLE_EXTRACT_STATUS_OUTPUT)) {
                controller.setStatusFiles(config.getString(Config.OUTPUT_STATUS_DIR), true, false);
            }

            logger.info(Messages.getFormattedString("Process.loggingIn", config.getString(Config.ENDPOINT))); //$NON-NLS-1$
            if (controller.login()) {
                // get the field info (using the describe call)
                logger.info(Messages.getString("Process.settingFieldTypes")); //$NON-NLS-1$
                controller.setFieldTypes();

                // get the object reference info (using the describe call)
                logger.info(Messages.getString("Process.settingReferenceTypes")); //$NON-NLS-1$
                controller.setReferenceDescribes();

                // instantiate the map
                logger.info(Messages.getString("Process.creatingMap")); //$NON-NLS-1$
                controller.createMapper(config.getString(Config.DAO_TYPE), 
                        config.getString(Config.DAO_NAME), config.getString(Config.ENTITY));

                // execute the requested operation
                controller.executeAction(monitor);

                // save last successful run date
                // FIXME look into a better place so that long runs don't skew this
                config.setValue(LastRun.LAST_RUN_DATE, Calendar.getInstance().getTime());
                config.saveLastRun();
            } else {
                logger.fatal(Messages.getString("Process.loginError")); //$NON-NLS-1$
            }
        } catch (ApiFault e) {
            // this is necessary, because the ConnectionException doesn't display the login fault message
            throw new RuntimeException(e.getExceptionMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        if (this.configOverrideMap.isEmpty())
            this.configOverrideMap.putAll(configOverrideMap);
        else
            throw new IllegalStateException("Attempting to set configOverrideMap but there are already "
                    + this.configOverrideMap.size() + " entries");
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

    private static void logErrorAndExitProcess(String message, Throwable err) {
        if (logger == null) {
            System.err.println(message);
        } else {
            logger.fatal(message, err);
        }
        System.exit(-1);
    }
    
    public static void runBatchMode(String[] args) {
        Map<String,String> argMap = Controller.getArgMapFromArgArray(args);
        try {
            runBatchMode(argMap, null);
        } catch (Throwable t) {
            logErrorAndExitProcess("Unable to run process", t);
        }
    }
    
    public static ProcessRunner runBatchMode(Map<String, String> argMap, ILoaderProgress monitor) {
        ProcessRunner runner = null;
        try {
            // create the process
            runner = ProcessRunner.getInstance(argMap);
            if (runner == null) logErrorAndExitProcess("Process runner is null", new NullPointerException());
        } catch (Throwable t) {
            logErrorAndExitProcess("Failed to create process", t);
        }
        // run the process
        runner.run(monitor);
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
        logger = LogManager.getLogger(ProcessRunner.class);
        logger.info(Messages.getString("Process.initializingEngine")); //$NON-NLS-1$
        String dynaBeanID = argMap.get(DYNABEAN_ID);
        ProcessRunner runner;
        if (dynaBeanID == null || dynaBeanID.isEmpty()) {
            // operation and other process params are specified through config.properties
            logger.info(DYNABEAN_ID 
                    + "is not specified in the command line. Loading the process properties from config.properties.");
            runner = new ProcessRunner();
            
            if (argMap.containsKey(PROCESS_THREAD_NAME)) {
                runner.setName(argMap.get(PROCESS_THREAD_NAME));
                argMap.remove(PROCESS_THREAD_NAME); // avoid this option from being considered as a property
            }
        } else {
            // process name specified in the command line arg. 
            // Load its DynaBean through process-conf.xml
            logger.info(DYNABEAN_ID 
                        + "is specified in the command line. Loading DynaBean with id " 
                        + dynaBeanID 
                        + " from process-conf.xml located in directory "
                        + AppUtil.getConfigurationsDir());
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
    
    private static void validateConfigProperties(Config config) throws ProcessInitializationException {
        if (config == null) {
            throw new ProcessInitializationException("Configuration not initialized");
        }

        for (String propName : PROP_NAME_ARRAY) {
            String propVal = config.getString(propName);
            if (propName.equals(Config.PASSWORD) && (propVal == null || propVal.isBlank())) {
                // OAuth access token must be specified if password is not specified
                propVal = config.getString(Config.OAUTH_ACCESSTOKEN);
            }
            if (propVal == null || propVal.isBlank()) {
                logger.fatal(Messages.getFormattedString("Config.errorNoRequiredParameter", propName));
                throw new ParameterLoadException(Messages.getFormattedString("Config.errorNoRequiredParameter", propName));
            }
        }
    }
    
    private void doLoginFromBrowser(Config config) throws OAuthBrowserLoginRunnerException {
        final String verificationURLStr;
        final OAuthBrowserLoginRunner loginRunner;
        try {
            loginRunner = new OAuthBrowserLoginRunner(config, true);
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
