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
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.validation.Validator;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.RegisterInitResponse;
import com.mykovol.takeandcharge.dataobj.UserCreationRequest;
import com.mykovol.takeandcharge.form.component.PhoneFieldContainer;
import com.mykovol.takeandcharge.form.component.SmsFieldContainer;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.tools.FormCommand;
import com.mykovol.takeandcharge.tools.InfinityProgressBlocking;

/**
 * Registering of a new user. Creating password
 *
 * @author Vlad Mykol
 */
public class LoginFormConfirmation extends Form {
    private final SmsFieldContainer smsCodeField;
    private final SpanLabel errorLabel = new SpanLabel("", "LoginError");
    private final UserCreationRequest userCreationRequest = new UserCreationRequest();
    private final Validator smsValidator = new Validator();
    private final Button registerButton = new Button("OK", "LoginButton");

    public LoginFormConfirmation(Form previousForm, PhoneFieldContainer phoneFieldContainer,
                                 RegisterInitResponse response) {
        super(BoxLayout.y());
        setFormBottomPaddingEditingMode(true);
        setToolbar(new Toolbar(false));

        FormCommand.setBackAction(previousForm, this);

        Label spaceLabel = new Label(" ");
        Label headerText = new Label("Confirmation", "LoginHeader");
        if (!Display.getInstance().isTablet() && Display.getInstance().getDeviceDensity() < Display.DENSITY_HD) {
            spaceLabel = new Label();
            setTitle(headerText.getText());
            headerText.setHidden(true);
            spaceLabel.setHidden(true);
        }

        getContentPane().getAllStyles().setMarginUnit(Style.UNIT_TYPE_DIPS);
        getContentPane().getAllStyles().setMargin(0, 4, 3.5f, 3.5f);

        smsCodeField = new SmsFieldContainer(response.code.get(), response.validForMin.getInt() * 60, this, previousForm);
        Label phoneNumberHolder = new Label("", "LoginSubHeader");
        phoneNumberHolder.setText(phoneFieldContainer.getFormattedPhoneNumber());

        userCreationRequest.name.set(phoneFieldContainer.getFullPhoneNumber());
        userCreationRequest.token.set(response.token.get());

        Validator.setValidateOnEveryKey(true);
        smsValidator.addSubmitButtons(registerButton);
        smsValidator.setValidationFailureHighlightMode(Validator.HighlightMode.NONE);
        smsCodeField.setValidator(smsValidator);

        errorLabel.setHidden(true);
        registerButton.addActionListener(evt -> {
            errorLabel.setHidden(true);
            if (!isValid()) {
                return;
            }
            InfinityProgressBlocking.get().start(this);
            setEditOnShow(null);

            userCreationRequest.smsCode.set(smsCodeField.getValue());

            UserService.registerUser(userCreationRequest, new Callback<String>() {
                @Override
                public void onSucess(String value) {
                    setTransitionOutAnimator(CommonTransitions.createEmpty());
                    InfinityProgressBlocking.get().stop();
                    MainForm.get().show();
                }

                @Override
                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                    if (errorCode == 412) {
                        errorMessage = "SMS code is incorrect";
                    }
                    InfinityProgressBlocking.get().stop();
                    showError(errorMessage);
                }
            });
        });

        addAll(
                headerText,
                phoneNumberHolder,
                spaceLabel,
                smsCodeField,
                errorLabel,
                registerButton
        );

        if (response.warningMessage.get() != null) {
            showError(response.warningMessage.get());
        } else {
            errorLabel.setHidden(true);
        }

        smsCodeField.getField().setNextFocusDown(registerButton);
    }


    private void showError(String errorMessage) {
        errorLabel.setText(errorMessage);
        errorLabel.revalidate();
        errorLabel.setHidden(false);
        errorLabel.getParent().animateLayoutFade(300, 0);
    }

    private boolean isValid() {
        if (!smsValidator.isValid()) {
            showError(smsCodeField.getErrorMessage());
        }

        return smsValidator.isValid();
    }

}
