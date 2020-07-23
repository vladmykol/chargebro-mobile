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
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.util.UITimer;
import com.codename1.ui.validation.Constraint;
import com.codename1.ui.validation.LengthConstraint;
import com.codename1.ui.validation.Validator;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.RegisterInitResponse;
import com.mykovol.takeandcharge.dataobj.User;
import com.mykovol.takeandcharge.service.RegisterStyle;
import com.mykovol.takeandcharge.service.RentService;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.tools.FabProgress;

import static com.codename1.ui.CN.getCurrentForm;

/**
 * Implements the SMS verification code logic
 *
 * @author Shai Almog
 */
public class RegisterVerificationCodeStep2 extends Form {
    private final Label phoneNumberHolder = new Label("", RegisterStyle.MOBILE_NUMBER);
    private final SpanLabel phoneNumberText = new SpanLabel("We have sent you an SMS with code", RegisterStyle.LABEL);
    private final TextField smsCode = new TextField("", "Code from SMS", 40, TextField.NUMERIC);
    private final Label resentLabel = new Label("code is valid for", RegisterStyle.RESEND_LABEL);
    private final Button resendButton = new Button("Resend code", RegisterStyle.RESEND_BUTTON);
    private final TextField passwordField = new TextField("", "New password", 40, TextField.PASSWORD);
    private final Button maskAndUnmaskPass = new Button("Show", RegisterStyle.TERMS_LINK);
    private final CheckBox termsCheckBox = new CheckBox("I accept");
    private final Button termsLink = new Button("Terms&Conditions", RegisterStyle.TERMS_LINK);
    private final String INCORRECT_PIN_ERROR_TEXT = "SMS code is incorrect";
    private final SpanLabel errorText = new SpanLabel(INCORRECT_PIN_ERROR_TEXT, RegisterStyle.ERROR_LABEL);
    private final User user = new User();
    private final String registerCode;
    private int registerCodeValidForSeconds;
    private UITimer timer;

