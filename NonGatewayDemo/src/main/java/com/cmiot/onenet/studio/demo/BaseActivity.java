package com.cmiot.onenet.studio.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cmiot.onenet.studio.demo.utils.NetworkUtils;

import java.util.List;

public abstract class BaseActivity<T extends ViewDataBinding, U extends ViewModel> extends AppCompatActivity {

    private T mBinding;

    private U mViewModel;

    private boolean mNetworkAvailable = true;

    protected abstract int getLayoutId();

    protected abstract Class<U> getViewModelClass();

    protected abstract void setViewModel(T binding, U viewModel);

    protected abstract void initViews();

    /**
     * 获取Intent传递的数据
     */
    protected void resolveIntents(Intent intent) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        if (getLayoutId() != 0) {
            mBinding = DataBindingUtil.setContentView(this, getLayoutId());
            if (getViewModelClass() != null) {
                mViewModel = new ViewModelProvider(this).get(getViewModelClass());
                setViewModel(mBinding, mViewModel);
            }
            mBinding.setLifecycleOwner(this);
            initViews();
        }
        resolveIntents(getIntent());
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (isActivityExist(intent)) {
            super.startActivityForResult(intent, requestCode);
        }
    }

    protected boolean isActivityExist(Intent intent) {
        PackageManager pm = getPackageManager();
        ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return info != null;
    }

    protected T getBinding() {
        return mBinding;
    }

    protected U getViewModel() {
        return mViewModel;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mNetworkReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean networkState = NetworkUtils.isNetworkAvailable();
            if (mNetworkAvailable != networkState) {
                mNetworkAvailable = networkState;
                if (mNetworkAvailable) {
                    onNetworkConnected();
                    onFragmentsNetworkConnected();
                } else {
                    onNetworkDisconnected();
                    onFragmentsNetworkDisconnected();
                }
            }
        }
    };

    protected void onNetworkConnected() {
    }

    protected void onNetworkDisconnected() {
    }

    private void onFragmentsNetworkConnected() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments == null) {
            return;
        }
        for (Fragment fragment : fragments) {
            if (fragment instanceof BaseFragment) {
                ((BaseFragment) fragment).onNetworkConnected();
            }
        }
    }

    private void onFragmentsNetworkDisconnected() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments == null) {
            return;
        }
        for (Fragment fragment : fragments) {
            if (fragment instanceof BaseFragment) {
                ((BaseFragment) fragment).onNetworkDisconnected();
            }
        }
    }

}
