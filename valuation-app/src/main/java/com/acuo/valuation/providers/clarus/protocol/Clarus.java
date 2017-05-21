package com.acuo.valuation.providers.clarus.protocol;

import com.acuo.common.model.margin.Types;

public interface Clarus {

    enum DataModel {
        LCH, CME
    }

    enum MarginCallType {
        IM(Types.CallType.Initial), VM(Types.CallType.Variation);

        private Types.CallType callType;

        private MarginCallType(Types.CallType callType) {
            this.callType = callType;
        }

        public Types.CallType getCallType() {
            return callType;
        }
    }

    enum CalculationMethod {
        Taylor​, Optimised​, FullReval
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
        House, Client
    }
}
