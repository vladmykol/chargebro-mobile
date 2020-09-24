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
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.validation.Validator;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.RegisterInitResponse;
import com.mykovol.takeandcharge.dataobj.User;
import com.mykovol.takeandcharge.form.component.PasswordFieldContainer;
import com.mykovol.takeandcharge.form.component.PhoneFieldContainer;
import com.mykovol.takeandcharge.form.component.SmsFieldContainer;
import com.mykovol.takeandcharge.service.RentService;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.tools.InfinityProgressBlocking;

/**
 * Registering of a new user. Creating password
 *
 * @author Vlad Mykol
 */
public class RegisterForm extends Form {
    private final SmsFieldContainer smsCodeField;
    private final PasswordFieldContainer passwordField = new PasswordFieldContainer();

    private final SpanLabel errorLabel = new SpanLabel("", "LoginError");
    private final User user = new User();
    private final Validator smsValidator = new Validator();
    private final Validator passwordValidator = new Validator();

    public RegisterForm(Form previousForm, PhoneFieldContainer phoneFieldContainer,
                        RegisterInitResponse response) {
        super(BoxLayout.y());
        setFormBottomPaddingEditingMode(true);
        setToolbar(new Toolbar(false));
        getToolbar().setBackCommand(constructBackCommand(previousForm), Toolbar.BackCommandPolicy.AS_ARROW, 4.5f);

        Label spaceLabel = new Label(" ");
        Label headerText = new Label("Sign Up", "LoginHeader");
        if (!Display.getInstance().isTablet() && Display.getInstance().getDeviceDensity() < Display.DENSITY_HD) {
            spaceLabel = new Label();
            setTitle(headerText.getText());
            headerText.setHidden(true);
            spaceLabel.setHidden(true);
        }

        getContentPane().getAllStyles().setMarginUnit(Style.UNIT_TYPE_DIPS);
        getContentPane().getAllStyles().setMargin(0, 4, 3.5f, 3.5f);

        smsCodeField = new SmsFieldContainer(response.code.get(), response.validForMin.getInt() * 60, this);
        Label phoneNumberHolder = new Label("", "LoginSubHeader");
        phoneNumberHolder.setText(phoneFieldContainer.getFormattedPhoneNumber());

        user.name.set(phoneFieldContainer.getFullPhoneNumber());
        user.token.set(response.token.get());

        Validator.setValidateOnEveryKey(true);
        Button registerButton = new Button("Register", "LoginButton");
        smsValidator.addSubmitButtons(registerButton);
        smsValidator.setValidationFailureHighlightMode(Validator.HighlightMode.NONE);
        smsCodeField.setValidator(smsValidator);

        passwordValidator.setValidationFailureHighlightMode(Validator.HighlightMode.NONE);
        passwordField.setValidator(passwordValidator);

        errorLabel.setHidden(true);
        registerButton.addActionListener(evt -> {
            errorLabel.setHidden(true);
            if (!isValid()) {
                return;
            }

            setEditOnShow(null);
            InfinityProgressBlocking.start();

            user.password.set(passwordField.getValue());
            user.smsCode.set(smsCodeField.getValue());

            UserService.registerUser(user, new Callback<String>() {
                @Override
                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                    InfinityProgressBlocking.stop();
                    showError(errorMessage);
                }

                @Override
                public void onSucess(String response) {
                    RentService.prepareCheckout(new Callback<String>() {
                        @Override
                        public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                            InfinityProgressBlocking.stop();
                            showError(errorMessage);
                        }

                        @Override
                        public void onSucess(String checkoutUrl) {
                            InfinityProgressBlocking.stop();
                            new BrowserPopUp(checkoutUrl, "take-and-charge", "Add credit card", MainForm.get()).show();
                        }
                    });
                }
            });
        });

        addAll(
                headerText,
                phoneNumberHolder,
                spaceLabel,
                smsCodeField,
                passwordField,
                errorLabel,
                registerButton
        );

        if (response.warningMessage.get() != null) {
            showError(response.warningMessage.get());
        } else {
            errorLabel.setHidden(true);
        }

        passwordField.getField().setNextFocusDown(smsCodeField.getField());
    }


    private void showError(String errorMessage) {
        errorLabel.setText(errorMessage);
        errorLabel.setHidden(false);
        errorLabel.getParent().animateLayoutFade(300, 0);
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


    private boolean isValid() {
        if (!smsValidator.isValid()) {
            showError(smsCodeField.getErrorMessage());
        } else if (!passwordValidator.isValid()) {
            showError(passwordField.getErrorMessage());
        }

        return smsValidator.isValid() && passwordValidator.isValid();
    }

}
