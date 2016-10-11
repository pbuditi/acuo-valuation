package com.acuo.valuation

import com.acuo.common.security.EncryptionModule
import com.acuo.valuation.modules.ConfigurationModule
import com.google.inject.Guice
import com.google.inject.Injector
import spock.lang.Specification

import static com.acuo.valuation.utils.PropertiesHelper.ACUO_CONFIG_APPID
import static com.acuo.valuation.utils.PropertiesHelper.ACUO_CONFIG_ENV
import static com.acuo.valuation.utils.PropertiesHelper.ACUO_SECURITY_KEY


class PropertiesSpecification extends Specification {

    static final String APPID_KEY = ACUO_CONFIG_APPID;
    static final String APPID_VALUE = "valuation-app";
    static final String ENV_KEY = ACUO_CONFIG_ENV;
    static final String ENV_VALUE = "dev";
    static final String SEC_KEY = ACUO_SECURITY_KEY;
    static final String SEC_VALUE = "dummy";
    static final String STRING_KEY = "value.string"
    static final String STRING_VALUE = "This is a string!"
    static final String INT_KEY = "value.int"
    static final String INT_VALUE = "${Integer.MAX_VALUE}"
    static final String LONG_KEY = "value.long"
    static final String LONG_VALUE = "${Long.MAX_VALUE}"
    static final String CHARSET_KEY = "value.charset"
    static final String CHARSET_VALUE = "US-ASCII"

    void setupSpec() {
        [
                "${APPID_KEY}": APPID_VALUE,
                "${ENV_KEY}": ENV_VALUE,
                "${SEC_KEY}": SEC_VALUE,
                "${STRING_KEY}": STRING_VALUE,
                "${INT_KEY}": INT_VALUE,
                "${LONG_KEY}": LONG_VALUE,
                "${CHARSET_KEY}": CHARSET_VALUE
        ].each { String key, String value ->
            System.properties[key] = value
        }
    }

    ConfigurationModule fixture = new ConfigurationModule()
    Injector injector = Guice.createInjector(new EncryptionModule(), fixture)

    void "sanity check"() {
        expect:
        System.properties[STRING_KEY] == STRING_VALUE
        System.properties[INT_KEY] == INT_VALUE
        System.properties[LONG_KEY] == LONG_VALUE
        System.properties[CHARSET_KEY] == CHARSET_VALUE
    }

}