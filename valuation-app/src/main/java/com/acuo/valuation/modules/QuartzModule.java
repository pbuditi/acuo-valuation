package com.acuo.valuation.modules;

import com.acuo.valuation.quartz.*;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.quartz.spi.JobFactory;

public class QuartzModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JobFactory.class).to(AcuoJobFactory.class);
        bind(TradePriceJob.class);
        bind(AssetPriceJob.class);
        bind(FXValueJob.class);

        Multibinder<Service> services = Multibinder.newSetBinder(binder(), Service.class);
        services.addBinding().to(SchedulerService.class);
    }
}
