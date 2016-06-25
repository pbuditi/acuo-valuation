package com.acuo.valuation.modules;

import com.acuo.common.type.TypedString;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class CustomModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public CustomModule() {

        addSerializer(TypedString.class, new ToStringSerializer(TypedString.class));

    }

}