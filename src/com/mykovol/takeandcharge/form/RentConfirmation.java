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
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.BeforeRentInfo;
import com.mykovol.takeandcharge.service.RentService;
import com.mykovol.takeandcharge.service.WebSocketClient;
import com.mykovol.takeandcharge.tools.CommonCode;

import static com.mykovol.takeandcharge.service.GlobalConst.PRICE_URL;

/**
 * @author Vlad Mykol
 */
public class RentConfirmation extends Form {

    public RentConfirmation(BeforeRentInfo beforeRentInfo) {
        super(new BorderLayout());
        setFormBottomPaddingEditingMode(true);
        setToolbar(new Toolbar(true));
        setTransitionOutAnimator(CommonTransitions.createEmpty());
//        getToolbar().setBackCommand(constructBackCommand(previousForm), Toolbar.BackCommandPolicy.AS_ARROW, 4.5f);

        getContentPane().getAllStyles().setMarginUnit(Style.UNIT_TYPE_DIPS);
        getContentPane().getAllStyles().setMargin(0, 4, 3.5f, 3.5f);

        Label spaceLabel = new Label(" ");
        if (!Display.getInstance().isTablet() && Display.getInstance().getDeviceDensity() < Display.DENSITY_HD) {
            spaceLabel.setHidden(true);
        }

        Label headerImage = new Label("", "RentConfirmationImage");
        headerImage.setMaterialIcon(FontImage.MATERIAL_RECEIPT_LONG);
        SpanLabel headerTextLabel = new SpanLabel("You are about to rent a powerbank", "RentConfirmationHeader");
        headerTextLabel.setEnabled(false);

        Label depositAmountLabel = new Label(String.valueOf(beforeRentInfo.holdAmount.getInt()), "RentConfirmationText");
        Label bonusAmountLabel = new Label(String.valueOf(beforeRentInfo.bonusAmount.getInt()), "RentConfirmationText");

        final Label panelDelimiterLabel = new Label("", "RentConfirmationDelimiter");
        panelDelimiterLabel.setShowEvenIfBlank(true);
        final Label panelDelimiterLabel2 = new Label("", "RentConfirmationDelimiter");
        panelDelimiterLabel2.setShowEvenIfBlank(true);

        final Container summaryAmountHolder = BoxLayout.encloseY(
                panelDelimiterLabel,
                BorderLayout.centerCenterEastWest(null, BoxLayout.encloseXRight(depositAmountLabel), new Label("Deposit", "RentConfirmationText")),
                BorderLayout.centerCenterEastWest(null, BoxLayout.encloseXRight(bonusAmountLabel), new Label("Bonus to be used", "RentConfirmationText")),
                panelDelimiterLabel2
        );

        Label amountHintLabel = new Label("We will hold", "RentConfirmationHint");
        Label amountHintLabel1 = new Label(" ", "RentConfirmationHint");
        Label amountHintLabel2 = new Label(String.valueOf(beforeRentInfo.holdAmount.getInt()), "RentConfirmationHint");
        Label amountHintLabel3 = new Label(" ", "RentConfirmationHint");
        Label amountHintLabel4 = new Label("UAH wich will be", "RentConfirmationHint");
        Label amountHintLabel5 = new Label(" ", "RentConfirmationHint");
        Label amountHintLabel6 = new Label("returned to your card", "RentConfirmationHint");
        Label amountHintLabel7 = new Label(" ", "RentConfirmationHint");
        Label amountHintLabel8 = new Label("right after", "RentConfirmationHint");
        Label amountHintLabel9 = new Label(" ", "RentConfirmationHint");
        Label amountHintLabel10 = new Label("returning a powerbank", "RentConfirmationHint");

        final Container centerHolder = BoxLayout.encloseYCenter(headerImage,
                headerTextLabel,
                spaceLabel,
                summaryAmountHolder,
                FlowLayout.encloseCenter(amountHintLabel,
                        amountHintLabel1,
                        amountHintLabel2,
                        amountHintLabel3,
                        amountHintLabel4,
                        amountHintLabel5,
                        amountHintLabel6,
                        amountHintLabel7,
                        amountHintLabel8,
                        amountHintLabel9,
                        amountHintLabel10)
        );
        centerHolder.setScrollableY(true);
        centerHolder.setScrollVisible(false);
        centerHolder.setTensileDragEnabled(false);
        add(BorderLayout.CENTER, centerHolder);

        final Label priceHintLabel1 = new Label("Once you return a powerbank", "RentConfirmationHint");
        final Label priceHintLabel2 = new Label(" ", "RentConfirmationHint");
        final Label priceHintLabel3 = new Label("you will be", "RentConfirmationHint");
        final Label priceHintLabel4 = new Label(" ", "RentConfirmationHint");
        final Label priceHintLabel5 = new Label("charged according to", "RentConfirmationHint");
        final Label priceHintLabel6 = new Label(" ", "RentConfirmationHint");
        final Button priceHintLinkButton = new Button("rent price", "RentConfirmationLink");
        final BrowserPopUp priceForm = new BrowserPopUp(PRICE_URL, null, "Price", this);
        priceForm.setTransitionInAnimator(CommonTransitions.createCover(CommonTransitions.SLIDE_VERTICAL, false, 300));
        priceForm.setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, false, 300));
        priceHintLinkButton.addActionListener(evt -> {
            CommonCode.removeTransitionsTemporarily(this);
            priceForm.show();
        });

        Button unlockPowerBankButton = new Button("Unlock a powerbank", "LoginButton");

        Button cancelButton = new Button("Cancel", "RentConfirmationCancel");
        cancelButton.addActionListener(evt -> {
            MainForm.get().show();
        });

        unlockPowerBankButton.addActionListener(evt -> {
            RentService.sendRentRequest(beforeRentInfo.stationId.get(), new Callback<String>() {
                @Override
                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                    MainForm.get().showError(errorMessage, errorCode);
                }

                @Override
                public void onSucess(String value) {
                    WebSocketClient.get().connect();
                    MainForm.get().revalidate();
//                    wait for async response for webSocke client
                }
            });
            MainForm.get().show();
        });

        final Container bottomHolder = BoxLayout.encloseY(
                FlowLayout.encloseCenter(priceHintLabel1,
                        priceHintLabel2,
                        priceHintLabel3,
                        priceHintLabel4,
                        priceHintLabel5,
                        priceHintLabel6,
                        priceHintLinkButton),
                unlockPowerBankButton,
                cancelButton
        );
        bottomHolder.setScrollableY(false);
        add(BorderLayout.SOUTH, bottomHolder);
    }
}
