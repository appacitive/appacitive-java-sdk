package com.appacitive.sdk.core;

import com.appacitive.sdk.core.model.Callback;
import com.appacitive.sdk.core.exceptions.AppacitiveException;
import com.appacitive.sdk.core.exceptions.ValidationException;
import com.appacitive.sdk.core.infra.APSerializable;
import com.appacitive.sdk.core.infra.AppacitiveHttp;
import com.appacitive.sdk.core.infra.Headers;
import com.appacitive.sdk.core.infra.Urls;
import com.appacitive.sdk.core.model.AppacitiveStatus;
import com.appacitive.sdk.core.model.PagedList;
import com.appacitive.sdk.core.query.AppacitiveQuery;

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
public class AppacitiveDevice extends AppacitiveEntity implements Serializable, APSerializable {

    public final static Logger LOGGER = Logger.getLogger(AppacitiveDevice.class.getName());

    public AppacitiveDevice(Map<String, Object> device) {
        this.setSelf(device);
    }

    public AppacitiveDevice(){}

    public void setSelf(Map<String, Object> device) {

        super.setSelf(device);

        if (device != null) {

            Object object = device.get("__typeid");
            if(object != null)
                this.typeId = Long.parseLong(object.toString());

            object = device.get("__type");
            if(object != null)
                this.type = object.toString();

        }
    }

    public Map<String, Object> getMap() {
        Map<String, Object> nativeMap = super.getMap();
        nativeMap.put("__type", this.type);
        nativeMap.put("__typeid", String.valueOf(this.typeId));

        return nativeMap;
    }

    public String type = null;

    public long typeId = 0;

    public String getDeviceType() {
        return this.getProperty("devicetype");
    }

    public void setDeviceType(String deviceType) {
        this.setProperty("devicetype", deviceType);

    }

    public String getDeviceToken() {
        return this.getProperty("devicetoken");
    }

    public void setDeviceToken(String deviceToken) {
        this.setProperty("devicetoken", deviceToken);

    }

    public double[] getLocation() {
        return this.getPropertyAsGeo("location");
    }

    public void setLocation(double[] coordinates) {
        this.setPropertyAsGeo("location", coordinates);

    }

    public List<String> getChannels() {
        return this.getPropertyAsMultiValued("channels");
    }

    public void setChannels(List<String> channels) {
        this.setProperty("channels", channels);

    }

    public int getBadge() {
        return this.getPropertyAsInt("badge");
    }

    public void setBadge(int badge) {
        this.setProperty("badge", badge);

    }

    public String getTimeZone() {
        return this.getProperty("timezone");
    }

    public void setTimeZone(String timezone) {
        this.setProperty("timezone", timezone);

    }

    public boolean getIsActive() {
        return this.getPropertyAsBoolean("isactive");
    }

    public void setIsActive(boolean isActive) {
        this.setProperty("isactive", isActive);

    }

    public void registerInBackground(Callback<AppacitiveDevice> callback) throws ValidationException
    {
        final List<String> mandatoryFields = new ArrayList<String>() {{
            add("devicetype");
            add("devicetoken");
        }};
        List<String> missingFields = new ArrayList<String>();
        for (String field : mandatoryFields) {
            if (this.getProperty(field) == null) {
                missingFields.add(field);
            }
        }

        if (missingFields.size() > 0)
            throw new ValidationException("Following mandatory fields are missing. - " + missingFields);

        final String url = Urls.ForDevice.getRegisterUrl().toString();
        final Map<String, String> headers = Headers.assemble();
        final Map<String, Object> payload = this.getMap();
        Future<Map<String, Object>> future = ExecutorServiceWrapper.submit(new Callable<Map<String, Object>>() {
            @Override
            public Map<String, Object> call() throws Exception {
                return AppacitiveHttp.put(url, headers, payload);
            }
        });
        try {
            Map<String, Object> responseMap = future.get();
            AppacitiveStatus status = new AppacitiveStatus((Map<String, Object>) responseMap.get("status"));
            if (status.isSuccessful()) {
                this.setSelf((Map<String, Object>) responseMap.get("device"));
                this.resetUpdateCommands();
                if (callback != null)
                    callback.success(this);
            } else {
                if (callback != null)
                    callback.failure(null, new AppacitiveException(status));
            }

        } catch (Exception e) {
            LOGGER.log(Level.ALL, e.getMessage());
            callback.failure(null, e);
        }
    }

    public static void getInBackground(long deviceId, Callback<AppacitiveDevice> callback) throws ValidationException {

        final String url = Urls.ForDevice.getDeviceUrl(String.valueOf(deviceId)).toString();
        final Map<String, String> headers = Headers.assemble();

        Future<Map<String, Object>> future = ExecutorServiceWrapper.submit(new Callable<Map<String, Object>>() {
            @Override
            public Map<String, Object> call() throws Exception {
                return AppacitiveHttp.get(url, headers);
            }
        });

        try {
            Map<String, Object> responseMap = future.get();
            AppacitiveStatus status = new AppacitiveStatus((Map<String, Object>) responseMap.get("status"));
            if (status.isSuccessful()) {
                if (callback != null)
                    callback.success(new AppacitiveDevice((Map<String, Object>) responseMap.get("device")));
            } else {
                if (callback != null)
                    callback.failure(null, new AppacitiveException(status));
            }

        } catch (Exception e) {
            LOGGER.log(Level.ALL, e.getMessage());
            callback.failure(null, e);
        }
    }

