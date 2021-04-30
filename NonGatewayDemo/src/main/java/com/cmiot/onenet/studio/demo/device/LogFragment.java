package com.cmiot.onenet.studio.demo.device;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.cmiot.onenet.studio.demo.BaseFragment;
import com.cmiot.onenet.studio.demo.R;
import com.cmiot.onenet.studio.demo.databinding.FragmentLogBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LogFragment extends BaseFragment<FragmentLogBinding, DeviceViewModel> {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_log;
    }

    @Override
    protected Class<DeviceViewModel> getViewModelClass() {
        return DeviceViewModel.class;
    }

    @Override
    protected void setViewModel(FragmentLogBinding binding, DeviceViewModel viewModel) {
        binding.setViewmodel(viewModel);
    }

    @Override
    protected void initViews() {
        getBinding().recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().recyclerView.setAdapter(mAdapter);
        mAdapter.setEmptyView(R.layout.layout_log_empty_view);
    }

    private BaseQuickAdapter<String, BaseViewHolder> mAdapter = new BaseQuickAdapter<String, BaseViewHolder>(R.layout.recycler_item_log) {
        @Override
        protected void convert(@NotNull BaseViewHolder holder, String item) {
            holder.setText(R.id.text, item);
        }
    };

    public void updateLog(String log) {
        JsonObject jsonObject = JsonParser.parseString(log).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        mAdapter.addData(gson.toJson(jsonObject));
    }

    public void updateLogs(List<String> logs) {
        mAdapter.addData(logs);
    }

    public void clearLogs() {
        mAdapter.setList(null);
    }
}
