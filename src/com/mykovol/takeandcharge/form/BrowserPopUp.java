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

import com.codename1.io.Preferences;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.Command;
import com.codename1.ui.Form;
import com.codename1.ui.Toolbar;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.mykovol.takeandcharge.tools.CommonCode;

import static com.codename1.ui.CN.callSerially;

/**
 * Authorization of a credit card
 *
 * @author Vlad Mykol
 */
public class BrowserPopUp extends Form {
    private final BrowserComponent browser;

    public BrowserPopUp(String title) {
        super(new BorderLayout());
//        CommonCode.removeTransitionsTemporarily(previous);
        setToolbar(new Toolbar(false));
        getToolbar().setTitle(title);
        browser = new BrowserComponent();
        add(BorderLayout.CENTER, browser);
    }

    public void show(String url) {
        browser.setURL(url);
        super.show();
    }

    public void show(String url, String closeUrl, Form closeToForm) {
        browser.setURL(url);
        browser.addBrowserNavigationCallback(currentUrl -> {
            if (closeUrl != null) {
                if (currentUrl.contains(closeUrl)) {
                    callSerially(closeToForm::show);
                    return false;
                } else {
                    return true;
                }
            }
            return true;
        });
        super.show();
    }

    public void showPaymentPage(String url) {
        browser.setURL(url);
        browser.addBrowserNavigationCallback(currentUrl -> {
            if (currentUrl.contains("chargebro")) {
                MainForm.get().showNoUpdate();
                Preferences.set("noPaymentMethod", "false");
                return false;
            } else {
                return true;
            }
        });
        super.show();
    }

    public void setBackAction(Form form) {
        setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_HORIZONTAL, false, 300));
        getToolbar().setBackCommand(new Command("") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                form.showBack();
            }
        }, Toolbar.BackCommandPolicy.AS_ARROW, 4.5f);
    }

    public void setCloseAction(Form form) {
        setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, false, 300));
        final Command closeToPrevFormCommand = CommonCode.getCloseCommand(form);
        getToolbar().addCommandToRightBar(closeToPrevFormCommand);
    }

}
