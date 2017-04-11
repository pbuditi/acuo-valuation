package com.acuo.valuation.learning.quartz;

import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import javax.inject.Inject;

public class Main {
    public static void main(final String[] args) {
        final Injector injector = Guice.createInjector(new QuartzModule(), new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder<Service> services = Multibinder.newSetBinder(binder(), Service.class);
                services.addBinding().to(FooJobActivator.class);
            }
        });
        ServiceManager serviceManager = injector.getInstance(ServiceManager.class);
        serviceManager.startAsync();
        System.out.println("Guice ready, waiting 130 secs now...");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("shutdown");
        serviceManager.stopAsync();
        System.out.println("done");
    }

    static class FooJob implements Job {
        public FooJob() {
            System.out.println(this + " was created");
        }

        @Override
        public void execute(final JobExecutionContext arg0) throws JobExecutionException {
            System.out.println(this + " was run!");
        }
    }

    static class FooJobActivator extends AbstractService {

        private final Quartz q;

        @Inject
        public FooJobActivator(final Quartz q) throws SchedulerException {
            this.q = q;
        }

        @Override
        protected void doStart() {
            try {
                JobDetail jobDetail = JobBuilder
                        .newJob(FooJob.class)
                        .withIdentity("myFooJob")
                        .build();

                Trigger trigger = TriggerBuilder
                        .newTrigger()
                        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever())
                        .build();

                q.getScheduler().scheduleJob(jobDetail, trigger);
                notifyStarted();
            } catch (Exception e) {
                notifyFailed(e);
            }
        }

        @Override
        protected void doStop() {
            try {
                q.getScheduler().shutdown();
            } catch (Exception e) {
                notifyFailed(e);
            }
        }
    }
}