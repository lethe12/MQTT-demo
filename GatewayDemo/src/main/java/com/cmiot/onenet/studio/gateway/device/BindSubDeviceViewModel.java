package com.cmiot.onenet.studio.gateway.device;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BindSubDeviceViewModel extends ViewModel {

    public final MutableLiveData<String> deviceName = new MutableLiveData<>();

}
