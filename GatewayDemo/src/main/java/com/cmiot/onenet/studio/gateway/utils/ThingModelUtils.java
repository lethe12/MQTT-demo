package com.cmiot.onenet.studio.gateway.utils;

import android.content.Context;

import com.cmiot.onenet.studio.mqtt.processor.ThingModelObject;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ThingModelUtils {

    public static String readThingModel(Context context, String fileName) {
        StringBuilder contentBuilder = new StringBuilder();
        BufferedReader br = null;
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            br = new BufferedReader(new InputStreamReader(inputStream));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                contentBuilder.append(sCurrentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return contentBuilder.toString();
    }

    public static ThingModelObject parseThingModel(String json) {
        Gson gson = new Gson();
        ThingModelObject thingModelObject = gson.fromJson(json, ThingModelObject.class);
        return thingModelObject;
    }

}
