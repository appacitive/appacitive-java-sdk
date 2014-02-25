package com.appacitive.sdk;

import com.appacitive.sdk.exceptions.AppacitiveException;
import com.appacitive.sdk.infra.APContainer;
import com.appacitive.sdk.infra.Headers;
import com.appacitive.sdk.infra.Urls;
import com.appacitive.sdk.interfaces.Http;
import com.appacitive.sdk.model.AppacitiveStatus;
import com.appacitive.sdk.model.Callback;
import com.appacitive.sdk.push.AndroidOptions;
import com.appacitive.sdk.push.IosOptions;
import com.appacitive.sdk.push.PlatformOptions;
import com.appacitive.sdk.push.WindowsPhoneOptions;
import com.appacitive.sdk.query.BooleanOperator;
import com.appacitive.sdk.query.Filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* Created by sathley.
*/
public class AppacitivePushNotification implements Serializable {

    public final static Logger LOGGER = Logger.getLogger(AppacitivePushNotification.class.getName());

    private Map<String, Object> getMap() {

        Map<String, Object> nativeMap = new HashMap<String, Object>();
        nativeMap.put("broadcast", this.isBroadcast);
        if (this.deviceIds.size() > 0)
            nativeMap.put("deviceids", this.deviceIds);
        if (this.channels.size() > 0)
            nativeMap.put("channels", this.channels);
        if (this.query != null)
            nativeMap.put("query", this.query);
        if (this.expiryInSeconds > 0)
            nativeMap.put("expireafter", this.expiryInSeconds);

        // write data
        if (this.alert != null && this.alert.isEmpty() == false)
            this.data.put("alert", this.alert);

        if (this.badge != null && this.badge.isEmpty() == false)
            this.data.put("badge", this.badge);

        nativeMap.put("data", this.data);

        // write platform options
        if (this.platformOptions != null) {
            nativeMap.put("platformoptions", this.platformOptions.getMap());
        }

        return nativeMap;
    }

    public static AppacitivePushNotification Broadcast(String message) {
        return new AppacitivePushNotification(message, true, null, null, null);
    }

    public static AppacitivePushNotification ToChannels(String message, List<String> channels) {
        return new AppacitivePushNotification(message, false, channels, null, null);
    }

    public static AppacitivePushNotification ToQueryResult(String message, BooleanOperator booleanOperator) {
        if (booleanOperator != null) {
            return new AppacitivePushNotification(message, false, null, null, booleanOperator.asString());
        }
        return new AppacitivePushNotification(message, false, null, null, null);
    }

    public static AppacitivePushNotification ToQueryResult(String message, Filter filter) {
        if (filter != null) {
            return new AppacitivePushNotification(message, false, null, null, filter.asString());
        }
        return new AppacitivePushNotification(message, false, null, null, null);
    }

    public static AppacitivePushNotification ToDeviceIds(String message, List<String> deviceIds) {
        return new AppacitivePushNotification(message, false, null, deviceIds, null);
    }

    private AppacitivePushNotification(String alert, boolean isBroadcast, List<String> channels, List<String> deviceIds, String query) {
        this.alert = alert;
        this.isBroadcast = isBroadcast;
        this.query = query;
        if (channels != null)
            this.channels.addAll(channels);
        if (deviceIds != null)
            this.deviceIds.addAll(deviceIds);
        this.expiryInSeconds = -1;
    }

    public String alert;

    public String badge;

    public boolean isBroadcast = false;

    public String query;

    public int expiryInSeconds;

    private List<String> deviceIds = new ArrayList<String>();

    private List<String> channels = new ArrayList<String>();

    public Map<String, String> data = new HashMap<String, String>();

    public PlatformOptions platformOptions;

    public AppacitivePushNotification withBadge(String badge) {
        this.badge = badge;
        return this;
    }

    public AppacitivePushNotification withPlatformOptions(IosOptions options) {
        if (this.platformOptions == null)
            this.platformOptions = new PlatformOptions();
        this.platformOptions.iOS = options;
        return this;
    }

    public AppacitivePushNotification withPlatformOptions(AndroidOptions options) {
        if (this.platformOptions == null)
            this.platformOptions = new PlatformOptions();
        this.platformOptions.android = options;
        return this;
    }

    public AppacitivePushNotification withPlatformOptions(WindowsPhoneOptions options) {
        if (this.platformOptions == null)
            this.platformOptions = new PlatformOptions();
        this.platformOptions.windowsPhone = options;
        return this;
    }

    public AppacitivePushNotification withData(Map<String, String> data) {
        this.data.putAll(data);
        return this;
    }

    public AppacitivePushNotification withExpiry(int seconds) {
        if (seconds <= 0)
            throw new IllegalArgumentException("Expiry time cannot be less than or equal to zero.");
        this.expiryInSeconds = seconds;
        return this;
    }

    public void sendInBackground(Callback<String> callback) {
        final String url = Urls.Misc.sendPushUrl().toString();
        final Map<String, String> headers = Headers.assemble();
        final Map<String, Object> payload = this.getMap();
        Future<Map<String, Object>> future = ExecutorServiceWrapper.submit(new Callable<Map<String, Object>>() {
            @Override
            public Map<String, Object> call() throws Exception {
                return APContainer.build(Http.class).post(url, headers, payload);
            }
        });
        boolean isSuccessful;
        AppacitiveStatus status;
        String id = null;
        try {
            Map<String, Object> responseMap = future.get();
            status = new AppacitiveStatus((Map<String, Object>) responseMap.get("status"));
            isSuccessful = status.isSuccessful();
            if (isSuccessful && callback != null) {
                id = (String) responseMap.get("id");
            }

        } catch (Exception e) {
            LOGGER.log(Level.ALL, e.getMessage());
            if (callback != null) callback.failure(null, e);
            return;
        }
        if (callback != null) {
            if (isSuccessful)
                callback.success(id);
            else
                callback.failure(null, new AppacitiveException(status));
        }


    }
}


