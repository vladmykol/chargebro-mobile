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

import com.codename1.ui.Display;

/**
 * Global settings used in the application
 *
 * @author Vlad Mykol
 */
public final class GlobalConst {
    public static final boolean FORCE_REMOTE = false;

    public static final String RENT_URL = "/rent";
    public static final String PAY_URL = "/pay/checkout";
    public static final String RENT_HISTORY_URL = RENT_URL + "/history";
    public static final String STATIONS_URL = "/stations";
    public static final String STATIONS_NEARBY_URL = STATIONS_URL + "/nearby";
    public static final String STATIONS_CAPACITY_URL = STATIONS_URL + "/{id}/capacity";
    public static final String SERVER_SOCKET_URL = "/socket/rent";
    public static final String API_APP_VERSION = "/a/version";
    public static final String API_APP_USER = "/user";
    public static final String API_APP_USER_CARD = API_APP_USER + "/card";
    public static final String POLICY_URL = "https://chargebro.com/policy";
    public static final String PRICE_URL = "https://chargebro.com/pricing";
    public static final String FONDY_POLICY_URL = "https://fondy.ua/ru/legal/public-offer/";
    private static final String API_AUTH = "/v1/auth";
    public static final String API_INIT = API_AUTH + "/init";
    public static final String API_RESET_PASS = API_AUTH + "/reset";
    public static final String API_REGISTER = API_AUTH + "/register";
    public static final String API_LOGIN = API_AUTH + "/login";
    public static final String API_LOGOUT = API_AUTH + "/logout";

    public static String getServerUrl() {
        if (isRunningOnLocalHost()) return "http://localhost:10381";
        else return "https://api.chargebro.com";
    }

    public static boolean isRunningOnLocalHost() {
        return Display.getInstance().isSimulator() && !FORCE_REMOTE;
    }
}
