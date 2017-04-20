package com.acuo.valuation.learning.quartz;

import com.google.inject.Injector;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import javax.inject.Inject;

class GuiceJobFactory implements JobFactory {

    private final Injector guice;

    @Inject
    public GuiceJobFactory(final Injector guice) {
        this.guice = guice;
    }

    @Override
    public Job newJob(final TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        JobDetail jobDetail = bundle.getJobDetail();
        Class jobClass = jobDetail.getJobClass();
        return (Job) guice.getInstance(jobClass);
    }
}