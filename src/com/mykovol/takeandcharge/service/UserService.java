/*
 * Copyright (c) 2012, Codename One and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Codename One designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Codename One through http://www.codenameone.com/ if you
 * need additional information or have any questions.
 */

package com.mykovol.takeandcharge.service;

import com.codename1.components.ToastBar;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.Log;
import com.codename1.io.MultipartRequest;
import com.codename1.io.Preferences;
import com.codename1.io.rest.Response;
import com.codename1.io.rest.Rest;
import com.codename1.properties.PreferencesObject;
import com.codename1.social.LoginCallback;
import com.codename1.ui.Display;
import com.codename1.ui.Image;
import com.codename1.util.Callback;
import com.codename1.util.FailureCallback;
import com.codename1.util.SuccessCallback;
import com.mykovol.takeandcharge.dataobj.*;
import com.mykovol.takeandcharge.form.MainForm;

import java.io.IOException;

import static com.codename1.ui.CN.addToQueue;
import static com.mykovol.takeandcharge.service.GlobalConst.*;

/**
 * A generic service class that handles login/creation etc.
 *
 * @author Shai Almog
 */
public class UserService {
    private static User me;

    public static User getUser() {
        return me;
    }

    public static String getToken() {
        return Preferences.get("token", null);
    }

    private static void setToken(String token) {
        Preferences.set("token", token);
    }

    public static void loadUser() {
        me = new User();
        PreferencesObject.create(me).bind();
        if (Display.getInstance().isSimulator()) {
            Log.p("User details: " + me.getPropertyIndex().toString());
        }
    }

    public static void logout() {
        Preferences.set("token", null);
        RentSocketService.get().close();
        MainForm.get().getBottomPanel().refreshRentContent();
    }

    public static boolean isLoggedIn() {
        return getToken() != null;
    }

    public static void validateUserPhone(String phoneNumber, final Callback<RegisterInitResponse> callback) {
        Rest.post(API_REGISTER_INIT)
//                .bearer(UserService.getToken())
                .queryParam("phone", phoneNumber)
                .timeout(10000)
                .acceptJson()
                .onErrorCode(errorData -> {
                    ErrorResponse responseData = (ErrorResponse) (errorData.getResponseData());
                    callback.onError(null, null, errorData.getResponseCode(), responseData.message.get());
                }, ErrorResponse.class)
                .fetchAsProperties(resp -> {
                    callback.onSucess((RegisterInitResponse) resp.getResponseData());
                }, RegisterInitResponse.class);
    }

    public static void registerUser(User request, final Callback<String>  callback) {
        Rest.post(API_REGISTER)
//                .bearer(UserService.getToken())
                .acceptJson()
                .timeout(10000)
                .jsonContent()
                .body(request)
                .onErrorCode(errorData -> {
                    ErrorResponse responseData = (ErrorResponse) (errorData.getResponseData());
                    callback.onError(null, null, errorData.getResponseCode(), responseData.message.get());
                }, ErrorResponse.class)
                .fetchAsJsonMap(resp -> {
                    String token = resp.getResponseData().get("token").toString();
                    setToken(token);
                    callback.onSucess(null);
                });
    }

    public static boolean validateSMSActivationCode(String code) {
        String val = Preferences.get("phoneVerification", null);
        return code.contains(val) && code.length() < 80;
    }


    public static void login(String username, String password, final LoginCallback callback) {
        Rest.post(API_LOGIN)
                .jsonContent()
                .acceptJson()
                .timeout(10000)
                .body(new UserLogin().username.set(username).password.set(password))
                .onErrorCode(errorData -> {
                    ErrorResponse responseData = (ErrorResponse) (errorData.getResponseData());
                    callback.loginFailed(responseData.message.get());
                }, ErrorResponse.class)
                .fetchAsJsonMap(resp -> {
                    String token = resp.getResponseData().get("token").toString();
                    setToken(token);
                    callback.loginSuccessful();
                });
    }

    public static void fetchAvatar(long id, SuccessCallback<Image> callback) {
        ConnectionRequest cr = new ConnectionRequest(SERVER_URL + "user/avatar/" + id, false);
        cr.setFailSilently(true);
        cr.downloadImageToStorage("avatarImage-" + id, callback);
    }

    public static void setAvatar(String imageFile) {
        try {
            MultipartRequest mp = new MultipartRequest();
            mp.setUrl(SERVER_URL + "user/updateAvatar/" + getToken());
            mp.addData("img", imageFile, "image/jpeg");
            addToQueue(mp);
        } catch (IOException err) {
            Log.e(err);
            ToastBar.showErrorMessage("Error uploading avatar file: " + err);
        }
    }


}
