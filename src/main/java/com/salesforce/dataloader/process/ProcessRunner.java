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

import java.util.*;
import java.util.Calendar;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.Logger;
import org.quartz.*;
import org.springframework.beans.factory.InitializingBean;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.action.progress.NihilistProgressAdapter;
import com.salesforce.dataloader.config.*;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.*;
import com.sforce.soap.partner.fault.ApiFault;

public class ProcessRunner implements InitializingBean, Job, Runnable {

    /**
     * Comment for <code>PROCESS_NAME</code>
     */
    public static final String PROCESS_NAME = "process.name";

    //logger
    private static final Logger logger = Logger.getLogger(ProcessRunner.class);

    // Name of the current engine runner.  Improves readability of the log output
    String name;

    // config override parameters
    private final Map<String, String> configOverrideMap = new HashMap<String, String>();

    private Controller controller;

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
            controller = Controller.getInstance(name, true);
        } catch (ControllerInitializationException e) {
            throw new RuntimeException(e);
        }

        try {
            logger.info(Messages.getString("Process.initializingEngine")); //$NON-NLS-1$
            Config config = controller.getConfig();
            // load parameter overrides (from command line or caller context)
            logger.info(Messages.getString("Process.loadingParameters")); //$NON-NLS-1$
            config.loadParameterOverrides(getConfigOverrideMap());

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

                Date currentTime = Calendar.getInstance().getTime();

                // execute the requested operation
                controller.executeAction(monitor);

                // save last successful run date
                // FIXME look into a better place so that long runs don't skew this
                config.setValue(LastRun.LAST_RUN_DATE, currentTime);
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

    private static void ensureLogging() throws FactoryConfigurationError {
        Controller.initLog();
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
        ensureLogging();
        logger.fatal(message, err);
        System.exit(-1);
    }

    public static void main(String[] args) {
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
        ensureLogging();

        if(!validateCmdLineArgs(args)) {
            return null;
        }

        Map<String,String> argMap = getArgMap(args);
        ProcessRunner runner = getInstance(argMap);
        return runner;
    }

    /**
     * @param argMap
     * @return instance of ProcessRunner
     * @throws ProcessInitializationException
     */
    public static ProcessRunner getInstance(Map<String, String> argMap) throws ProcessInitializationException {
        ProcessRunner runner;
        if(argMap != null && argMap.containsKey(PROCESS_NAME)) {
            // if process name is specified, get it from configuration
            String processName = argMap.get(PROCESS_NAME);
            runner = ProcessConfig.getProcessInstance(processName);
            // command line param values override the ones in config file
            runner.getConfigOverrideMap().putAll(argMap);
        } else {
            // if process.name is not given, load defaults & override with command-line args
            runner = new ProcessRunner();
            runner.setConfigOverrideMap(argMap);
        }
        return runner;
    }

    private static Map<String, String> getArgMap(String[] args) {
        //every arg is a name=value config setting, save it in a map of name/value pairs
        Map<String,String> argMap = new HashMap<String,String>();
        for (int i = 0; i < args.length; i++) {
            String[] argArray = args[i].split("="); //$NON-NLS-1$

            if (argArray.length == 2) {
                argMap.put(argArray[0], argArray[1]);
            }
        }
        return argMap;
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
}