    public RegisterVerificationCodeStep2(Form previousForm, String digitsPhone,
                                         RegisterInitResponse response) {
        super(BoxLayout.y());
        phoneNumberHolder.setText(formatPhoneNumber(digitsPhone));
        user.name.set(digitsPhone);
        user.token.set(response.token.get());
        registerCodeValidForSeconds = response.validForMin.getInt() * 60;
        registerCode = response.code.get();
        smsCode.setMaxSize(registerCode.length());
        passwordField.setMaxSize(16);

        setToolbar(new Toolbar(false));
        getToolbar().setTitle("Step 2 from 3");
        getToolbar().setBackCommand(constructBackCommand(previousForm), Toolbar.BackCommandPolicy.AS_ARROW, 4.5f);

        Container box = new Container(BoxLayout.y());
        box.setScrollableY(true);
        box.add(FlowLayout.encloseCenter(phoneNumberHolder));

//        box.add(FlowLayout.encloseCenter(phoneNumberText));

        smsCode.setUIID(RegisterStyle.TEXT_FIELD);
        passwordField.setUIID(RegisterStyle.TEXT_FIELD);

        smsCode.getAllStyles().setMargin(LEFT, 0);
        passwordField.getAllStyles().setMargin(LEFT, 0);
        Label smsIcon = new Label("", "CredentialsField");
        smsIcon.setShowEvenIfBlank(true);
        Label passwordIcon = new Label("", "CredentialsField");
        passwordIcon.setShowEvenIfBlank(true);
        smsIcon.getAllStyles().setMargin(RIGHT, 0);
        passwordIcon.getAllStyles().setMargin(RIGHT, 0);
        FontImage.setMaterialIcon(smsIcon, FontImage.MATERIAL_CHAT_BUBBLE_OUTLINE, 3);
        FontImage.setMaterialIcon(passwordIcon, FontImage.MATERIAL_LOCK_OUTLINE, 3);


        box.add(BorderLayout.center(smsCode).
                add(BorderLayout.WEST, smsIcon));

        FontImage.setMaterialIcon(resendButton, FontImage.MATERIAL_REPLAY);
        Label resentTimeLabel = new Label("", RegisterStyle.RESEND_LABEL);
        resentLabel.getAllStyles().setPaddingRight(0.7f);
        resentTimeLabel.getAllStyles().setPaddingLeft(0);
        Container resendContainer = BoxLayout.encloseXRight(resentLabel, resentTimeLabel);
        startResendTimer(resendContainer, resentTimeLabel);
        box.add(resendContainer);


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
                passwordField.getParent().revalidateWithAnimationSafety();
            }
        });

        box.add(BorderLayout.center(passwordField).
                add(BorderLayout.WEST, passwordIcon));

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
            passwordField.startEditingAsync();
        });
        passwordField.addActionListener(evt -> {
                    passwordField.stopEditing();
                }
        );

        resendButton.addActionListener(evt -> {
            previousForm.showBack();
        });

        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_ARROW_FORWARD);
        fab.bindFabToContainer(this);

        fab.addActionListener(e -> {
            errorText.setVisible(false);

            if (!new RegisterValidator().validate()) {
                return;
            }

            FabProgress.bind(fab);
            resendContainer.setVisible(true);

            user.password.set(passwordField.getText());
            user.smsCode.set(smsCode.getText());

            UserService.registerUser(user, new Callback<String>() {
                @Override
                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                    errorText.setText(errorCode + " " + errorMessage);
                    errorText.setVisible(true);
                    errorText.getParent().revalidateWithAnimationSafety();
//                    revalidateWithAnimationSafety();
                    FabProgress.stop(fab);
                }

                @Override
                public void onSucess(String response) {

                    RentService.prepareCheckout(new Callback<String>() {
                        @Override
                        public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                            errorText.setText(errorCode + " " + errorMessage);
                            errorText.setVisible(true);
                            errorText.getParent().revalidateWithAnimationSafety();
//                    revalidateWithAnimationSafety();
                            FabProgress.stop(fab);
                        }

                        @Override
                        public void onSucess(String checkoutUrl) {
                            new RegisterCreditCardStep3(checkoutUrl).show();
                            FabProgress.stop(fab);
                        }
                    });
                }
            });
        });
    }


    public void setSmsCode(String code) {
        smsCode.setText(code);
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
        resentTimeLabel.setText(formatSeconds(registerCodeValidForSeconds));
        timer = UITimer.timer(1000, true, this, () -> {
            if (registerCodeValidForSeconds > 0) {
                registerCodeValidForSeconds--;
                resentTimeLabel.setText(formatSeconds(registerCodeValidForSeconds));
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

    public String formatPhoneNumber(String number) {
        StringBuilder stringBuffer = new StringBuilder(number);
        stringBuffer.insert(0, "+");
        stringBuffer.insert(4, " (");
        stringBuffer.insert(8, ") ");
        stringBuffer.insert(13, "-");
        stringBuffer.insert(16, "-");
        return stringBuffer.toString();
    }

    private class RegisterValidator extends Validator {
        private static final String VALID_MARKER = "cn1$$VALID_MARKER";


        public RegisterValidator() {
            super();
            addConstraint(smsCode, new Constraint() {
                @Override
                public boolean isValid(Object value) {
                    return registerCode.equals(value.toString());
                }

                @Override
                public String getDefaultFailMessage() {
                    return "";
                }
            });
            addConstraint(passwordField, new LengthConstraint(4,
                    ""));
        }

        public boolean validate() {
            boolean valid = true;
            setValidateOnEveryKey(true);
            validate(smsCode);
            validate(passwordField);

            if (!isCurrentlyValid(smsCode)) {
                errorText.setText(INCORRECT_PIN_ERROR_TEXT);
                valid = false;
            } else if (!isCurrentlyValid(passwordField)) {
                errorText.setText("Password should contain minimum 4 characters");
                valid = false;
            } else if (!termsCheckBox.isSelected()) {
                errorText.setText("Please accept Terms&Conditions");
                valid = false;
            }

            if (!valid) {
                errorText.setVisible(true);
                errorText.getParent().revalidateWithAnimationSafety();
            }

            return valid;
        }

        private boolean isCurrentlyValid(Component cmp) {
            return (Boolean) cmp.getClientProperty(VALID_MARKER);
        }

    }

}
