package com.acuo.valuation.learning.quartz;

import com.acuo.common.app.ServiceManagerModule;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        install(new ServiceManagerModule());

        bind(SchedulerFactory.class).to(StdSchedulerFactory.class).in(Scopes.SINGLETON);
        bind(GuiceJobFactory.class).in(Scopes.SINGLETON);
        bind(Quartz.class).in(Scopes.SINGLETON);
    }
}