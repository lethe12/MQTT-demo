/*
 * Copyright (c) 2016-2019 China Mobile Communications Corporation. All rights reserved.
 */

package com.cmiot.onenet.studio.demo.utils;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;

public final class NetworkUtils {


    /**
     * 没有连接网络
     */
    private static final int NETWORK_NONE = -1;
    /**
     * 移动网络
     */
    private static final int NETWORK_MOBILE = 0;
    /**
     * 无线网络
     */
    private static final int NETWORK_WIFI = 1;

    private static WifiManager sWifiManager;
    private static ConnectivityManager sConnectivityManager;
    private static IntentFilter sIntentFilter;

    private NetworkUtils() {

    }

    public static void init(Context context) {
        if (null == sWifiManager) {
            Context applicationContext = context.getApplicationContext();
            sWifiManager = (WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE);
            sConnectivityManager = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            sIntentFilter = new IntentFilter();
            sIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            sIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        }
    }

    private static void checkConnectivityManager() {
        if (null == sConnectivityManager) {
            throw new IllegalStateException("init method must be called first");
        }
    }

    private static void checkWifiManager() {
        if (null == sWifiManager) {
            throw new IllegalStateException("init method must be called first");
        }
    }

    /**
     * 判断网络是否可用
     */
    public static boolean isNetworkAvailable() {
        checkConnectivityManager();
        NetworkInfo info = sConnectivityManager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
     * 检测wifi是否连接
     */
    public static boolean isWifiConnected() {
        checkConnectivityManager();
        NetworkInfo info = sConnectivityManager.getActiveNetworkInfo();
        return info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 检测3G是否连接
     */
    public static boolean is3gConnected() {
        checkConnectivityManager();
        NetworkInfo info = sConnectivityManager.getActiveNetworkInfo();
        return info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public static WifiInfo getWifiInfo() {
        checkWifiManager();
        return sWifiManager.getConnectionInfo();
    }

    public static NetworkInfo getNetworkInfo() {
        checkConnectivityManager();
        return sConnectivityManager.getActiveNetworkInfo();
    }

    public static DhcpInfo getDhcpInfo() {
        checkWifiManager();
        return sWifiManager.getDhcpInfo();
    }

    public static IntentFilter getWifiIntentFilter() {
        return sIntentFilter;
    }

    public static int getRssi() {
        return getWifiInfo().getRssi();
    }

    public static void startScan() {
        sWifiManager.startScan();
    }

    public static int getSignalLevel(int rssi, int numLevels) {
        return WifiManager.calculateSignalLevel(rssi, numLevels);
    }

    public static String getSsid() {
        String ssid = getWifiInfo().getSSID();
        if (ssid != null) {
            if (ssid.contains("unknown ssid") || "".equals(ssid)) {
                ssid = getNetworkInfo().getExtraInfo();
                if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    ssid = ssid.substring(1, ssid.length() - 1);
                }
            } else {
                if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    ssid = ssid.substring(1, ssid.length() - 1);
                }
            }
        }
        return ssid != null ? ssid : "";
    }

    public static int getNetWorkState() {
        checkConnectivityManager();
        // 得到连接管理器对象
        NetworkInfo activeNetworkInfo = sConnectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return NETWORK_WIFI;
            } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return NETWORK_MOBILE;
            }
        } else {
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }

    public static String getGateway() {
        DhcpInfo dhcpInfo = getDhcpInfo();
        int gateway = dhcpInfo.gateway;
        return convertIpToString(gateway);
    }

    public static List<ScanResult> getScanResults() {
        checkWifiManager();
        return sWifiManager.getScanResults();
    }

    private static String convertIpToString(int ip) {
        StringBuffer sb = new StringBuffer();
        sb.append(ip & 0xff);
        sb.append('.');
        sb.append((ip >> 8) & 0xff);
        sb.append('.');
        sb.append((ip >> 16) & 0xff);
        sb.append('.');
        sb.append((ip >> 24) & 0xff);
        return sb.toString();
    }

    public boolean is24GHzWifi(int frequency) {
        return frequency > 2400 && frequency < 2500;
    }

    public boolean is5GHzWifi(int frequency) {
        return frequency > 4900 && frequency < 5900;
    }

}
