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

import com.codename1.ext.codescan.CodeScanner;
import com.codename1.ext.codescan.ScanResult;
import com.codename1.io.Log;
import com.codename1.io.Preferences;
import com.codename1.io.rest.Rest;
import com.codename1.maps.Coord;
import com.codename1.ui.Display;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.BeforeRentInfo;
import com.mykovol.takeandcharge.dataobj.ErrorResponse;
import com.mykovol.takeandcharge.dataobj.RentHistory;
import com.mykovol.takeandcharge.dataobj.StationInfo;
import com.mykovol.takeandcharge.form.LoginForm;
import com.mykovol.takeandcharge.form.WalletForm;
import com.mykovol.takeandcharge.tools.MainNoBlockingLoader;
import org.littlemonkey.qrscanner.QRScanner;

import java.util.List;

import static com.mykovol.takeandcharge.service.GlobalConst.*;

/**
 * A generic service class that handles login/creation etc.
 *
 * @author Vlad Mykol
 */
public class RentService {

    private static void getBeforeRentInfo(String stationId, final Callback<BeforeRentInfo> callback) {
        Rest.get(GlobalConst.getServerUrl() + RENT_URL)
                .bearer(UserService.getToken())
                .queryParam("stationId", stationId)
                .acceptJson()
                .timeout(60000)
                .onErrorCode(errorData -> {
                    // TODO: 5/27/2020 move to general error handler
                    if (errorData.getResponseCode() == 403 || errorData.getResponseCode() == 401) {
                        new LoginForm().show();
                        return;
                    }
                    if (errorData.getResponseCode() == 402) {
                        new WalletForm().show();
                        return;
                    }
                    ErrorResponse responseData = (ErrorResponse) (errorData.getResponseData());
                    callback.onError(null, null, errorData.getResponseCode(), responseData.message.get());
                }, ErrorResponse.class)
                .fetchAsProperties(resp -> {
                    callback.onSucess((BeforeRentInfo) resp.getResponseData());
                }, BeforeRentInfo.class);
    }

    public static void sendRentRequest(String stationId, final Callback<String> callback) {
        Rest.post(GlobalConst.getServerUrl() + RENT_URL)
                .bearer(UserService.getToken())
                .queryParam("stationId", stationId)
                .acceptJson()
                .timeout(60000)
                .onErrorCode(errorData -> {
                    // TODO: 5/27/2020 move to general error handler
                    if (errorData.getResponseCode() == 403 || errorData.getResponseCode() == 401) {
                        MainNoBlockingLoader.get().stop();
                        new LoginForm().show();
                        return;
                    }
                    ErrorResponse responseData = (ErrorResponse) (errorData.getResponseData());
                    callback.onError(null, null, errorData.getResponseCode(), responseData.message.get());
                }, ErrorResponse.class)
                .fetchAsString(resp -> {
                    callback.onSucess(resp.getResponseData());
                });
    }

    public static void getRentHistory(boolean onlyCurrentlyInRent, final Callback<List<RentHistory>> callback) {
        Rest.get(GlobalConst.getServerUrl() + RENT_HISTORY_URL)
                .bearer(UserService.getToken())
                .timeout(5000)
                .queryParam("onlyActive", String.valueOf(onlyCurrentlyInRent))
                .acceptJson()
                .onErrorCode(errorData -> {
                    ErrorResponse responseData = (ErrorResponse) (errorData.getResponseData());
                    callback.onError(null, null, errorData.getResponseCode(), responseData.message.get());
                }, ErrorResponse.class)
                .fetchAsPropertyList(historyList -> {
                    List<RentHistory> responseData = (List<RentHistory>) (List<?>) historyList.getResponseData();
                    callback.onSucess(responseData);
                }, RentHistory.class);

    }

    public static void getStationsNearBy(Coord coord, final Callback<List<StationInfo>> callback) {
        Rest.get(GlobalConst.getServerUrl() + STATIONS_NEARBY_URL)
//                .bearer(UserService.getToken())
                .timeout(5000)
                .queryParam("x", String.valueOf(coord.getLatitude()))
                .queryParam("y", String.valueOf(coord.getLongitude()))
                .acceptJson()
                .onErrorCode(errorData -> {
                    ErrorResponse responseData = (ErrorResponse) (errorData.getResponseData());
                    callback.onError(null, null, errorData.getResponseCode(), responseData.message.get());
                }, ErrorResponse.class)
                .fetchAsPropertyList(stationList -> {
                    List<StationInfo> responseData = (List<StationInfo>) (List<?>) stationList.getResponseData();
                    callback.onSucess(responseData);
                }, StationInfo.class);
    }

