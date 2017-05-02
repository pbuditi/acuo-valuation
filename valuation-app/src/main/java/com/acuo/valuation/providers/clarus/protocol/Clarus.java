package com.acuo.valuation.providers.clarus.protocol;

public interface Clarus {

    enum DataModel {
        LCH, CME;
    }

    enum MarginCallType {
        IM, VM;
    }

    enum CalculationMethod {
        Taylor​, Optimised​, FullReval;
    }

    enum ResultStats {
        Default("Default"),
        AvgMaturity("AvgMaturity"),
        CalcTime("CalcTime"),
        CurveSize("CurveSize"),
        MTMCPUs("MTM/CPU/s"),
        MTMs("MTM/s"),
        Memory("Memory%"),
        NumCPU("NumCPU"),
        NumMTM("NumMTM"),
        NumRequests("NumRequests"),
        NumTrades("NumTrades"),
        ResponseTime("ResponseTime"),
        TaskTime("TaskTime");

        private final String key;

        private ResultStats(String key) {
            this.key = key;
        }
    }

    enum HouseClient {
        House, Client;
    }
}
