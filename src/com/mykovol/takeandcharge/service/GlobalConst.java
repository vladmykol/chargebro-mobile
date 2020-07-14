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
    public static final String SERVER_URL = "https://your-domain.example.com:10381";
//        public static final String SERVER_URL = "http://192.168.0.124:10381";
//    public static final String SERVER_URL = "https://localhost:10381";
    private static final String API_AUTH = SERVER_URL + "/auth";
    public static final String API_LOGIN = API_AUTH + "/login";
    public static final String API_REGISTER_INIT = API_AUTH + "/register";
    public static final String API_REGISTER = API_AUTH + "/singup";
    public static final String API_LOGOUT = API_AUTH + "/logout";

    public static final String RENT_URL = "/rent";
    public static final String PAY_URL = "/pay/checkout";
    public static final String RENT_HISTORY_URL = RENT_URL + "/history";
    public static final String SERVER_SOCKET_URL = "/socket/rent";
    public static final String TWILIO_ACCOUNT_SID = "REDACTED_TWILIO_SID";
    public static final String TWILIO_AUTH_TOKEN = "REDACTED_TWILIO_TOKEN";
    public static final String TWILIO_FROM_PHONE = "REDACTED_PHONE";
    public static final String GOOGLE_DIRECTIONS_KEY = "REDACTED_GOOGLE_KEY";
    public static final String GOOGLE_GEOCODING_KEY = "REDACTED_GOOGLE_KEY";
    public static final String GOOGLE_PLACES_KEY = "REDACTED_GOOGLE_KEY";
    public static final String CODENAME_ONE_PUSH_KEY = "REDACTED_PUSH_KEY";
    public static final String GOOGLE_PUSH_AUTH_KEY = "";
    public static final String APNS_DEV_PUSH_CERT = "";
    public static final String APNS_PROD_PUSH_CERT = "";
    public static final String APNS_DEV_PUSH_PASS = "";
    public static final String APNS_PROD_PUSH_PASS = "";
    public static final boolean DEBUG = true;
    public static final boolean APNS_PRODUCTION = !DEBUG;
}
