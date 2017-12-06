package com.ievolutioned.auria.net.service;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.ievolutioned.auria.entity.UserEntity;
import com.ievolutioned.auria.net.HttpGetParam;
import com.ievolutioned.auria.net.HttpHeader;
import com.ievolutioned.auria.net.NetResponse;
import com.ievolutioned.auria.net.NetUtil;
import com.ievolutioned.auria.util.LogUtil;

/**
 * Manages the log in / out services for the user on the system
 * Created by Daniel on 20/03/2015.
 */
public class LoginService extends ServiceBase {

    private static final String TAG = LoginService.class.getName();

    /**
     * Admin token for log in.
     */
    private static final String adminToken = "nosession";

    /**
     * Instantiates a LoginService with the current parameters
     *
     * @param deviceId - The device id
     */
    public LoginService(String deviceId) {
        super(deviceId, adminToken);
    }

    /**
     * Logs the user in the system
     *
     * @param id       - The id of the user
     * @param pass     - The password
     * @param callback a LoginHandler callback handler
     */
    public void logIn(final String id, final String pass, final LoginHandler callback) {
        task = new AsyncTask<Void, Void, ResponseBase>() {
            @Override
            protected LoginResponse doInBackground(Void... voids) {
                if (isCancelled())
                    return null;
                try {
                    if (deviceId == null) {
                        callback.onError(new LoginResponse(false, null, "Device id is null", null));
                        this.cancel(true);
                    }
                    HttpGetParam params = new HttpGetParam();
                    params.add("iac_id", id);
                    params.add("password", pass);

                    HttpHeader headers = getHeaders(ACTION_LOGIN, CONTROLLER_LOGIN);

                    // Get response
                    NetResponse response = NetUtil.get(URL_LOGIN, params, headers);
                    if (response == null)
                        return new LoginResponse(false, null, "No response", null);
                    if (response.isBadStatus())
                        return new LoginResponse(false, null, response.toString(), null);

                    LogUtil.d(LoginService.class.getName(), response.result);

                    //Parse response
                    Gson g = new Gson();
                    UserEntity user = g.fromJson(response.result, UserEntity.class);
                    if (user.getIacId() != null)
                        return new LoginResponse(true, user, response.result, null);
                    return new LoginResponse(false, null, null, null);
                } catch (Exception e) {
                    LogUtil.e(TAG, e.getMessage(), e);
                    return new LoginResponse(false, null, e.getMessage(), e);
                }
            }

            @Override
            protected void onPostExecute(ResponseBase response) {
                hanldeResult(callback, (LoginResponse) response);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                callback.onCancel();
            }
        };
        task.execute();
    }

    /**
     * Logs the user out of system
     *
     * @param token    - The token
     * @param callback a LoginHandler callback handler
     */
    public void logOut(final String token, final LoginHandler callback) {
        task = new AsyncTask<Void, Void, ResponseBase>() {
            @Override
            protected LoginResponse doInBackground(Void... voids) {
                if (isCancelled())
                    return null;
                try {
                    return new LoginResponse(false, null, null, null);
                } catch (Exception e) {
                    return new LoginResponse(true, null, e.getMessage(), e);
                }
            }

            @Override
            protected void onPostExecute(ResponseBase response) {
                hanldeResult(callback, (LoginResponse) response);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                callback.onCancel();
            }
        };
        task.execute();
    }

    /**
     * Cancels the request
     *
     * @param callback a LoginHandler callback handler
     */
    public void cancel(final LoginHandler callback) {
        if (task != null)
            task.cancel(true);
        callback.onCancel();
    }

    /**
     * Handles the result
     *
     * @param callback a LoginHandler callback handler
     * @param response the current LoginResponse
     */
    protected void hanldeResult(final LoginHandler callback, final LoginResponse response) {
        if (response == null)
            callback.onError(new LoginResponse(false, null, "Service error", new RuntimeException()));
        else if (response.e != null || !response.logged)
            callback.onError(response);
        else
            callback.onSuccess(response);
    }

    /**
     * Log in/out handler
     */
    public interface LoginHandler {
        public void onSuccess(final LoginResponse response);

        public void onError(final LoginResponse response);

        public void onCancel();
    }

    /**
     * Login response class, manages the response from service
     */
    public class LoginResponse extends ResponseBase {
        public boolean logged;
        public UserEntity user;

        public LoginResponse(final boolean logged, final UserEntity user, final String msg, final Throwable e) {
            super(msg, e);
            this.logged = logged;
            this.user = user;
        }
    }
}
