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
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.BeforeRentInfo;
import com.mykovol.takeandcharge.service.RentService;
import com.mykovol.takeandcharge.tools.RentFullScreenLoader;

import static com.mykovol.takeandcharge.service.GlobalConst.PRICE_URL;

/**
 * @author Vlad Mykol
 */
public class RentConfirmation extends Form {

    public RentConfirmation(BeforeRentInfo beforeRentInfo) {
        super(new LayeredLayout());
        final Container mainContent = new Container(new BorderLayout());
        setScrollableY(false);

        setFormBottomPaddingEditingMode(true);
        setToolbar(new Toolbar(true));
        setTransitionOutAnimator(CommonTransitions.createEmpty());
        setTransitionInAnimator(CommonTransitions.createEmpty());
//        setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, true, 300));
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

        Label depositAmountLabel = new Label(beforeRentInfo.holdAmount.get(), "RentConfirmationText");
        final Label panelDelimiterLabel = new Label("", "RentConfirmationDelimiter");
        panelDelimiterLabel.setShowEvenIfBlank(true);
        final Label panelDelimiterLabel2 = new Label("", "RentConfirmationDelimiter");
        panelDelimiterLabel2.setShowEvenIfBlank(true);

        final Container summaryAmountHolder = BoxLayout.encloseY(
                panelDelimiterLabel,
                BorderLayout.centerCenterEastWest(null, BoxLayout.encloseXRight(depositAmountLabel), new Label("Deposit", "RentConfirmationText")),
                panelDelimiterLabel2
        );
        SpanLabel amountHintLabel = new SpanLabel("Deposit is an amount of money that we block on your card until the rent finish", "RentConfirmationHint");
        amountHintLabel.setEnabled(false);

        final Container centerHolder = BoxLayout.encloseYCenter(headerImage,
                headerTextLabel,
                spaceLabel,
                summaryAmountHolder,
                amountHintLabel
        );
        centerHolder.setScrollableY(true);
        centerHolder.setScrollVisible(false);
        centerHolder.setTensileDragEnabled(false);
        mainContent.add(BorderLayout.CENTER, centerHolder);

        final Label priceHintLabel1 = new Label("Rent", "RentConfirmationHintNoCenter");
        final Label priceHintLabel2 = new Label(": ", "RentConfirmationHintNoCenter");
        final Label priceHintLabel3 = new Label("30 min free", "RentConfirmationHintNoCenterGreen");
        final Label priceHintLabel4 = new Label(", ", "RentConfirmationHintNoCenter");
        final Label priceHintLabel5 = new Label("then according to", "RentConfirmationHintNoCenter");
        final Label priceHintLabel6 = new Label(" ", "RentConfirmationHintNoCenter");
        final Button priceHintLinkButton = new Button("the rental price", "RentConfirmationLink");
        final BrowserPopUp priceForm = new BrowserPopUp("Price");
        priceForm.setFadeBackDownTo(this);
        priceHintLinkButton.addActionListener(evt -> {
            priceForm.show();
            priceForm.serUrlNoReload(PRICE_URL);
        });

        Button unlockPowerBankButton = new Button("Unlock a powerbank", "RentConfirmationUnlockButton");

        Button cancelButton = new Button("Cancel", "RentConfirmationCancel");
        cancelButton.addActionListener(evt -> {
            MainForm.get().showNoUpdate();
        });

//        addShowListener(evt -> {
//            EasyThread et = EasyThread.start("imageBluer");
//            et.run(() -> {
//                RentGifLoader.get().setBackgroundForm(this);
//            });
//        });

        unlockPowerBankButton.addActionListener(evt -> {
            RentFullScreenLoader.get().setBackgroundForm(this);
            RentFullScreenLoader.get().start();
            RentService.sendRentRequest(beforeRentInfo.stationId.get(), new Callback<String>() {
                @Override
                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                    MainForm.get().showErrorOnMainScreen(errorMessage, errorCode);
                }

                @Override
                public void onSucess(String value) {
                    RentFullScreenLoader.get().setStageWaitingBankResponse();
                }
            });
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

        mainContent.add(BorderLayout.SOUTH, bottomHolder);

        add(mainContent);
    }
}
