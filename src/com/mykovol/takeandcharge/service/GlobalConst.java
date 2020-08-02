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

/**
 * Global settings used in the application
 *
 * @author Shai Almog
 */
public final class GlobalConst {
    public static final boolean LOCAL = true;

    public static final String RENT_URL = "/rent";
    public static final String PAY_URL = "/pay/checkout";
    public static final String RENT_HISTORY_URL = RENT_URL + "/history";
    public static final String STATIONS_URL = "/stations";
    public static final String STATIONS_NEARBY_URL = STATIONS_URL + "/nearby";
    public static final String STATIONS_CAPACITY_URL = STATIONS_URL + "/{id}/capacity";
    public static final String SERVER_SOCKET_URL = "/socket/rent";

    private static final String API_AUTH = "/auth";
    public static final String API_LOGIN = API_AUTH + "/login";
    public static final String API_REGISTER_INIT = API_AUTH + "/register";
    public static final String API_REGISTER = API_AUTH + "/singup";
    public static final String API_LOGOUT = API_AUTH + "/logout";

    public static String getServerUrl() {
        if (LOCAL) return "http://192.168.0.124:10381";
        else return "https://server.your-domain.example.com";
    }

    public static final String POLICY_URL = "https://takeandcharge.space/pravyla-ta-umovy";
    public static final String PRICE_URL = "https://takeandcharge.space/#pricing";
}
