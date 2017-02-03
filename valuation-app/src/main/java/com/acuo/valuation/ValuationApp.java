package com.acuo.valuation;

import com.acuo.common.app.ResteasyConfig;
import com.acuo.common.app.ResteasyMain;
import com.acuo.common.security.EncryptionModule;
import com.acuo.persist.modules.*;
import com.acuo.valuation.modules.*;
import com.acuo.valuation.quartz.DailyPriceJob;
import com.acuo.valuation.web.ObjectMapperContextResolver;
import com.google.inject.Module;
import com.opengamma.strata.basics.schedule.Schedule;
import com.opengamma.strata.basics.schedule.ScheduleException;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Collection;

import static java.util.Arrays.asList;

@Slf4j
public class ValuationApp extends ResteasyMain {

    @Override
    public Class<? extends ResteasyConfig> config() {
        return ResteasyConfigImpl.class;
    }

    @Override
    public Collection<Class<?>> providers() {
        return asList(ObjectMapperContextResolver.class);
    }

    @Override
    public Collection<Module> modules() {
        return asList(new MappingModule(),
                    new EncryptionModule(),
                    new ConfigurationModule(),
                    new ParsersModule(),
                    new EndPointModule(),
                    new ServicesModule(),
                    new ResourcesModule(),
                    new HealthChecksModule(),
                    new Neo4jPersistModule(),
                    new Neo4jPersistModule(),
                    new DataLoaderModule(),
                    new DataImporterModule(),
                    new ImportServiceModule(),
                    new RepositoryModule());
    }

    public static void main(String[] args) throws Exception {

        ValuationApp valuationApp = new ValuationApp();
        valuationApp.startAsync();
        JobDetail jobDetail = JobBuilder.newJob(DailyPriceJob.class).withIdentity("DailyPriceJob", "markitgroup").build();
        try
        {
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity("DailyPriceJob", "markitgroup").withSchedule(CronScheduleBuilder.cronSchedule("0 */40 * * * ?")).build();
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(jobDetail, trigger);
        }
        catch (ScheduleException e)
        {
            log.error("error in Scheduler:" + e.toString());
        }
    }
}