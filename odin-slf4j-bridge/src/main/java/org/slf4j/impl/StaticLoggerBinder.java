package org.slf4j.impl;

import com.permutive.logging.slf4j.odin.OdinLoggerFactoryBinder;

public class StaticLoggerBinder extends OdinLoggerFactoryBinder {

    public static String REQUESTED_API_VERSION = "1.7";

    private static final StaticLoggerBinder _instance = new StaticLoggerBinder();

    public static StaticLoggerBinder getSingleton() {
        return _instance;
    }

}