    public static void getRemainingPowerBanks(String stationId, final Callback<Integer> callback) {
        Rest.get(GlobalConst.getServerUrl() + STATIONS_CAPACITY_URL)
                .bearer(UserService.getToken())
                .timeout(5000)
                .pathParam("id", stationId)
                .acceptJson()
                .onErrorCode(errorData -> {
                    ErrorResponse responseData = (ErrorResponse) (errorData.getResponseData());
                    callback.onError(null, null, errorData.getResponseCode(), responseData.message.get());
                }, ErrorResponse.class)
                .fetchAsString(response -> {
                    int remainingPowerBanks = Integer.parseInt(response.getResponseData());
                    callback.onSucess(remainingPowerBanks);
                });
    }

    public static void prepareForRent(String predefinedStationId, final Callback<BeforeRentInfo> callback) {
        if (!UserService.isLoggedIn()) {
            new LoginForm().show();
            return;
        }

        if (Preferences.get("noPaymentMethod", "false").equals("true")) {
            new WalletForm().show();
            return;
        }

        if (predefinedStationId != null) {
            getBeforeRentInfo(predefinedStationId, callback);
        } else if (Display.getInstance().isSimulator()) {
            getBeforeRentInfo("https://api.chargebro.com/a/k20", callback);
        } else {
            if (!CodeScanner.isSupported()) {
                callback.onError(null, null, 0, "Not possible to scan QR code without camera access");
            } else {
//            boolean isUserNotifiedAboutLocationUse = Preferences.get("isUserNotifiedAboutCameraUse", false);
//            boolean isUserAgreeToGiveCameraAccess = true;
//            if (!isUserNotifiedAboutLocationUse) {
//                isUserAgreeToGiveCameraAccess = Dialog.show("Permission required", "Please allow using of your camera to scan QR code", "OK", "Cancel");
//            }

//            if (isUserAgreeToGiveCameraAccess) {
                // TODO: 5/27/2020 replace by custom dialog with QR code or enter number option and remember choice option
//                Dialog.show("QR code scanning", "Please point the camera at the QR code", "OK", null);
//                ToastBar.showInfoMessage("Please point the camera at the QR code");
                QRScanner.scanQRCode(new ScanResult() {
                    @Override
                    public void scanCompleted(String contents, String formatName, byte[] rawBytes) {
                        Preferences.set("isUserNotifiedAboutCameraUse", true);
                        getBeforeRentInfo(contents, callback);
                    }

                    @Override
                    public void scanCanceled() {
                        Preferences.set("isUserNotifiedAboutCameraUse", true);
                        callback.onError(null, null, 0, "Scan is cancelled");
                    }

                    @Override
                    public void scanError(int errorCode, String message) {
                        callback.onError(null, null, errorCode, "Error when scanning a QR code");
                        Log.e(new RuntimeException("QR scanning error -" + errorCode + message));
                    }
                });
//            } else {
//                callback.onError(null, null, 0, "Not possible to scan QR code without camera access");
//            }
            }
        }

    }

    public static void prepareCheckout(final Callback<String> callback) {
        Rest.get(GlobalConst.getServerUrl() + PAY_URL)
                .bearer(UserService.getToken())
//                .queryParam("stationId", stationId)
                .acceptJson()
                .timeout(10000)
                .onErrorCode(errorData -> {
                    // TODO: 5/27/2020 move to general error handler
                    if (errorData.getResponseCode() == 403 || errorData.getResponseCode() == 401) {
                        new LoginForm().show();
                        return;
                    }
                    ErrorResponse responseData = (ErrorResponse) (errorData.getResponseData());
//                    Log.p(("Error:" + errorData.getResponseCode() + responseData.message.get());
                    callback.onError(null, null, errorData.getResponseCode(), responseData.message.get());
                }, ErrorResponse.class)
                .fetchAsString(resp -> {
                    callback.onSucess(resp.getResponseData());
                });
    }

}
