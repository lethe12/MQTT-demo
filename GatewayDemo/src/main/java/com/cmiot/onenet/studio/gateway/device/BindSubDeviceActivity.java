package com.cmiot.onenet.studio.gateway.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cmiot.onenet.studio.gateway.BaseActivity;
import com.cmiot.onenet.studio.gateway.R;
import com.cmiot.onenet.studio.gateway.databinding.ActivityBindSubDeviceBinding;
import com.cmiot.onenet.studio.gateway.utils.MqttUtils;
import com.cmiot.onenet.studio.mqtt.MessageReceiver;
import com.cmiot.onenet.studio.mqtt.MqttClientCallback;
import com.cmiot.onenet.studio.mqtt.MqttClientHelper;
import com.cmiot.onenet.studio.mqtt.MqttClientHelperREy3k0pk6N;

/**
 * 绑定子设备页面
 */
public class BindSubDeviceActivity extends BaseActivity<ActivityBindSubDeviceBinding, BindSubDeviceViewModel> {

    private String mProductId;
    private String mProductKey;

    public static void open(Context context) {
        Intent intent = new Intent(context, BindSubDeviceActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_bind_sub_device;
    }

    @Override
    protected Class<BindSubDeviceViewModel> getViewModelClass() {
        return BindSubDeviceViewModel.class;
    }

    @Override
    protected void setViewModel(ActivityBindSubDeviceBinding binding, BindSubDeviceViewModel viewModel) {
        binding.setViewmodel(viewModel);
    }

    @Override
    protected void initViews() {
        setSupportActionBar(getBinding().toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] titles = {MqttClientHelper.PRODUCT_NAME, MqttClientHelperREy3k0pk6N.PRODUCT_NAME};
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, R.layout.layout_project_name_spinner_item, titles);
        adapter.setDropDownViewResource(R.layout.layout_spinner_item);
        getBinding().spinner.setAdapter(adapter);
        getBinding().spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (0 == position) {
                    mProductId = MqttClientHelper.PRODUCT_ID;
                    mProductKey = MqttClientHelper.PRODUCT_KEY;
                } else if (1 == position) {
                    mProductId = MqttClientHelperREy3k0pk6N.PRODUCT_ID;
                    mProductKey = MqttClientHelperREy3k0pk6N.PRODUCT_KEY;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        getBinding().bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MqttUtils.mqttClient.bindSubDevice(System.currentTimeMillis() + "", mProductId, getViewModel().deviceName.getValue(), mProductKey);
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MqttUtils.mqttClient.addCallback(mCallback);
        MqttUtils.mqttClientHelper.addMessageReceiver(mMessageReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        MqttUtils.mqttClient.removeCallback(mCallback);
        MqttUtils.mqttClientHelper.removeMessageReceiver(mMessageReceiver);
        super.onDestroy();
    }

    private MqttClientCallback mCallback = new MqttClientCallback() {

        @Override
        public void onReceiveMessage(final String topic, final byte[] payload) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MqttUtils.mqttClientHelper.onReceiveMessage(topic, payload);
                }
            });
        }
    };

    private MessageReceiver mMessageReceiver = new MessageReceiver() {
        @Override
        public void onBindSubDevice(String msgId, int code, String msg) {
            if (200 == code) {
                Toast.makeText(getApplicationContext(), "绑定成功", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        }
    };
}
