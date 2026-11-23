package com.ign.cartociudad.ws;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.ign.cartociudad.ws.impl.GeocodingServiceElasticImpl;

public class GeocoderApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.<Class<?>>singleton(
            GeocodingServiceElasticImpl.class
        );
    }
}
