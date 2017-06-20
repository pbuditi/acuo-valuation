package com.acuo.valuation.providers.acuo.calls;


import com.acuo.common.util.SimulationHelper;
import com.acuo.valuation.utils.PropertiesHelper;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class Simulator {

    private final SimulationHelper helper = new SimulationHelper();
    private final boolean enabled;

    @Inject
    public Simulator(@Named(PropertiesHelper.ACUO_SIMULATION_ENABLED) boolean enabled) {
        this.enabled = enabled;
    }

    public double getRandomAmount(Double value) {
        return (enabled) ? helper.getRandomAmount(value) : value;
    }

    public boolean getRandomBoolean() {
        return !enabled || helper.getRandomBoolean();
    }
}
