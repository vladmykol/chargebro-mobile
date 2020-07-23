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

import com.codename1.components.FloatingActionButton;
import com.codename1.components.ToastBar;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BorderLayout;

import static com.codename1.ui.CN.getCurrentForm;

/**
 * Implements the SMS verification code logic
 *
 * @author Shai Almog
 */
public class RegisterCreditCardStep3 extends Form {

    public RegisterCreditCardStep3(String url) {
        super(new BorderLayout());
        Form previous = getCurrentForm();
//        CommonCode.removeTransitionsTemporarily(previous);
        setToolbar(new Toolbar(false));
        getToolbar().setTitle("Step 3 from 3");

        setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, false, 300));
        FontImage mat = FontImage.createMaterial(FontImage.MATERIAL_CLOSE, "", 4.5f);
        getToolbar().addCommandToRightBar("", mat, e -> MainForm.get().show());

        BrowserComponent browser = new BrowserComponent();
        browser.setURL(url);
        browser.addBrowserNavigationCallback(url1 -> {
            if (url1.indexOf("your-domain.example.com") > 0) {
                MainForm.get().show();
                return false;
            } else
                return true;
        });

        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_DONE);
        fab.bindFabToContainer(this);
        fab.addActionListener(evt -> {
            ToastBar.showMessage("Welcom...", FontImage.MATERIAL_INFO);
            MainForm.get().show();
        });

        add(BorderLayout.CENTER, browser);
    }

}
