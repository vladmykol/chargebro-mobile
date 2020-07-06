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
import com.codename1.components.ToastBar;
import com.codename1.sms.intercept.SMSInterceptor;
import com.codename1.ui.*;
import com.codename1.ui.animations.MorphTransition;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.Resources;
import com.codename1.ui.validation.RegexConstraint;
import com.codename1.ui.validation.Validator;
import com.mykovol.takeandcharge.service.RegisterStyle;
import com.mykovol.takeandcharge.service.UserService;

import static com.codename1.ui.CN.callSerially;
import static com.codename1.ui.CN.getCurrentForm;

public class RegisterMobileNumberStep1 extends Form {

    private final RegisterVerificationCodeStep2 registerVerificationCodeStep2 = new RegisterVerificationCodeStep2(getCurrentForm());


    private final Image logoImage = Resources.getGlobalResources().getImage("mobile-number.png");
    private final TextField phoneNumber = new TextField("", "(93) 123-45-67", 40, TextField.PHONENUMBER);
    private final SpanLabel errorText = new SpanLabel("Please enter valid phone number", RegisterStyle.ERROR_LABEL);
    private final CountryCodePicker countryCodeButton = new CountryCodePicker();
    private final Label errorTimeLabel = new Label("", RegisterStyle.ERROR_LABEL);
    private final Container errorContainer = BoxLayout.encloseX(errorText, errorTimeLabel);
    private final FloatingActionButton submitButton = FloatingActionButton.createFAB(FontImage.MATERIAL_ARROW_FORWARD);
    private final String countryCodeButtonName = "CountryCodeButton";
    private final String enterMobileNumberName = "EnterMobileNumber";
    private final String errorLabelName = "ErrorLabel";
    private SpanLabel mobileNumber = new SpanLabel("We need your mobile number to send SMS with PIN code", RegisterStyle.LABEL);

    public RegisterMobileNumberStep1() {
        super(BoxLayout.y());
        getToolbar().setTitle("Step 1 from 3");
        getToolbar().addCommandToRightBar(getCloseCommand());

        initComponents();
        attachComponentsToForm();

        setEditOnShow(phoneNumber);
    }

    private void attachComponentsToForm() {
        add(BoxLayout.encloseXCenter(new Label(logoImage)));
        add(mobileNumber);
        add(BorderLayout.centerEastWest(
                phoneNumber,
                null,
                countryCodeButton));
        add(errorContainer);
        submitButton.bindFabToContainer(this);
    }

    private void initComponents() {
        phoneNumber.setUIID(RegisterStyle.TEXT_FIELD);
        phoneNumber.setName(enterMobileNumberName);
        Validator phoneNumberValidator = createPhoneNumberValidator();
        ActionListener<?> submitAction = createSubmitAction(phoneNumberValidator);
        submitButton.addActionListener(submitAction);
        phoneNumber.addActionListener(submitAction);

        countryCodeButton.setName(countryCodeButtonName);
        Style ps = phoneNumber.getUnselectedStyle();
        Style cs = countryCodeButton.getUnselectedStyle();
        int pl = cs.getPaddingLeft(isRTL());
        int pr = cs.getPaddingRight(isRTL());
        countryCodeButton.getAllStyles().setPaddingUnit(Style.UNIT_TYPE_PIXELS);
        countryCodeButton.getAllStyles().setPadding(ps.getPaddingTop(), ps.getPaddingBottom(), pl, pr);

        errorTimeLabel.setVisible(false);
        errorText.getAllStyles().setPaddingRight(1);
        errorTimeLabel.getAllStyles().setPaddingLeft(0);
        errorContainer.setVisible(false);
        errorContainer.setName(errorLabelName);
    }

    private Validator createPhoneNumberValidator() {
        Validator validator = new Validator();
        validator.addConstraint(phoneNumber, new RegexConstraint("^[0-9][0-9- ]{7,15}[0-9]$",
                "Please enter valid phone number"));
        return validator;
    }

    private Command getCloseCommand() {
        FontImage mat = FontImage.createMaterial(FontImage.MATERIAL_CLOSE, "", 4.5f);
        return Command.create("", mat, e -> {

            MorphTransition morph = MorphTransition.create(400).
                    morph(enterMobileNumberName).
                    morph(countryCodeButtonName).
                    morph(errorLabelName);
            setTransitionOutAnimator(morph);
            if (phoneNumber.isEditing()) {
                phoneNumber.stopEditing(() -> {
                    revalidate();
                    callSerially(MainForm.get()::show);
                });
            } else {
                MainForm.get().show();
            }
        });
    }

    private ActionListener<?> createSubmitAction(Validator validator) {
        return e -> {
            if (!validator.isValid()) {
                errorContainer.setVisible(true);
                repaint();
                return;
            }
            errorContainer.setVisible(false);
            String number = phoneNumber.getText();

            String phone = formatPhoneNumber(countryCodeButton, number);

            registerVerificationCodeStep2.show(phone);

            registerVerificationCodeStep2.addShowListener(ee -> {
                if (SMSInterceptor.isSupported()) {
                    SMSInterceptor.grabNextSMS(s -> {
                        if (UserService.validateSMSActivationCode(s)) {
                            new EditAccountForm().show();
                            ToastBar.showMessage("Automatically Validated Phone Number!", FontImage.MATERIAL_THUMB_UP);
                        }
                    });
                }

                UserService.sendSMSActivationCode(phone);
            });
        };
    }

    public String formatPhoneNumber(CountryCodePicker countryCodeButton, String number) {
        StringBuilder stringBuffer = new StringBuilder(number);
        stringBuffer.insert(0, " (");
        stringBuffer.insert(4, ") ");
        stringBuffer.insert(9, "-");
        stringBuffer.insert(12, "-");
        return countryCodeButton.getText() + stringBuffer;
    }

//    public void startResendTimer(Container resendContainer, Label resentTimeLabel) {
//        resendTime = 120;
//        timer = UITimer.timer(1000, true, this, () -> {
//            if (resendTime > 0) {
//                resendTime--;
//                resentTimeLabel.setText(formatSeconds(resendTime));
//                return;
//            }
//            timer.cancel();
//            resentTimeLabel.setHidden(true, true);
//            resendContainer.replace(resentLabel, resendButton, CommonTransitions.createFade(50));
////            UserService.resendSMSActivationCode(phone);
//        });
//    }
//
//    private String formatSeconds(int time) {
//        return twoDigits(time / 60) + ":" + twoDigits(time % 60);
//    }
//
//    private String twoDigits(int t) {
//        if (t < 10) {
//            return "0" + t;
//        }
//        return "" + t;
//    }
}
