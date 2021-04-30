package com.cmiot.onenet.studio.gateway.device;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cmiot.onenet.studio.mqtt.MqttClient;
import com.cmiot.onenet.studio.mqtt.MqttClientCallback;
import com.cmiot.onenet.studio.mqtt.MqttClientHelper;
import com.cmiot.onenet.studio.mqtt.MqttClientHelperREy3k0pk6N;
import com.cmiot.onenet.studio.mqtt.annotations.ThingModel;
import com.cmiot.onenet.studio.mqtt.annotations.ThingModels;
import com.cmiot.onenet.studio.mqtt.processor.ThingModelObject;
import com.cmiot.onenet.studio.gateway.BaseActivity;
import com.cmiot.onenet.studio.gateway.BaseFragment;
import com.cmiot.onenet.studio.gateway.R;
import com.cmiot.onenet.studio.gateway.databinding.ActivityDeviceBinding;
import com.cmiot.onenet.studio.gateway.utils.MqttUtils;
import com.cmiot.onenet.studio.gateway.utils.ThingModelUtils;

import java.util.ArrayList;
import java.util.List;

@ThingModels({
        @ThingModel(
                path = "GatewayDemo/src/main/assets/model-I2ShgOIGdw.json",
                productId = "I2ShgOIGdw",
                productName = "DeviceTest",
                productKey = "raM9//M9ORHVC5VRR+91QVsfOXG1VZnnrZNtC+dpS4k="
        ),
        @ThingModel(
                path = "GatewayDemo/src/main/assets/model-REy3k0pk6N.json",
                productId = "REy3k0pk6N",
                productName = "SubDeviceTest",
                productKey = "P41PiWW5wxnTOzcXo3Pp83OZ+y9x968RJZWnO7sHFwU=",
                subDevice = true // 子设备
        )
})
public class DeviceActivity extends BaseActivity<ActivityDeviceBinding, DeviceViewModel> {

    private List<BaseFragment> mFragments = new ArrayList<>(2);
    private LogFragment mLogFragment;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_device;
    }

    @Override
    protected Class<DeviceViewModel> getViewModelClass() {
        return DeviceViewModel.class;
    }

    @Override
    protected void setViewModel(ActivityDeviceBinding binding, DeviceViewModel viewModel) {
        binding.setViewmodel(viewModel);
    }

    @Override
    protected void initViews() {
        setSupportActionBar(getBinding().toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_titles, R.layout.layout_spinner_title);
        adapter.setDropDownViewResource(R.layout.layout_spinner_item);
        getBinding().actionBarSpinner.setAdapter(adapter);
        getBinding().actionBarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, mFragments.get(position)).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 此处要替换成用户自己的物模型配置文件
        String json = ThingModelUtils.readThingModel(this, "model-I2ShgOIGdw.json");
        ThingModelObject thingModelObject = ThingModelUtils.parseThingModel(json);
        mFragments.add(new PropertiesFragment(thingModelObject.properties, false));
        mFragments.add(new EventsFragment(thingModelObject.events, false));
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mFragments.get(0)).commit();

        mLogFragment = (LogFragment) getSupportFragmentManager().findFragmentById(R.id.log_fragment);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MqttUtils.mqttClient = createMqttClient(
                MqttClientHelper.PRODUCT_ID,
                MqttClientHelper.PRODUCT_KEY,
                "Gateway" // 此处应替换为用户的设备名称
        );
        MqttUtils.mqttClient.addCallback(mCallback);
        MqttUtils.mqttClientHelper = MqttClientHelper.bind(MqttUtils.mqttClient);
        MqttUtils.mqttClientHelperREy3k0pk6N = MqttClientHelperREy3k0pk6N.bind(MqttUtils.mqttClient);

        try {
            MqttUtils.mqttClient.connect(); // 连接服务器
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "清除日志").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, 1, 1, "子设备").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case 0:
                mLogFragment.clearLogs();
                break;
            case 1:
                SubDeviceListActivity.open(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * productKey 和 deviceKey 二选一，如果同时设置，优先选择 deviceKey 鉴权
     */
    private MqttClient createMqttClient(String productId, String accessKey, String deviceName) {
        return new MqttClient.Builder()
                // 产品id（必填）
                .productId(productId)
                // 产品key或设备秘钥
                .accessKey(accessKey)
                // 设备名称（必填）
                .deviceName(deviceName)
                // 是否使用 ssl 加密通信，默认 true
                .ssl(true)
                // 鉴权 token 超时时间，单位秒，默认 100 天
                .expireTime(100 * 24 * 60 * 60)
                // 是否自动重连
                .autoReconnect(true)
                // 连接超时时间，以秒为单位，默认15秒
                .connectionTimeout(10)
                // keep-alive 发送间隔，以秒为单位，默认120秒
                .keepAliveInterval(60)
                // 是否显示调试日志
                .showDebugLog(true)
                .build();
    }

    private MqttClientCallback mCallback = new MqttClientCallback() {

        @Override
        public void onConnected(String serverUrl) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "连接成功", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onConnectFailed(String serverUrl, Throwable throwable) {
            if (throwable != null) {
                throwable.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onConnectionLost(Throwable throwable) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "连接断开", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onReceiveMessage(final String topic, final byte[] payload) {
            // 平台下发到设备的原始数据，在 MqttClientHelper.onReceiveMessage() 方法中根据topic的不同进行分发
            // 因为回调都是异步执行在子线程中，所以需要回到主线程更新 UI
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLogFragment.updateLog(new String(payload));
                    MqttUtils.mqttClientHelper.onReceiveMessage(topic, payload);
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 移除 Mqtt 回调
        MqttUtils.mqttClient.removeCallback(mCallback);
        // 断开服务器连接
        MqttUtils.mqttClient.disconnect();
    }

}
