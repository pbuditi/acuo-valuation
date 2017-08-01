package com.acuo.valuation.quartz;

import com.google.common.util.concurrent.AbstractService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;

import javax.inject.Inject;
import javax.inject.Named;

import static com.acuo.valuation.utils.PropertiesHelper.ACUO_SCHEDULER_ENABLED;
import static com.acuo.valuation.utils.PropertiesHelper.ACUO_SIMULATION_ENABLED;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Slf4j
public class SchedulerService extends AbstractService {

    private final Scheduler scheduler;
    private final boolean enabled;

    @Inject
    public SchedulerService(JobFactory jobFactory,
                            @Named(ACUO_SCHEDULER_ENABLED) boolean enabled) throws SchedulerException {
        scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.setJobFactory(jobFactory);
        this.enabled = enabled;
    }

    @Override
    protected void doStart() {
        if (enabled) {
            try {
                JobDetail jobDetail = JobBuilder
                        .newJob(GenerateCallJob.class)
                        .withIdentity("GenerateCallJob", "markitgroup")
                        .build();
                Trigger trigger = TriggerBuilder
                        .newTrigger()
                        .withIdentity("GenerateCallJob", "markitgroup")
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

                JobDetail fxScheduledJob = JobBuilder.newJob(FXScheduledValueJob.class)
                        .withIdentity("FXScheduledValueJob", "datascoupegroup")
                        .build();

                JobDetail fxIntradayJob = JobBuilder.newJob(FXRatesIntradayJob.class)
                        .withIdentity("FXRatesIntradayJob", "datascoupegroup")
                        .build();

                Trigger fxTrigger = TriggerBuilder
                        .newTrigger()
                        .withIdentity("FXScheduledValueJob", "datascoupegroup")
                        .withSchedule(cronSchedule("0 0 * * * ?").withMisfireHandlingInstructionFireAndProceed())
                        .build();

                Trigger once = TriggerBuilder
                        .newTrigger()
                        .withIdentity("FXValueJobOnce", "datascoupegroup")
                        .startNow()
                        .withSchedule(simpleSchedule().withRepeatCount(0))
                        .build();

                JobDetail settlementDateJob = JobBuilder
                        .newJob(SettlementDateJob.class)
                        .withIdentity("SettlementDateJob", "reutersgroup")
                        .build();

                Trigger settlementDateTrigger = TriggerBuilder
                        .newTrigger()
                        .withIdentity("SettlementDateJob", "reutersgroup")
                        .withSchedule(cronSchedule("0 0/5 * * * ?"))
                        .build();

                scheduler.scheduleJob(jobDetail, trigger);
                scheduler.scheduleJob(assetjob, assetTrigger);
                scheduler.scheduleJob(fxIntradayJob, fxTrigger);
                scheduler.scheduleJob(fxScheduledJob, once);
                scheduler.scheduleJob(settlementDateJob,settlementDateTrigger);

                scheduler.start();
                notifyStarted();
            } catch (Exception e) {
                log.error("error in Scheduler:" + e.toString());
                notifyFailed(e);
            }
        }
    }

    @Override
    protected void doStop() {
        if (enabled && scheduler != null) {
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