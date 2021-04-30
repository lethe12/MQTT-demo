package com.cmiot.onenet.studio.gateway;

import android.app.Application;

import com.cmiot.onenet.studio.gateway.utils.NetworkUtils;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NetworkUtils.init(this);
    }
}
