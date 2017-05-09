package com.acuo.valuation.quartz;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.AbstractService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;

import javax.inject.Inject;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Slf4j
public class SchedulerService extends AbstractService {

    private final Scheduler scheduler;

    @Inject
    public SchedulerService(JobFactory jobFactory) throws SchedulerException {
        scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.setJobFactory(jobFactory);
    }

    @Override
    protected void doStart() {
        try {
            JobDetail jobDetail = JobBuilder
                    .newJob(TradePriceJob.class)
                    .withIdentity("TradePriceJob", "markitgroup")
                    .build();
            Trigger trigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity("TradePriceJob", "markitgroup")
                    .withSchedule(cronSchedule("0 0 1 * * ?"))
                    .build();

            JobDetail assetjob = JobBuilder
                    .newJob(AssetPriceJob.class)
                    .withIdentity("AssetPriceJob", "reutersgroup")
                    .build();

            Trigger assetTrigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity("AssetPriceJob", "reutersgroup")
                    .withSchedule(cronSchedule("0 0/5 * * * ?"))
                    .build();

            JobDetail fxjob = JobBuilder.newJob(FXValueJob.class)
                    .withIdentity("FXValueJob", "datascoupegroup")
                    .build();

            Trigger fxTrigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity("FXValueJob", "datascoupegroup")
                    .withSchedule(cronSchedule("0 0 3 * * ?").withMisfireHandlingInstructionFireAndProceed())
                    .build();

            Trigger once = TriggerBuilder
                    .newTrigger()
                    .withIdentity("FXValueJobOnce", "datascoupegroup")
                    .startNow()
                    .withSchedule(simpleSchedule().withRepeatCount(0))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.scheduleJob(assetjob, assetTrigger);
            scheduler.scheduleJob(fxjob, ImmutableSet.of(fxTrigger, once), false);

            scheduler.start();
            notifyStarted();
        } catch (Exception e) {
            log.error("error in Scheduler:" + e.toString());
            notifyFailed(e);
        }
    }

    @Override
    protected void doStop() {
        if (scheduler != null) {
            try {
                scheduler.shutdown();
                notifyStopped();
                log.info("stop scheduler successfully!");
            } catch (Exception e) {
                log.error("stop scheduler failed ", e);
                notifyFailed(e);
            }
        }
    }
}