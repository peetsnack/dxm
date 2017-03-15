/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin;

import org.apache.commons.io.FileUtils;
import org.jahia.bin.errors.ErrorFileDumper;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.RequestLoadAverage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.StopWatch;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Test unit for error file dumper sub system.
 */
public class ErrorFileDumperTest {

    private final long LOOP_COUNT = 1000L;
    private final long THREAD_COUNT = 200;
    private Set<Thread> threadSet = new HashSet<Thread>();
    private static File todaysDirectory;

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(ErrorFileDumperTest.class);

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        Date now = new Date();
        todaysDirectory = new File(SettingsBean.getErrorDir(), ErrorFileDumper.DATE_FORMAT_DIRECTORY.format(now));
        logger.info("Error directory is " + todaysDirectory.getAbsolutePath());
        new SettingsBean(null, new Properties(), Collections.<String>emptyList());
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        FileUtils.deleteDirectory(todaysDirectory);
        RequestLoadAverage.getInstance().stop();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(todaysDirectory);
    }

    @Test
    public void testDumperActivation() throws InterruptedException {

        logger.info("Starting testDumperInSequence test...");

        ErrorFileDumper.start();

        logger.info("Activating Error file dumping...");
        ErrorFileDumper.setFileDumpActivated(true);

        File[] files = todaysDirectory.listFiles();
        int fileCountBeforeTest = (files == null ? 0 : files.length);

        generateExceptions();

        Thread.sleep(5000);

        files = todaysDirectory.listFiles();
        int fileCountAfterTest = (files == null ? 0 : files.length);
        Assert.assertTrue("File count after test should be higher but it is not", (fileCountAfterTest > fileCountBeforeTest));

        logger.info("De-activating Error file dumping...");

        ErrorFileDumper.setFileDumpActivated(false);

        files = todaysDirectory.listFiles();
        fileCountBeforeTest = (files == null ? 0 : files.length);

        generateExceptions();

        Thread.sleep(5000);

        files = todaysDirectory.listFiles();
        fileCountAfterTest = (files == null ? 0 : files.length);

        Assert.assertEquals("File count after test should be the same as before", fileCountBeforeTest, fileCountAfterTest);

        ErrorFileDumper.shutdown(10000L);

    }

    @Test
    public void testHighLoadDeactivation() throws InterruptedException {

        logger.info("Starting testHighLoadDeactivation test...");

        RequestLoadAverage.RequestCountProvider requestCountProvider = new RequestLoadAverage.RequestCountProvider() {
            public long getRequestCount() {
                return 100;
            }
        };

        RequestLoadAverage requestLoadAverage = new RequestLoadAverage("requestLoadAverage", requestCountProvider);
        requestLoadAverage.start();
        logger.info("Waiting for load average to reach 10...");
        while (requestLoadAverage.getOneMinuteLoad() < 10.0) {
            Thread.sleep(500);
        }

        StopWatch stopWatch = new StopWatch("testHighLoadDeactivation");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");

        int fileCountBeforeTest = 0;
        if (todaysDirectory.exists()) {
            File[] files = todaysDirectory.listFiles();
            fileCountBeforeTest = (files == null ? 0 : files.length);
        }

        ErrorFileDumper.setHighLoadBoundary(10.0);
        ErrorFileDumper.start();

        generateExceptions();

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = ((double) totalTime) / ((double) LOOP_COUNT);
        logger.info("Milliseconds per exception = " + averageTime);
        logger.info(stopWatch.prettyPrint());

        ErrorFileDumper.shutdown(10000L);

        RequestLoadAverage.getInstance().stop();

        int fileCountAfterTest = 0;
        if (todaysDirectory.exists()) {
            File[] files = todaysDirectory.listFiles();
            fileCountAfterTest = (files == null ? 0 : files.length);
        }

        Assert.assertEquals("File count should stay the same because high load deactivates file dumping !", fileCountBeforeTest, fileCountAfterTest);

        requestLoadAverage = new RequestLoadAverage("requestLoadAverage");
    }

    @Test
    public void testDumperInSequence() throws InterruptedException {

        logger.info("Starting testDumperInSequence test...");

        StopWatch stopWatch = new StopWatch("testDumperInSequence");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");

        ErrorFileDumper.start();

        generateExceptions();

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = ((double) totalTime) / ((double) LOOP_COUNT);
        logger.info("Milliseconds per exception = " + averageTime);
        logger.info(stopWatch.prettyPrint());

        ErrorFileDumper.shutdown(10000L);

        Assert.assertTrue("Error dump directory does not exist !", todaysDirectory.exists());
        Assert.assertTrue("Error dump directory should have error files in it !", todaysDirectory.listFiles().length > 0);
    }

    @Test
    public void testDumpErrorsToFilesSetting() throws InterruptedException {
        SettingsBean.getInstance().setDumpErrorsToFiles(false);
        logger.info("Starting testDumpErrorsToFilesSetting test...");

        StopWatch stopWatch = new StopWatch("testDumpErrorsToFilesSetting");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");

        ErrorFileDumper.start();

        generateExceptions();

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = ((double) totalTime) / ((double) LOOP_COUNT);
        logger.info("Milliseconds per exception = " + averageTime);
        logger.info(stopWatch.prettyPrint());

        ErrorFileDumper.shutdown(10000L);

        SettingsBean.getInstance().setDumpErrorsToFiles(true);
        Assert.assertFalse("Error dump directory should not exist !", todaysDirectory.exists());
    }

    @Test
    public void testDumperInParallel() throws IOException, InterruptedException {

        logger.info("Starting testDumperInParallel test...");

        StopWatch stopWatch = new StopWatch("testDumperInParallel");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");

        ErrorFileDumper.start();

        threadSet.clear();

        for (int i=0; i < THREAD_COUNT; i++) {
            Thread newThread = new Thread(new Runnable() {

                public void run() {
                    generateExceptions();
                }
            }, "ErrorFileDumperTestThread" + i);
            threadSet.add(newThread);
            newThread.start();
        }

        logger.info("Waiting for dumps to be processed...");

        for (Thread curThread : threadSet) {
            curThread.join();
        }

        ErrorFileDumper.shutdown(10000L);

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = ((double) totalTime) / ((double) LOOP_COUNT);
        logger.info("Milliseconds per exception = " + averageTime);
        logger.info(stopWatch.prettyPrint());

        Assert.assertTrue("Error dump directory does not exist !", todaysDirectory.exists());
        Assert.assertTrue("Error dump directory should have error files in it !", todaysDirectory.listFiles().length > 0);
    }

    @Test
    public void testOutputSystemInfoAllInParallel() throws InterruptedException {

        logger.info("Starting testOutputSystemInfoAllInParallel test...");

        StopWatch stopWatch = new StopWatch("testDumperInParallel");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");

        ErrorFileDumper.start();

        threadSet.clear();
        final int[] dumpLengths = new int[(int)THREAD_COUNT];

        for (int i=0; i < THREAD_COUNT; i++) {
            final int threadCounter = i;
            Thread newThread = new Thread(new Runnable() {

                public void run() {
                    // this is the call made in errors.jsp file.
                    StringWriter stringWriter = new StringWriter();
                    ErrorFileDumper.outputSystemInfo(new PrintWriter(stringWriter));
                    dumpLengths[threadCounter] = stringWriter.toString().length();
                    stringWriter = null;
                }
            }, "ErrorFileDumperTestThread" + i);
            threadSet.add(newThread);
            newThread.start();
        }

        logger.info("Waiting for dumps to be processed...");

        for (Thread curThread : threadSet) {
            curThread.join();
        }

        ErrorFileDumper.shutdown(10000L);

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = ((double) totalTime) / ((double) LOOP_COUNT);
        logger.info("Milliseconds per exception = " + averageTime);
        logger.info(stopWatch.prettyPrint());
        for (int dumpLength : dumpLengths) {
            Assert.assertTrue("System info dump is empty", dumpLength > 0);
        }
    }

    private void generateExceptions() {
        for (int i=0; i < LOOP_COUNT; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/cms");
            request.setQueryString("name=value");
            request.addHeader("headerName", "headerValue");
            try {
                ErrorFileDumper.dumpToFile(new Throwable("mock error " + i), (HttpServletRequest) request);
            } catch (IOException e) {
                logger.error("Error while dumping error", e);
            }
        }
    }
}
