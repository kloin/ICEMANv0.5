package com.teamunemployment.breadcrumbs.Weather;

/**
 * Created by jek40 on 24/03/2016.
 */
import android.content.Context;

import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;


public class WeatherContext {
    private static WeatherContext me;
    private WeatherClient client;

    private WeatherContext() {}

    public static WeatherContext getInstance() {
        if (me == null)
            me = new WeatherContext();

        return me;
    }

    public WeatherClient getClient(Context ctx) {
        if (client != null)
            return client;

        try {
            WeatherConfig config = new WeatherConfig();
            config.ApiKey = "cb8f99f89cda12614a6f8466339e19bf";
            client = new WeatherClient.ClientBuilder()
                    .attach(ctx)
                    .config(config)
                    .provider(new OpenweathermapProviderType())
                    .httpClient(WeatherDefaultClient.class)
                    .build();
        }
        catch (WeatherProviderInstantiationException e) {
            e.printStackTrace();
        }

        return client;
    }
}