    public void fetchLatestInBackground(Callback<Void> callback) {
        final String url = Urls.ForDevice.getDeviceUrl(String.valueOf(this.getId())).toString();
        final Map<String, String> headers = Headers.assemble();

        Future<Map<String, Object>> future = ExecutorServiceWrapper.submit(new Callable<Map<String, Object>>() {
            @Override
            public Map<String, Object> call() throws Exception {
                return AppacitiveHttp.get(url, headers);
            }
        });

        try {
            Map<String, Object> responseMap = future.get();
            AppacitiveStatus status = new AppacitiveStatus((Map<String, Object>) responseMap.get("status"));
            if (status.isSuccessful()) {
                this.setSelf((Map<String, Object>) responseMap.get("device"));
                this.resetUpdateCommands();
                if (callback != null)
                    callback.success(null);
            } else {
                if (callback != null)
                    callback.failure(null, new AppacitiveException(status));
            }

        } catch (Exception e) {
            LOGGER.log(Level.ALL, e.getMessage());
            callback.failure(null, e);
        }
    }

    public static void multiGetInBackground(List<Long> ids, List<String> fields, Callback<List<AppacitiveDevice>> callback) throws ValidationException {

        final String url = Urls.ForObject.multiGetObjectUrl("device", ids, fields).toString();
        final Map<String, String> headers = Headers.assemble();
        Future<Map<String, Object>> future = ExecutorServiceWrapper.submit(new Callable<Map<String, Object>>() {
            @Override
            public Map<String, Object> call() throws Exception {
                return AppacitiveHttp.get(url, headers);
            }
        });

        try {
            Map<String, Object> responseMap = future.get();
            AppacitiveStatus status = new AppacitiveStatus((Map<String, Object>) responseMap.get("status"));
            if (status.isSuccessful()) {
                ArrayList<Object> objects = (ArrayList<Object>) responseMap.get("objects");
                List<AppacitiveDevice> returnDevices = new ArrayList<AppacitiveDevice>();
                for (Object device : objects) {
                    returnDevices.add(new AppacitiveDevice((Map<String, Object>) device));
                }
                callback.success(returnDevices);
            } else {
                if (callback != null)
                    callback.failure(null, new AppacitiveException(status));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ALL, e.getMessage());
            callback.failure(null, e);
        }
    }

    public void updateInBackground(boolean withRevision, Callback<AppacitiveDevice> callback) {

        final String url = Urls.ForDevice.updateDeviceUrl(this.getId(), withRevision, this.getRevision()).toString();
        final Map<String, String> headers = Headers.assemble();
        final Map<String, Object> payload = new HashMap<String, Object>();
        payload.putAll(super.getUpdateCommand());
        Future<Map<String, Object>> future = ExecutorServiceWrapper.submit(new Callable<Map<String, Object>>() {
            @Override
            public Map<String, Object> call() throws Exception {
                return AppacitiveHttp.post(url, headers, payload);
            }
        });

        try {
            Map<String, Object> responseMap = future.get();
            AppacitiveStatus status = new AppacitiveStatus((Map<String, Object>) responseMap.get("status"));
            if (status.isSuccessful()) {
                this.resetUpdateCommands();
                this.setSelf((Map<String, Object>)responseMap.get("object"));
                if (callback != null)
                    callback.success(this);
            } else {
                if (callback != null)
                    callback.failure(null, new AppacitiveException(status));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ALL, e.getMessage());
            callback.failure(null, e);
        }
    }

    public void deleteInBackground(boolean deleteConnections, Callback<Void> callback) {
        final String url = Urls.ForDevice.deleteDeviceUrl(this.getId(), deleteConnections).toString();
        final Map<String, String> headers = Headers.assemble();
        Future<Map<String, Object>> future = ExecutorServiceWrapper.submit(new Callable<Map<String, Object>>() {
            @Override
            public Map<String, Object> call() throws Exception {
                return AppacitiveHttp.delete(url, headers);
            }
        });

        try {
            Map<String, Object> responseMap = future.get();
            AppacitiveStatus status = new AppacitiveStatus((Map<String, Object>) responseMap.get("status"));
            if (status.isSuccessful()) {
                if (callback != null)
                    callback.success(null);
            } else {
                if (callback != null)
                    callback.failure(null, new AppacitiveException(status));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ALL, e.getMessage());
            callback.failure(null, e);
        }
    }

    public static void findInBackground(AppacitiveQuery query, List<String> fields, Callback<PagedList<AppacitiveDevice>> callback) {
        final String url = Urls.ForObject.findObjectsUrl("device", query, fields).toString();
        final Map<String, String> headers = Headers.assemble();

        Future<Map<String, Object>> future = ExecutorServiceWrapper.submit(new Callable<Map<String, Object>>() {
            @Override
            public Map<String, Object> call() throws Exception {
                return AppacitiveHttp.get(url, headers);
            }
        });

        try {
            Map<String, Object> responseMap = future.get();
            AppacitiveStatus status = new AppacitiveStatus((Map<String, Object>) responseMap.get("status"));
            if (status.isSuccessful()) {
                if (callback != null) {
                    ArrayList<Object> devices = (ArrayList<Object>) responseMap.get("objects");
                    List<AppacitiveDevice> returnDevices = new ArrayList<AppacitiveDevice>();
                    for (Object device : devices) {
                        returnDevices.add(new AppacitiveDevice((Map<String, Object>) device));
                    }
                    PagedList<AppacitiveDevice> pagedResult = new PagedList<AppacitiveDevice>((Map<String, Object>) responseMap.get("paginginfo"));
                    pagedResult.results = returnDevices;
                    callback.success(pagedResult);
                }

            } else {
                if (callback != null)
                    callback.failure(null, new AppacitiveException(status));
            }

        } catch (Exception e) {
            LOGGER.log(Level.ALL, e.getMessage());
            callback.failure(null, e);
        }
    }
}