package com.cmiot.onenet.studio.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public abstract class BaseFragment<T extends ViewDataBinding, U extends ViewModel> extends Fragment {

    private T mBinding;

    private U mViewModel;

    protected abstract int getLayoutId();

    protected abstract Class<U> getViewModelClass();

    protected abstract void setViewModel(T binding, U viewModel);

    protected abstract void initViews();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getViewModelClass() != null) {
            mViewModel = new ViewModelProvider(requireActivity()).get(getViewModelClass());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getLayoutId() != 0) {
            mBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
            if (mViewModel != null) {
                setViewModel(mBinding, mViewModel);
            }
            mBinding.setLifecycleOwner(this);
        }
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
    }

    protected T getBinding() {
        return mBinding;
    }

    protected U getViewModel() {
        return mViewModel;
    }

    protected void onNetworkConnected() {
    }

    protected void onNetworkDisconnected() {
    }
}
