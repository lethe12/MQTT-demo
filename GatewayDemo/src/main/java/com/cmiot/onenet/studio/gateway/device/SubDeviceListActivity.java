package com.cmiot.onenet.studio.gateway.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.cmiot.onenet.studio.gateway.BaseActivity;
import com.cmiot.onenet.studio.gateway.R;
import com.cmiot.onenet.studio.gateway.databinding.ActivitySubDeviceListBinding;
import com.cmiot.onenet.studio.mqtt.MessageReceiver;
import com.cmiot.onenet.studio.mqtt.MqttClientCallback;
import com.cmiot.onenet.studio.gateway.utils.MqttUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * 子设备列表
 */
public class SubDeviceListActivity extends BaseActivity<ActivitySubDeviceListBinding, SubDeviceListViewModel> {

    public static void open(Context context) {
        Intent intent = new Intent(context, SubDeviceListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_sub_device_list;
    }

    @Override
    protected Class<SubDeviceListViewModel> getViewModelClass() {
        return SubDeviceListViewModel.class;
    }

    @Override
    protected void setViewModel(ActivitySubDeviceListBinding binding, SubDeviceListViewModel viewModel) {
        binding.setViewmodel(viewModel);
    }

    @Override
    protected void initViews() {
        setSupportActionBar(getBinding().toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getBinding().deviceList.setLayoutManager(new LinearLayoutManager(this));
        getBinding().deviceList.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                Map data = mAdapter.getItem(position);
                String productId = (String) data.get("productID");
                String deviceName = (String) data.get("deviceName");
                SubDeviceActivity.open(SubDeviceListActivity.this, productId, deviceName);
            }
        });
        getBinding().swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MqttUtils.mqttClient.getSubDevices(System.currentTimeMillis() + "");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getBinding().swipeRefreshLayout.setRefreshing(false);
                    }
                }, 5000);
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MqttUtils.mqttClient.addCallback(mCallback);
        MqttUtils.mqttClientHelper.addMessageReceiver(mMessageReceiver);
        MqttUtils.mqttClient.getSubDevices(System.currentTimeMillis() + "");
    }

    @Override
    protected void onDestroy() {
        MqttUtils.mqttClient.removeCallback(mCallback);
        MqttUtils.mqttClientHelper.removeMessageReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "关联子设备").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case 0:
                BindSubDeviceActivity.open(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private MqttClientCallback mCallback = new MqttClientCallback() {

        @Override
        public void onReceiveMessage(final String topic, final byte[] payload) {
            // 平台下发到设备的原始数据，在 MqttClientHelper.onReceiveMessage() 方法中根据topic的不同进行分发
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
        public void onGetSubDevices(String msgId, int code, String msg, List<Map> data) {
            // 收到网关设备拓扑关系数据
            getBinding().swipeRefreshLayout.setRefreshing(false);
            if (200 == code) {
                mAdapter.setNewInstance(data);
            } else {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onSubDevicesChanged(String msgId, List<Map> data) {
            // 收到拓扑关系变化通知
            mAdapter.setNewInstance(data);
        }
    };

    private BaseQuickAdapter<Map, BaseViewHolder> mAdapter = new BaseQuickAdapter<Map, BaseViewHolder>(R.layout.sub_device_list_item) {
        @Override
        protected void convert(@NotNull BaseViewHolder helper, Map map) {
            helper.setText(R.id.device_name, (String) map.get("deviceName"))
                    .setText(R.id.product_id, (String) map.get("productID"));
        }
    };
}
