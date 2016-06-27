package com.acuo.valuation.modules;

import com.acuo.common.metrics.DiskSpaceHealthCheck;
import com.acuo.common.metrics.PingHealthCheck;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.health.HealthCheck;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class HealthChecksModule extends AbstractModule {
    @Override
    protected void configure() {

        final Multibinder<MetricSet> metricSetBinder = Multibinder.newSetBinder(binder(), MetricSet.class);
        //metricSetBinder.addBinding().toInstance(new VmSpecsMetricSet());

        final Multibinder<HealthCheck> healthChecksBinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
        healthChecksBinder.addBinding().to(PingHealthCheck.class).asEagerSingleton();
        healthChecksBinder.addBinding().to(DiskSpaceHealthCheck.class).asEagerSingleton();
    }
}
