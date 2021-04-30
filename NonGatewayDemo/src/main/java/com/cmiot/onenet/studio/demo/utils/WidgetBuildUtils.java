package com.cmiot.onenet.studio.demo.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cmiot.onenet.studio.demo.R;

public class WidgetBuildUtils {

    public static ViewGroup buildStructField(Context context) {
        return (ViewGroup) LayoutInflater.from(context).inflate(R.layout.layout_struct, null, false);
    }

    public static View buildNumberField(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.layout_number_field, null, false);
    }

    public static View buildSpinnerField(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.layout_spinner_field, null, false);
    }

    public static View buildTextField(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.layout_text_field, null, false);
    }

    public static ViewGroup buildArrayField(Context context) {
        return (ViewGroup) LayoutInflater.from(context).inflate(R.layout.layout_array, null, false);
    }

}
