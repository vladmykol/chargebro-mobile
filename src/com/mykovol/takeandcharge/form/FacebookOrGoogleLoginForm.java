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

package com.mykovol.takeandcharge.form;

import com.codename1.components.ToastBar;
import com.codename1.io.rest.Response;
import com.codename1.io.rest.Rest;
import com.codename1.social.FacebookConnect;
import com.codename1.social.GoogleConnect;
import com.codename1.social.Login;
import com.codename1.social.LoginCallback;
import com.codename1.ui.Button;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.Toolbar;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.util.Resources;
import com.mykovol.takeandcharge.tools.CommonCode;

import java.util.Map;

import static com.codename1.ui.CN.getCurrentForm;

/**
 * @author Shai Almog
 */
public class FacebookOrGoogleLoginForm extends Form {
    public FacebookOrGoogleLoginForm() {
        super(BoxLayout.y());
        Form previous = getCurrentForm();
        setTransitionInAnimator(CommonTransitions.createCover(CommonTransitions.SLIDE_VERTICAL, false, 300));
        setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, true, 300));
        CommonCode.removeTransitionsTemporarily(previous);

        getToolbar().setBackCommand("", Toolbar.BackCommandPolicy.AS_ARROW, e -> previous.showBack());
        add(new Label("Choose an account", "FlagButton"));
        Button facebook = new Button("Facebook", Resources.getGlobalResources().getImage("facebook.png"), "FlagButton");
        Button google = new Button("Google", Resources.getGlobalResources().getImage("google.png"), "FlagButton");
        add(facebook).add(google);

        facebook.addActionListener(e -> {
            final Login fb = FacebookConnect.getInstance();

                fb.setClientId("REDACTED_FB_CLIENT_ID");
                fb.setClientSecret("REDACTED_FB_CLIENT_SECRET");

            fb.setRedirectURI("https://www.codenameone.com/");
            fb.setCallback(new LoginCallback() {
                @Override
                public void loginFailed(String errorMessage) {
                    ToastBar.showErrorMessage("Login failed: " + errorMessage);
                }

                @Override
                public void loginSuccessful() {
                    String token = fb.getAccessToken().getToken();
                    Response<Map> resp = Rest.get("https://graph.facebook.com/v2.12/me").
                            queryParam("access_token", token).
                            acceptJson().getAsJsonMap();
                    String userId = (String) resp.getResponseData().get("id");
//                    new LoginForm(null, userId, null).show();
                }
            });
            fb.doLogin();
        });

        google.addActionListener(e -> {
            Login gc = GoogleConnect.getInstance();
                gc.setClientId("REDACTED_GOOGLE_CLIENT_ID");
                gc.setClientSecret("REDACTED_GOOGLE_CLIENT_SECRET");
            gc.setRedirectURI("https://www.codenameone.com/login");
            GoogleConnect.getInstance().setCallback(new LoginCallback() {
                @Override
                public void loginFailed(String errorMessage) {
                    ToastBar.showErrorMessage("Login failed: " + errorMessage);
                }

                @Override
                public void loginSuccessful() {
                    String token = GoogleConnect.getInstance().getAccessToken().getToken();
                    Response<Map> resp = Rest.get("https://www.googleapis.com/plus/v1/people/me").
                            header("Authorization", "Bearer " + token).
                            acceptJson().getAsJsonMap();
                    String userId = (String) resp.getResponseData().get("id");
//                    new LoginForm(null, null, userId).show();
                }
            });
            GoogleConnect.getInstance().doLogin();
        });
    }

}
