package com.acuo.valuation;

import com.acuo.common.http.server.HttpServerWrapperConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResteasyConfigImplTest {

    @Test
    public void testWithContextPathEndingWithSlash() throws Exception {
        ResteasyConfigImpl configFactory = new ResteasyConfigImpl(0, "127.0.0.1", "", "/", "test");
        HttpServerWrapperConfig config = configFactory.getConfig();

        assertThat(config.getContextPath()).isEqualTo("/");
        assertThat(config.getHttpServerConnectorConfigs()).isNotEmpty();
        assertThat(config.getInitParemeters().get("resteasy.servlet.mapping.prefix")).isEqualTo("test");;
    }

    @Test
    public void testWithContextPathEndingWithNoSlash() throws Exception {
        ResteasyConfigImpl configFactory = new ResteasyConfigImpl(0, "127.0.0.1", "","", "test");
        HttpServerWrapperConfig config = configFactory.getConfig();

        assertThat(config.getContextPath()).isEqualTo("/");
        assertThat(config.getHttpServerConnectorConfigs()).isNotEmpty();
        assertThat(config.getInitParemeters().get("resteasy.servlet.mapping.prefix")).isEqualTo("test");;
    }

}