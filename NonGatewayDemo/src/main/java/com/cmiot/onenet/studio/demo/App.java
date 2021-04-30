package com.cmiot.onenet.studio.demo;

import android.app.Application;

import com.cmiot.onenet.studio.demo.utils.NetworkUtils;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NetworkUtils.init(this);
    }
}
