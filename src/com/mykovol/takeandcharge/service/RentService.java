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

import com.codename1.components.InfiniteProgress;
import com.codename1.components.ToastBar;
import com.codename1.ext.codescan.CodeScanner;
import com.codename1.ext.codescan.ScanResult;
import com.codename1.io.rest.Rest;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.ErrorResponse;
import com.mykovol.takeandcharge.dataobj.RentHistory;
import com.mykovol.takeandcharge.form.LoginForm;
import org.littlemonkey.qrscanner.QRScanner;

import java.util.List;

import static com.mykovol.takeandcharge.service.Const.*;

/**
 * A generic service class that handles login/creation etc.
 *
 * @author Shai Almog
 */
public class RentService {

    private static void sendRentRequest(String stationId, final Callback<String> callback) {
        Display.getInstance().getCurrent().revalidate();
        InfiniteProgress ip = new InfiniteProgress();
        Dialog dlg = ip.showInfiniteBlocking();
        Rest.post(SERVER_URL + RENT_URL)
                .bearer(UserService.getToken())
                .queryParam("stationId", stationId)
                .acceptJson()
                .onError(errorData -> {
                    errorData.consume();
                    callback.onError(null, errorData.getError(), errorData.getResponseCode(), "something is terribly wrong");
                })
                .onErrorCode(errorData -> {
                    // TODO: 5/27/2020 move to general error handler
                    if (errorData.getResponseCode() == 403) {
                        new LoginForm().show();
                        return;
                    }
                    ErrorResponse responseData = (ErrorResponse) (errorData.getResponseData());
                    callback.onError(null, null, errorData.getResponseCode(), responseData.message.get());
                }, ErrorResponse.class)
                .fetchAsString(resp -> {
                    callback.onSucess(resp.getResponseData());
                })
                .setDisposeOnCompletion(dlg);
    }

    public static void getRentHistory(boolean onlyCurrentlyInRent, final Callback<List<RentHistory>> callback) {
        Rest.get(SERVER_URL + RENT_HISTORY_URL)
                .bearer(UserService.getToken())
                .queryParam("filter", onlyCurrentlyInRent ? "current" : "all")
                .acceptJson()
                .onError(errorData -> {
                    errorData.consume();
                    callback.onError(null, errorData.getError(), errorData.getResponseCode(), "something is terribly wrong");
                })
                .onErrorCode(errorData -> {
                    ErrorResponse responseData = (ErrorResponse) (errorData.getResponseData());
                    callback.onError(null, null, errorData.getResponseCode(), responseData.message.get());
                }, ErrorResponse.class)
                .fetchAsPropertyList(historyList -> {
                    List<RentHistory> responseData = (List<RentHistory>) (List<?>) historyList.getResponseData();
                    callback.onSucess(responseData);
                }, RentHistory.class);

    }

    public static void rent(final Callback<String> callback) {
        if (!UserService.isLoggedIn()) {
            new LoginForm().show();
            return;
        }


        if (CodeScanner.getInstance() == null) {
            ToastBar.showErrorMessage("CodeScanner is not supported on this platform");
        } else {
            if (Display.getInstance().isSimulator()) {
                sendRentRequest("STWA312001000005", callback);
            } else {
                // TODO: 5/27/2020 replace by custom dialog with QR code or enter number option and remember choice option
//                Dialog.show("QR code scanning", "Please point the camera at the QR code", "OK", null);
//                ToastBar.showInfoMessage("Please point the camera at the QR code");
                QRScanner.scanQRCode(new ScanResult() {
                    @Override
                    public void scanCompleted(String contents, String formatName, byte[] rawBytes) {
                        String stationId = contents.substring(contents.indexOf("id=") + 3);

                        sendRentRequest(stationId, callback);
                    }

                    @Override
                    public void scanCanceled() {
                        ToastBar.showInfoMessage("Scan canceled");
                    }

                    @Override
                    public void scanError(int errorCode, String message) {
                        ToastBar.showInfoMessage("Scan ERROR");
                    }
                });
            }
        }
    }

}
