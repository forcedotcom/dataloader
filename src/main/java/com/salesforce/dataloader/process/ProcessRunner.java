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
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.exception.ProcessInitializationException;
import com.sforce.soap.partner.fault.ApiFault;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProcessRunner implements InitializingBean, Job, Runnable {

    /**
     * Comment for <code>PROCESS_NAME</code>
     */
    public static final String PROCESS_NAME = "process.name";

    //logger
    private static Logger logger;
    
    private String name; // name of the loaded process DynaBean

    // config override parameters
    private final Map<String, String> configOverrideMap = new HashMap<String, String>();

    private Controller controller;
    
    private static final String PROP_NAME_ARRAY[] = {
            Config.OPERATION,
            Config.ENDPOINT,
            Config.USERNAME,
            Config.PASSWORD,
            Config.DAO_TYPE,
            Config.DAO_NAME,
            Config.ENTITY,
    };
    
    static {

    }
    
    /**
     * Enforce use of factory method - getInstance() by hiding the constructor
     */
    protected ProcessRunner() {
    }

    @Override
    public synchronized void run() {
        run(NihilistProgressAdapter.get());
    }

    public synchronized void run(ILoaderProgress monitor) {
        final String oldName = Thread.currentThread().getName();
        final String name = getName();

        setThreadName(name);

        try {
            controller = Controller.getInstance(name, true, null);
        } catch (ControllerInitializationException e) {
            throw new RuntimeException(e);
        }

        try {
            logger.info(Messages.getString("Process.initializingEngine")); //$NON-NLS-1$
            Config config = controller.getConfig();
            // load parameter overrides (from command line or caller context)
            logger.info(Messages.getString("Process.loadingParameters")); //$NON-NLS-1$
            config.loadParameterOverrides(getConfigOverrideMap());
            
            // Make sure that the required properties are specified.
            validateConfigProperties(config);

            // create files for status output unless it's an extract and status output is disabled
            if (!config.getOperationInfo().isExtraction() || config.getBoolean(Config.ENABLE_EXTRACT_STATUS_OUTPUT)) {
                controller.setStatusFiles(config.getString(Config.OUTPUT_STATUS_DIR), true, false);
            }

            logger.info(Messages.getFormattedString("Process.loggingIn", config.getString(Config.ENDPOINT))); //$NON-NLS-1$
            if (controller.login()) {
                // instantiate the data access object
                controller.createDao();

                logger.info(Messages.getString("Process.checkingDao")); //$NON-NLS-1$
                // check to see if the the data access object has any connection problems
                controller.getDao().checkConnection();

                // get the field info (using the describe call)
                logger.info(Messages.getString("Process.settingFieldTypes")); //$NON-NLS-1$
                controller.setFieldTypes();

                // get the object reference info (using the describe call)
                logger.info(Messages.getString("Process.settingReferenceTypes")); //$NON-NLS-1$
                controller.setReferenceDescribes();

                // instantiate the map
                logger.info(Messages.getString("Process.creatingMap")); //$NON-NLS-1$
                controller.createMapper();

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

    private static boolean validateCmdLineArgs (String[] args) {

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("-help".equals(arg) ) {
                System.out.println(Messages.getString("Process.help1"));
                System.out.println(Messages.getString("Process.help2"));
                System.out.println(Messages.getString("Process.help3"));
                System.out.println(Messages.getString("Process.help4"));
                System.out.println(Messages.getString("Process.help5"));
                System.out.println(Messages.getString("Process.help6"));
                return false;
            }
        }
        return true;
    }

    private static void topLevelError(String message, Throwable err) {
        if (logger == null) {
            System.err.println(message);
        } else {
            logger.fatal(message, err);
        }
        System.exit(-1);
    }
    
    public static void runBatchMode(String[] args) {
        ProcessRunner runner = null;
        try {
            // create the process
            runner = ProcessRunner.getInstance(args);
            if (runner == null) topLevelError("Process runner is null", new NullPointerException());
        } catch (Throwable t) {
            topLevelError("Failed to create process", t);
        }
        try {
            // run the process
            runner.run();
        } catch (Throwable e) {
            topLevelError("Unable to run process " + runner.getName(), e);
        }
    }

    /**
     * Get an instance of the engine runner that can be scheduled in it's own thread
     *
     * @param args String set of name=value pairs of arguments for the runner
     * @throws ProcessInitializationException
     */
    private static ProcessRunner getInstance(String[] args) throws ProcessInitializationException {
        if(!validateCmdLineArgs(args)) {
            return null;
        }
        Map<String,String> argMap = Controller.getArgMapFromArgArray(args);
        return getInstance(argMap);
    }

    /**
     * @param argMap
     * @return instance of ProcessRunner
     * @throws ProcessInitializationException
     */
    public static synchronized ProcessRunner getInstance(Map<String, String> argMap) throws ProcessInitializationException {
        ProcessRunner runner;
        Controller.setConfigDir(argMap);
        try {
            Controller.initLog();
        } catch (ControllerInitializationException e) {
            System.err.println("ProcessRunner: log not configured" + e );
            throw new RuntimeException(e.getMessage());
        }
        logger = LogManager.getLogger(ProcessRunner.class);
        
            // get a controller instance to load the properties except for the
            // runtime properties stored in XXX_lastRun.properties file.
            Controller controller = Controller.getInstance("", true, null);
            logger.info(Messages.getString("Process.initializingEngine")); //$NON-NLS-1$
            Config config = controller.getConfig();
            // load parameter overrides (from command line or caller context)
            logger.info(Messages.getString("Process.loadingParameters")); //$NON-NLS-1$
            config.loadParameterOverrides(argMap);
            String processName = config.getString(PROCESS_NAME);
            logger.debug("process name is " + processName);
            if (processName == null || processName.isEmpty()) {
                logger.info(PROCESS_NAME + "is not set in the command line or config.properties file.");
                // operation and other process params are specified through properties
                validateConfigProperties(config);
                runner = new ProcessRunner();
                runner.setName(config.getString(Config.OPERATION));
                runner.setConfigOverrideMap(argMap);
            } else {
                // process DynaBean name specified.
                runner = ProcessConfig.getProcessInstance(processName);
                runner.getConfigOverrideMap().putAll(argMap);
            }
        
                
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

    /* (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        run();
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
            if (propVal == null || propVal.isEmpty()) {
                logger.fatal(Messages.getFormattedString("Config.errorNoRequiredParameter", propName));
                throw new ParameterLoadException(Messages.getFormattedString("Config.errorNoRequiredParameter", propName));
            }
        }
    }
}
