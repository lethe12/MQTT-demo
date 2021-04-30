package com.cmiot.onenet.studio.demo.device;

import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cmiot.onenet.studio.demo.BaseFragment;
import com.cmiot.onenet.studio.demo.R;
import com.cmiot.onenet.studio.demo.databinding.FragmentEventsBinding;
import com.cmiot.onenet.studio.demo.utils.MqttUtils;
import com.cmiot.onenet.studio.demo.utils.WidgetBuildUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventsFragment extends BaseFragment<FragmentEventsBinding, DeviceViewModel> implements View.OnClickListener {

    private List<Map> mEvents;
    private Map<String, Object> mWidgetMap = new HashMap<>();

    public EventsFragment() {

    }

    public EventsFragment(List<Map> events) {
        this.mEvents = events;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_events;
    }

    @Override
    protected Class<DeviceViewModel> getViewModelClass() {
        return DeviceViewModel.class;
    }

    @Override
    protected void setViewModel(FragmentEventsBinding binding, DeviceViewModel viewModel) {
        binding.setViewmodel(viewModel);
    }

    @Override
    protected void initViews() {
        getBinding().send.setOnClickListener(this);
        List<String> eventTitles = new ArrayList<>();
        for (Map event : mEvents) {
            String name = (String) event.get("name");
            String identifier = (String) event.get("identifier");
            eventTitles.add(name + "(" + identifier + ")");
        }
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(requireContext(), R.layout.layout_spinner_field_item, eventTitles.toArray(new String[]{}));
        getBinding().eventSpinner.setAdapter(adapter);
        getBinding().eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mWidgetMap.clear();
                getBinding().widgetsContainer.removeAllViews();
                List<Map> outputDatas = (List<Map>) mEvents.get(position).get("outputData");
                for (Map outputData : outputDatas) {
                    buildWidgets(outputData, getBinding().widgetsContainer, (String) mEvents.get(position).get("identifier"));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (!mEvents.isEmpty()) {
            List<Map> outputDatas = (List<Map>) mEvents.get(0).get("outputData");
            for (Map outputData : outputDatas) {
                buildWidgets(outputData, getBinding().widgetsContainer, (String) mEvents.get(0).get("identifier"));
            }
        }
    }

    private void buildWidgets(Map map, ViewGroup container, String rootIdentifier) {
        String identifier = (String) map.get("identifier");
        String name = (String) map.get("name");
        String type;
        Map dataType;
        if (identifier != null && name != null) {
            dataType = (Map) map.get("dataType");
        } else {
            dataType = map;
        }
        type = (String) dataType.get("type");
        switch (type) {
            case "int32":
            case "int64":
            case "float":
            case "double":
                buildNumberField(identifier, name, dataType, type, container, rootIdentifier);
                break;
            case "bitMap":
                buildBitMapField(identifier, name, dataType, container, rootIdentifier);
                break;
            case "enum":
                buildEnumField(identifier, name, dataType, container, rootIdentifier);
                break;
            case "bool":
                buildBoolField(identifier, name, dataType, container, rootIdentifier);
                break;
            case "string":
                buildTextField(identifier, name, dataType, container, rootIdentifier);
                break;
            case "struct":
                buildStructField(identifier, name, dataType, container, false, rootIdentifier);
                break;
            case "array":
                buildArrayField(identifier, name, dataType, container, rootIdentifier);
                break;
        }
    }

    private void buildNumberField(String identifier, String name, Map dataType, String type, ViewGroup container, String rootIdentifier) {
        View view = WidgetBuildUtils.buildNumberField(requireContext());
        TextView titleView = view.findViewById(R.id.title);
        if (identifier != null && name != null) {
            titleView.setText(name + "(" + identifier + ")(" + type + ")");
            titleView.setVisibility(View.VISIBLE);
        } else {
            titleView.setVisibility(View.GONE);
        }
        EditText editText = view.findViewById(R.id.edit_text);
        Map specs = (Map) dataType.get("specs");
        String max = (String) specs.get("max");
        String min = (String) specs.get("min");
        String step = (String) specs.get("step");
        String unit = (String) specs.get("unit");
        editText.setHint(min + "-" + max + "；步长：" + (step != null ? step : "无") + "；单位：" + (unit != null ? unit : "无"));
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        container.addView(view);
        int i = 0;
        String key = identifier != null ? identifier : rootIdentifier;
        while (mWidgetMap.get(key + i) != null) {
            i++;
        }
        mWidgetMap.put(key + i, editText);
    }

    private void buildBitMapField(String identifier, String name, Map dataType, ViewGroup container, String rootIdentifier) {
        View view = WidgetBuildUtils.buildNumberField(requireContext());
        TextView titleView = view.findViewById(R.id.title);
        if (identifier != null && name != null) {
            titleView.setText(name + "(" + identifier + ")(bitMap)");
            titleView.setVisibility(View.VISIBLE);
        } else {
            titleView.setVisibility(View.GONE);
        }
        EditText editText = view.findViewById(R.id.edit_text);
        Map specs = (Map) dataType.get("specs");
        int length = ((Double) specs.get("length")).intValue();
        editText.setHint("请输入最多" + length + "位二进制");
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(length)});
        container.addView(view);
        int i = 0;
        String key = identifier != null ? identifier : rootIdentifier;
        while (mWidgetMap.get(key + i) != null) {
            i++;
        }
        mWidgetMap.put(key + i, editText);
    }

    private void buildStructField(String identifier, String name, Map dataType, final ViewGroup container, boolean showDelete, String rootIdentifier) {
        final ViewGroup viewGroup = WidgetBuildUtils.buildStructField(requireContext());
        TextView titleView = viewGroup.findViewById(R.id.title);
        if (identifier != null) {
            titleView.setText(name + "(" + identifier + ")(struct)");
        } else {
            titleView.setText(name + "(struct)");
        }
        container.addView(viewGroup);

        Object specs = dataType.get("specs");
        if (specs instanceof List) {
            for (Map spec : (List<Map>) specs) {
                buildWidgets(spec, viewGroup, rootIdentifier);
            }
        } else {
            buildWidgets(dataType, viewGroup, rootIdentifier);
        }

        Button button = viewGroup.findViewById(R.id.delete);
        if (showDelete) {
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    container.removeView(viewGroup);
                }
            });
        } else {
            button.setVisibility(View.GONE);
        }
    }

    private void buildArrayField(String identifier, String name, Map dataType, ViewGroup container, String rootIdentifier) {
        Map rootSpecs = (Map) dataType.get("specs");
        final int size = ((Double) rootSpecs.get("length")).intValue();
        ViewGroup viewGroup = WidgetBuildUtils.buildArrayField(requireContext());
        TextView titleView = viewGroup.findViewById(R.id.title);
        titleView.setText(name + "(" + identifier + ")(array)(size:" + size + ")");
        container.addView(viewGroup);

        final ViewGroup childRoot = (ViewGroup) viewGroup.findViewById(R.id.child_root);
        buildStructField(null, "元素1", (Map) rootSpecs, childRoot, true, rootIdentifier);
        Button button = viewGroup.findViewById(R.id.add);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int childCount = childRoot.getChildCount();
                if (childCount < size) {
                    buildStructField(null, "元素" + (childCount + 1), (Map) rootSpecs, childRoot, true, rootIdentifier);
                } else {
                    Toast.makeText(requireContext(), "元素数量已达上限", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void buildBoolField(String identifier, String name, Map dataType, ViewGroup container, String rootIdentifier) {
        View view = WidgetBuildUtils.buildSpinnerField(requireContext());
        TextView titleView = view.findViewById(R.id.title);
        if (identifier != null && name != null) {
            titleView.setText(name + "(" + identifier + ")(bool)");
            titleView.setVisibility(View.VISIBLE);
        } else {
            titleView.setVisibility(View.GONE);
        }
        Spinner spinner = view.findViewById(R.id.spinner);
        Map specs = (Map) dataType.get("specs");
        String[] datas = {"true-" + specs.get("true"), "false-" + specs.get("false")};
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(requireContext(), R.layout.layout_spinner_field_item, datas);
        spinner.setAdapter(adapter);
        container.addView(view);
        int i = 0;
        String key = identifier != null ? identifier : rootIdentifier;
        while (mWidgetMap.get(key + i) != null) {
            i++;
        }
        mWidgetMap.put(key + i, spinner);
    }

    private void buildEnumField(String identifier, String name, Map dataType, ViewGroup container, String rootIdentifier) {
        View view = WidgetBuildUtils.buildSpinnerField(requireContext());
        TextView titleView = view.findViewById(R.id.title);
        if (identifier != null && name != null) {
            titleView.setText(name + "(" + identifier + ")(enum)");
            titleView.setVisibility(View.VISIBLE);
        } else {
            titleView.setVisibility(View.GONE);
        }
        Spinner spinner = view.findViewById(R.id.spinner);
        Map<String, String> specs = (Map<String, String>) dataType.get("specs");
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(requireContext(), R.layout.layout_spinner_field_item, specs.values().toArray(new String[]{}));
        spinner.setAdapter(adapter);
        container.addView(view);
        int i = 0;
        String key = identifier != null ? identifier : rootIdentifier;
        while (mWidgetMap.get(key + i) != null) {
            i++;
        }
        mWidgetMap.put(key + i, spinner);
    }

    private void buildTextField(String identifier, String name, Map dataType, ViewGroup container, String rootIdentifier) {
        View view = WidgetBuildUtils.buildTextField(requireContext());
        TextView titleView = view.findViewById(R.id.title);
        if (identifier != null && name != null) {
            titleView.setText(name + "(" + identifier + ")(string)");
            titleView.setVisibility(View.VISIBLE);
        } else {
            titleView.setVisibility(View.GONE);
        }
        EditText editText = view.findViewById(R.id.edit_text);
        Map specs = (Map) dataType.get("specs");
        int length = Integer.parseInt((String) specs.get("length"));
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(length)});
        editText.setHint("不超过" + length + "个字符");
        container.addView(view);
        int i = 0;
        String key = identifier != null ? identifier : rootIdentifier;
        while (mWidgetMap.get(key + i) != null) {
            i++;
        }
        mWidgetMap.put(key + i, editText);
    }

    @Override
    public void onClick(View v) {
        int position = getBinding().eventSpinner.getSelectedItemPosition();
        Map event = mEvents.get(position);
        String eventIdentifier = (String) event.get("identifier");
        List<Map> outputDatas = (List<Map>) event.get("outputData");

        Map params = new HashMap();
        for (Map map : outputDatas) {
            String identifier = (String) map.get("identifier");
            Object value = getEventValue(map, 0, identifier);
            if (value != null) {
                Map param = new HashMap();
                Map valueMap = new HashMap();
                valueMap.put(identifier, value);
                param.put("value", valueMap);
                param.put("time", System.currentTimeMillis());
                params.put(eventIdentifier, param);
            } else {
                Toast.makeText(requireContext(), "请完整填写事件功能点参数值", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        MqttUtils.mqttClient.postEvents(System.currentTimeMillis() + "", params);
    }

    private Object getEventValue(Map map, int index, String rootIdentifier) {
        String identifier = (String) map.get("identifier");
        Map dataType;
        String type;
        if (identifier != null) {
            dataType = (Map) map.get("dataType");
        } else {
            identifier = rootIdentifier;
            dataType = map;
        }
        type = (String) dataType.get("type");
        switch (type) {
            case "int32": {
                EditText editText = (EditText) mWidgetMap.get(identifier + index);
                if (editText == null) {
                    return null;
                }
                String value = editText.getText().toString();
                if (!value.trim().isEmpty()) {
                    return Integer.parseInt(value);
                }
                break;
            }
            case "int64": {
                EditText editText = (EditText) mWidgetMap.get(identifier + index);
                if (editText == null) {
                    return null;
                }
                String value = editText.getText().toString();
                if (!value.trim().isEmpty()) {
                    return Long.parseLong(value);
                }
                break;
            }
            case "float": {
                EditText editText = (EditText) mWidgetMap.get(identifier + index);
                if (editText == null) {
                    return null;
                }
                String value = editText.getText().toString();
                if (!value.trim().isEmpty()) {
                    return Float.parseFloat(value);
                }
                break;
            }
            case "double": {
                EditText editText = (EditText) mWidgetMap.get(identifier + index);
                if (editText == null) {
                    return null;
                }
                String value = editText.getText().toString();
                if (!value.trim().isEmpty()) {
                    return Double.parseDouble(value);
                }
                break;
            }
            case "bitMap": {
                EditText editText = (EditText) mWidgetMap.get(identifier + index);
                if (editText == null) {
                    return null;
                }
                String value = editText.getText().toString();
                if (!value.trim().isEmpty()) {
                    return Integer.parseInt(value, 2);
                }
                break;
            }
            case "enum": {
                Spinner spinner = (Spinner) mWidgetMap.get(identifier + index);
                if (spinner == null) {
                    return null;
                }
                int position = spinner.getSelectedItemPosition();
                Map specs = (Map) dataType.get("specs");
                Object[] keys = specs.keySet().toArray(new String[]{});
                return Integer.valueOf((String) keys[position]);
            }
            case "bool": {
                Spinner spinner = (Spinner) mWidgetMap.get(identifier + index);
                if (spinner == null) {
                    return null;
                }
                int position = spinner.getSelectedItemPosition();
                return 0 == position;
            }
            case "string": {
                EditText editText = (EditText) mWidgetMap.get(identifier + index);
                if (editText == null) {
                    return null;
                }
                String value = editText.getText().toString();
                if (!value.trim().isEmpty()) {
                    return value;
                }
                break;
            }
            case "struct": {
                Map value = new HashMap();
                List<Map> specs = (List<Map>) dataType.get("specs");
                for (Map spec : specs) {
                    Object childObject = getEventValue(spec, index, rootIdentifier);
                    if (childObject != null) {
                        value.put(spec.get("identifier"), childObject);
                    }
                }
                return !value.isEmpty() ? value : null;
            }
            case "array": {
                List<Map> values = new ArrayList<Map>();
                Map outerSpecs = (Map) dataType.get("specs");
                Object innerSpecs = ((Map) outerSpecs).get("specs");
                int i = 0;
                if (innerSpecs instanceof List) {
                    while (true) {
                        Map value = new HashMap();
                        for (Map spec : (List<Map>) innerSpecs) {
                            Object childObject = getEventValue(spec, i, rootIdentifier);
                            if (childObject != null) {
                                value.put(spec.get("identifier"), childObject);
                            }
                        }
                        if (!value.isEmpty()) {
                            values.add(value);
                            i++;
                        } else {
                            break;
                        }
                    }
                } else {
                    while (true) {
                        Map value = new HashMap();
                        Object childObject = getEventValue(outerSpecs, i, rootIdentifier);
                        if (childObject != null) {
                            value.put(((Map) innerSpecs).get("identifier"), childObject);
                        }
                        if (!value.isEmpty()) {
                            values.add(value);
                            i++;
                        } else {
                            break;
                        }
                    }
                }
                return !values.isEmpty() ? values : null;
            }
        }
        return null;
    }
}
