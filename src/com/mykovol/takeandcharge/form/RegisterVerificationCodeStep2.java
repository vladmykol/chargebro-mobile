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
import com.codename1.components.SpanLabel;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.util.UITimer;
import com.mykovol.takeandcharge.service.RegisterStyle;
import com.mykovol.takeandcharge.service.UserService;

import static com.codename1.ui.CN.getCurrentForm;

/**
 * Implements the SMS verification code logic
 *
 * @author Shai Almog
 */
public class RegisterVerificationCodeStep2 extends Form {
    private final Label phoneNumberHolder = new Label("", RegisterStyle.MOBILE_NUMBER);
    private final SpanLabel phoneNumberText = new SpanLabel("We have sent you an SMS with code", RegisterStyle.LABEL);
    private final TextField smsCode = new TextField("", "Code", 40, TextField.NUMERIC);
    private final Label resentLabel = new Label("code is valid for ", RegisterStyle.RESEND_LABEL);
    private final Button resendButton = new Button("Resend code", RegisterStyle.RESEND_BUTTON);
    private final TextField passwordField = new TextField("", "New password", 40, TextField.PASSWORD);
    private final Button maskAndUnmaskPass = new Button("Show", RegisterStyle.TERMS_LINK);
    private final CheckBox termsCheckBox = new CheckBox("I accept");
    private final Button termsLink = new Button("Terms&Conditions", RegisterStyle.TERMS_LINK);
    private final SpanLabel errorText = new SpanLabel("PIN you've entered is incorrect", RegisterStyle.ERROR_LABEL);
    private int resendTime = 600;
    private UITimer timer;

    public RegisterVerificationCodeStep2(Form previousForm) {
        super(BoxLayout.y());
        getToolbar().setTitle("Step 2 from 3");
        getToolbar().setBackCommand(constructBackCommand(previousForm), Toolbar.BackCommandPolicy.AS_ARROW, 4.5f);

        Container box = new Container(BoxLayout.y());
        box.setScrollableY(true);
        box.add(FlowLayout.encloseCenter(phoneNumberHolder));

        box.add(FlowLayout.encloseCenter(phoneNumberText));

        smsCode.setUIID(RegisterStyle.TEXT_FIELD);
        box.add(smsCode);

        FontImage.setMaterialIcon(resendButton, FontImage.MATERIAL_REPLAY);
        Label resentTimeLabel = new Label(formatSeconds(resendTime), RegisterStyle.RESEND_LABEL);
        resentLabel.getAllStyles().setPaddingRight(1);
        resentTimeLabel.getAllStyles().setPaddingLeft(0);
        Container resendContainer = BoxLayout.encloseXRight(resentLabel, resentTimeLabel);
        box.add(resendContainer);

        passwordField.setUIID(RegisterStyle.TEXT_FIELD);
        maskAndUnmaskPass.addActionListener(evt -> {
            if (passwordField.getConstraint() == TextField.PASSWORD) {
                passwordField.setConstraint(TextField.ANY);
                maskAndUnmaskPass.setText("Hide");
            } else {
                passwordField.setConstraint(TextField.PASSWORD);
                maskAndUnmaskPass.setText("Show");
            }
            if (passwordField.isEditing()) {
                passwordField.stopEditing();
                passwordField.startEditingAsync();
            } else {
                passwordField.getParent().revalidate();
            }
        });
        box.add(passwordField);
        box.add(BoxLayout.encloseXRight(maskAndUnmaskPass));

        termsCheckBox.setUIID(RegisterStyle.TERMS_CHECK_BOX);
        termsCheckBox.setGap(2);
        termsCheckBox.setOppositeSide(false);
        termsCheckBox.getAllStyles().setPaddingRight(0.7f);
        termsLink.getAllStyles().setPaddingLeft(0);
        box.add(BoxLayout.encloseX(termsCheckBox, termsLink));

        termsLink.addActionListener(evt -> {
            new BrowserPopUp(getCurrentForm(),
                    "Terms&Conditions",
                    "https://www.termsandconditionsgenerator.com/")
                    .show();
        });

        errorText.setVisible(false);
        box.add(errorText);
        add(box);

        smsCode.addActionListener(evt -> {
            smsCode.stopEditing();
        });
        startResendTimer(resendContainer, resentTimeLabel);

        resendButton.addActionListener(evt -> {
            resentTimeLabel.setHidden(false, true);
            resendContainer.replace(resendButton, resentLabel, CommonTransitions.createFade(50));
            startResendTimer(resendContainer, resentTimeLabel);
        });

        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_ARROW_FORWARD);
        fab.bindFabToContainer(this);

        fab.addActionListener(e -> {
            if (!isValid(smsCode.getText())) {
                errorText.setVisible(true);
                smsCode.clear();
                repaint();
                return;
            }
            new RegisterCreditCardStep3().show();
        });
    }

    private Command constructBackCommand(Form previousForm) {
        //        CommonCode.removeTransitionsTemporarily(previous);
        return new Command("") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                previousForm.showBack();
            }
        };
    }

    public void startResendTimer(Container resendContainer, Label resentTimeLabel) {
        resendTime = 120;
        timer = UITimer.timer(1000, true, this, () -> {
            if (resendTime > 0) {
                resendTime--;
                resentTimeLabel.setText(formatSeconds(resendTime));
                return;
            }
            timer.cancel();
            resentTimeLabel.setHidden(true, true);
            resendContainer.replace(resentLabel, resendButton, CommonTransitions.createFade(50));
//            UserService.resendSMSActivationCode(phone);
        });
    }

    private String formatSeconds(int time) {
        return twoDigits(time / 60) + ":" + twoDigits(time % 60);
    }

    private String twoDigits(int t) {
        if (t < 10) {
            return "0" + t;
        }
        return "" + t;
    }

    public final boolean isValid(String s) {
        return UserService.validateSMSActivationCode(s);
    }

    public void show(String phone) {
        phoneNumberHolder.setText(phone);
//        phoneNumberHolder.getParent().revalidate();
        super.show();
    }
}
