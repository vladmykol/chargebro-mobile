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

import com.codename1.components.SpanLabel;
import com.codename1.io.Preferences;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.service.RentService;
import com.mykovol.takeandcharge.tools.CommonCode;
import com.mykovol.takeandcharge.tools.MainGifLoader;

/**
 * @author Vlad Mykol
 */
public class SettingsForm extends Form {

    public SettingsForm() {
        super(new BoxLayout(BoxLayout.Y_AXIS));

        setScrollableY(false);
        setTensileDragEnabled(false);

        setFormBottomPaddingEditingMode(true);
        setToolbar(new Toolbar(false));
        setTitle("Settings");
        setTransitionInAnimator(CommonTransitions.createEmpty());
        setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, false, 300));

        getToolbar().addCommandToRightBar(CommonCode.getCloseCommand(MainForm.get()));

        getContentPane().getAllStyles().setMarginUnit(Style.UNIT_TYPE_DIPS);
        getContentPane().getAllStyles().setMargin(0, 4, 3.5f, 3.5f);

        SpanLabel headerSubText = new SpanLabel("Settings tab is in development. " +
                "Not all feature working. No design", "RentConfirmationHint");
        headerSubText.setEnabled(false);

        add(headerSubText);

        final Button buttonUa = new Button("ua", "SideMenuButton");
        final Button buttonRu = new Button("ru", "SideMenuButton");
        final Button buttonEn = new Button("en", "SideMenuButton");
        final Button buttonDef = new Button("def", "SideMenuButton");

        buttonEn.addActionListener(evt -> {
            Preferences.set("userLang", "en");
        });
        buttonUa.addActionListener(evt -> {
            Preferences.set("userLang", "uk");
        });
        buttonRu.addActionListener(evt -> {
            Preferences.set("userLang", "ru");
        });
        buttonDef.addActionListener(evt -> {
            Preferences.set("userLang", null);
        });

        final Container langTool = BoxLayout.encloseX(buttonUa, buttonRu, buttonEn, buttonDef);

        add(langTool);

        final Button addCreditCard = new Button("Add Credit Card", "WalkthrSkipButton");

        addCreditCard.addActionListener(evt -> {
            MainGifLoader.get().start();
            RentService.prepareCheckout(new Callback<String>() {
                @Override
                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                    MainForm.get().showError(errorMessage, errorCode);
                }

                @Override
                public void onSucess(String checkoutUrl) {
                    BrowserPopUp addMoney = new BrowserPopUp(checkoutUrl, "chargebro", "Add credit card", MainForm.get());
                    addMoney.setTransitionInAnimator(CommonTransitions.createFade(300));
                    addMoney.setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, false, 300));
                    MainGifLoader.get().stop();
                    addMoney.show();
                }
            });
        });

        add(addCreditCard);
    }
}
