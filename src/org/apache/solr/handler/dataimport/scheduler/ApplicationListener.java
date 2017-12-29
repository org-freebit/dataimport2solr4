package org.apache.solr.handler.dataimport.scheduler;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author shiyanwu
 */
public class ApplicationListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationListener.class);

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        //获取ServletContext
        ServletContext servletContext = servletContextEvent.getServletContext();

        // get our timer from the context
        ScheduledExecutorService deltaScheduledThreadPool = (ScheduledExecutorService) servletContext.getAttribute("deltaScheduledThreadPool");
        ScheduledExecutorService fullScheduledThreadPool = (ScheduledExecutorService) servletContext.getAttribute("fullScheduledThreadPool");

        // cancel all active tasks in the timers queue
        if (deltaScheduledThreadPool != null) {
            deltaScheduledThreadPool.shutdown();
        }
        if (fullScheduledThreadPool != null) {
            fullScheduledThreadPool.shutdown();
        }

        // remove the timer from the context
        servletContext.removeAttribute("deltaScheduledThreadPool");
        servletContext.removeAttribute("fullScheduledThreadPool");

    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        /**
         * 获取ServletContext
         */
        ServletContext servletContext = servletContextEvent.getServletContext();
        try {
            // 增量更新任务计划
            ScheduledExecutorService deltaScheduledThreadPool = new ScheduledThreadPoolExecutor(1, new BasicThreadFactory.Builder().namingPattern("deltaImport-schedule-pool-%d").daemon(true).build());

            DeltaImportHTTPPostScheduler deltaImport = new DeltaImportHTTPPostScheduler(
                    servletContext.getServletContextName());

            // get our interval from HTTPPostScheduler
            int interval = deltaImport.getIntervalInt();

            // schedule the task
            deltaScheduledThreadPool.scheduleAtFixedRate(deltaImport, 1L * interval, 1000L * interval, TimeUnit.SECONDS);

            // save the timer in context
            servletContext.setAttribute("deltaScheduledThreadPool", deltaScheduledThreadPool);

            // 重做索引任务计划
            ScheduledExecutorService fullScheduledThreadPool = new ScheduledThreadPoolExecutor(1, new BasicThreadFactory.Builder().namingPattern("fullImport-schedule-pool-%d").daemon(true).build());
            FullImportHTTPPostScheduler fullImport = new FullImportHTTPPostScheduler(
                    servletContext.getServletContextName());

            int reBuildIndexInterval = fullImport.getReBuildIndexIntervalInt();
            if (reBuildIndexInterval <= 0) {
                logger.warn("Full Import Schedule disabled");
                return;
            }

            Calendar fullImportCalendar = Calendar.getInstance();
            Date beginDate = fullImport.getReBuildIndexBeginTime();
            fullImportCalendar.setTime(beginDate);
            fullImportCalendar.add(Calendar.MINUTE, reBuildIndexInterval);
            Date fullImportStartTime = fullImportCalendar.getTime();

            // schedule the task
            fullScheduledThreadPool.scheduleAtFixedRate(fullImport,
                    1L * reBuildIndexInterval, 1000L * reBuildIndexInterval, TimeUnit.SECONDS);

            // save the timer in context
            servletContext.setAttribute("fullScheduledThreadPool", fullScheduledThreadPool);

        } catch (Exception e) {
            if (e.getMessage().endsWith("disabled")) {
                logger.warn("Schedule disabled");
            } else {
                logger.error("Problem initializing the scheduled task: ", e);
            }
        }

    }

}