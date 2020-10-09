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
import com.codename1.io.Log;
import com.codename1.io.MultipartRequest;
import com.codename1.io.Preferences;
import com.codename1.io.rest.Rest;
import com.codename1.properties.PreferencesObject;
import com.codename1.social.LoginCallback;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.Image;
import com.codename1.util.Callback;
import com.codename1.util.FailureCallback;
import com.codename1.util.SuccessCallback;
import com.mykovol.takeandcharge.dataobj.*;
import com.mykovol.takeandcharge.form.MainForm;
import com.mykovol.takeandcharge.form.component.PhoneFieldContainer;
import com.mykovol.takeandcharge.tools.CommonCode;
import com.mykovol.takeandcharge.tools.InfinityProgressBlocking;

import java.io.IOException;
import java.util.List;

import static com.codename1.ui.CN.addToQueue;
import static com.codename1.ui.CN.callSerially;
import static com.mykovol.takeandcharge.service.GlobalConst.*;

/**
 * A generic service class that handles login/creation etc.
 *
 * @author Vlad Mykol
 */
public class UserService {
    private static final String TOKEN_PROP_NAME = "token1";
    private static User me = new User();

    public static User getUser() {
        PreferencesObject.create(me).bind();
        return me;
    }

    public static String getToken() {
        return Preferences.get(TOKEN_PROP_NAME, null);
    }

    private static void setToken(String token) {
        Preferences.set(TOKEN_PROP_NAME, token);
    }

    public static void loadUser() {
        me = new User();
        if (Display.getInstance().isSimulator()) {
            Log.p("User details: " + me.getPropertyIndex().toString());
        }
    }

    public static void logout() {
        Preferences.set(TOKEN_PROP_NAME, null);

        callSerially(() -> {
            CommonCode.refreshMenuItems();
            MainForm.get().removeAllRentRows();
            MainForm.get().refreshScanButton();
        });
    }

    public static boolean isLoggedIn() {
        return getToken() != null;
    }

    public static void validateUserPhone(String phoneNumber, final Callback<RegisterInitResponse> callback) {
        Rest.post(GlobalConst.getServerUrl() + API_REGISTER_INIT)
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

    public static void registerUser(User request, final Callback<String> callback) {
        Rest.post(GlobalConst.getServerUrl() + API_REGISTER)
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
                    Preferences.set("phoneNumber", PhoneFieldContainer.formattedPhoneNumber(request.name.get()));
                    String token = resp.getResponseData().get("token").toString();
                    setToken(token);
                    MainForm.get().refreshScanButton();
                    CommonCode.refreshMenuItems();

                    callback.onSucess(null);
                });
    }

    public static boolean validateSMSActivationCode(String code) {
        String val = Preferences.get("phoneVerification", null);
        return code.contains(val) && code.length() < 80;
    }

    public static void checkForNewVersion() {
        Rest.get(GlobalConst.getServerUrl() + API_APP_VERSION)
                .jsonContent()
                .acceptJson()
                .timeout(5000)
                .queryParam("os", Display.getInstance().getPlatformName())
                .queryParam("currentVersion", Display.getInstance().getProperty("AppVersion", "0.1"))
                .onErrorCode(errorData -> {
                    System.out.println("New app version error - " + errorData.getResponseCode());
                }, ErrorResponse.class)
                .fetchAsString(link -> {
                    System.out.println("New app version is here");
                    if (Dialog.show("New version available", "Update to latest version and get new feature and improvements", "Update", "Later")) {
                        Display.getInstance().execute(link.getResponseData());
                    }
                });
    }


    public static void fetchUserCards(final Callback<List<UserCardResponse>> callback) {
        Rest.get(GlobalConst.getServerUrl() + API_APP_USER_CARD)
                .bearer(UserService.getToken())
                .jsonContent()
                .acceptJson()
                .timeout(5000)
                .onErrorCode(errorData -> {
                    ErrorResponse responseData = (ErrorResponse) (errorData.getResponseData());
                    callback.onError(null, null, errorData.getResponseCode(), responseData.message.get());
                }, ErrorResponse.class)
                .fetchAsPropertyList(resp -> {
                    List<UserCardResponse> responseData = (List<UserCardResponse>) (List<?>) resp.getResponseData();
                    callback.onSucess(responseData);
                }, UserCardResponse.class);
    }

    public static void removeUserCard(String id, final FailureCallback<String> errorCallback) {
        Rest.delete(GlobalConst.getServerUrl() + API_APP_USER_CARD + "/{id}")
                .bearer(UserService.getToken())
                .pathParam("id", id)
                .jsonContent()
                .acceptJson()
                .timeout(5000)
                .onErrorCode(errorData -> {
                    ErrorResponse responseData = (ErrorResponse) (errorData.getResponseData());
                    errorCallback.onError(null, null, errorData.getResponseCode(), responseData.message.get());
                }, ErrorResponse.class)
                .fetchAsString(v -> {
                });
    }


    public static void login(PhoneFieldContainer phoneFieldContainer, String password, final LoginCallback callback) {
        Rest.post(GlobalConst.getServerUrl() + API_LOGIN)
                .jsonContent()
                .acceptJson()
                .timeout(5000)
                .body(new UserLogin().username.set(phoneFieldContainer.getFullPhoneNumber()).password.set(password))
                .onErrorCode(errorData -> {
                    ErrorResponse responseData = (ErrorResponse) (errorData.getResponseData());
                    callback.loginFailed(responseData.message.get());
                }, ErrorResponse.class)
                .fetchAsJsonMap(resp -> {
                    Preferences.set("phoneNumber", phoneFieldContainer.getFormattedPhoneNumber());
                    String token = resp.getResponseData().get("token").toString();
                    setToken(token);
                    MainForm.get().refreshScanButton();
                    MainForm.get().refreshRentContent();
                    CommonCode.refreshMenuItems();
                    callback.loginSuccessful();
                })
                .setDisposeOnCompletion(InfinityProgressBlocking.get());
    }

    public static void fetchAvatar(long id, SuccessCallback<Image> callback) {
//        ConnectionRequest cr = new ConnectionRequest(GlobalConst.getServerUrl() + "/user/avatar/" + id, false);
//        cr.setFailSilently(true);
//        cr.downloadImageToStorage("avatarImage-" + id, callback);
    }


    public static void setAvatar(String imageFile) {
        try {
            MultipartRequest mp = new MultipartRequest();
            mp.setUrl(GlobalConst.getServerUrl() + "user/updateAvatar/" + getToken());
            mp.addData("img", imageFile, "image/jpeg");
            addToQueue(mp);
        } catch (IOException err) {
            Log.e(err);
            ToastBar.showErrorMessage("Error uploading avatar file: " + err);
        }
    }


}
