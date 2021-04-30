package com.cmiot.onenet.studio.gateway.device;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.cmiot.onenet.studio.mqtt.MessageReceiver;
import com.cmiot.onenet.studio.mqtt.MessageReceiverREy3k0pk6N;
import com.cmiot.onenet.studio.mqtt.MqttClientCallback;
import com.cmiot.onenet.studio.mqtt.MqttClientHelperREy3k0pk6N;
import com.cmiot.onenet.studio.mqtt.processor.ThingModelObject;
import com.cmiot.onenet.studio.gateway.BaseActivity;
import com.cmiot.onenet.studio.gateway.BaseFragment;
import com.cmiot.onenet.studio.gateway.R;
import com.cmiot.onenet.studio.gateway.databinding.ActivitySubDeviceBinding;
import com.cmiot.onenet.studio.gateway.utils.MqttUtils;
import com.cmiot.onenet.studio.gateway.utils.ThingModelUtils;

import java.util.ArrayList;
import java.util.List;

public class SubDeviceActivity extends BaseActivity<ActivitySubDeviceBinding, SubDeviceViewModel> {

    private static final String EXTRA_PRODUCT_ID = "extra_product_id";
    private static final String EXTRA_DEVICE_NAME = "extra_device_name";

    public String mProductId;
    public String mDeviceName;

    private List<BaseFragment> mFragments = new ArrayList<>(2);
    private LogFragment mLogFragment;

    public static void open(Context context, String productId, String deviceName) {
        Intent intent = new Intent(context, SubDeviceActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, productId);
        intent.putExtra(EXTRA_DEVICE_NAME, deviceName);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_sub_device;
    }

    @Override
    protected Class<SubDeviceViewModel> getViewModelClass() {
        return SubDeviceViewModel.class;
    }

    @Override
    protected void setViewModel(ActivitySubDeviceBinding binding, SubDeviceViewModel viewModel) {
        binding.setViewmodel(viewModel);
    }

    @Override
    protected void initViews() {
        setSupportActionBar(getBinding().toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

        String json = ThingModelUtils.readThingModel(this, "model-REy3k0pk6N.json");
        ThingModelObject thingModelObject = ThingModelUtils.parseThingModel(json);
        mFragments.add(new PropertiesFragment(thingModelObject.properties, true));
        mFragments.add(new EventsFragment(thingModelObject.events, true));
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mFragments.get(0)).commit();

        mLogFragment = (LogFragment) getSupportFragmentManager().findFragmentById(R.id.log_fragment);
    }

    @Override
    protected void resolveIntents(Intent intent) {
        mProductId = intent.getStringExtra(EXTRA_PRODUCT_ID);
        mDeviceName = intent.getStringExtra(EXTRA_DEVICE_NAME);
        MqttUtils.mqttClient.addCallback(mCallback);
        MqttUtils.mqttClientHelper.addMessageReceiver(mMessageReceiver);
        MqttUtils.mqttClientHelperREy3k0pk6N.addMessageReceiver(mMessageReceiverREy3k0pk6N);
        // 子设备上线
        MqttUtils.mqttClient.subDeviceLogin(System.currentTimeMillis() + "", mProductId, mDeviceName);
    }

    @Override
    protected void onDestroy() {
        MqttUtils.mqttClient.removeCallback(mCallback);
        MqttUtils.mqttClientHelper.removeMessageReceiver(mMessageReceiver);
        MqttUtils.mqttClientHelperREy3k0pk6N.removeMessageReceiver(mMessageReceiverREy3k0pk6N);
        // 子设备下线
        MqttUtils.mqttClient.subDeviceLogout(System.currentTimeMillis() + "", mProductId, mDeviceName);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "清除日志").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, 1, 1, "解绑").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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
                MqttUtils.mqttClient.unbindSubDevice(System.currentTimeMillis() + "", mProductId, mDeviceName, MqttClientHelperREy3k0pk6N.PRODUCT_KEY);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private MqttClientCallback mCallback = new MqttClientCallback() {
        @Override
        public void onReceiveMessage(final String topic, final byte[] payload) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLogFragment.updateLog(new String(payload));
                    MqttUtils.mqttClientHelper.onReceiveMessage(topic, payload);
                    MqttUtils.mqttClientHelperREy3k0pk6N.onReceiveMessage(topic, payload);
                }
            });
        }
    };

    private MessageReceiver mMessageReceiver = new MessageReceiver() {
        @Override
        public void onDeleteSubDevice(String msgId, int code, String msg) {
            if (200 == code) {
                Toast.makeText(getApplicationContext(), "解绑成功", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private MessageReceiverREy3k0pk6N mMessageReceiverREy3k0pk6N = new MessageReceiverREy3k0pk6N() {

    };
}
