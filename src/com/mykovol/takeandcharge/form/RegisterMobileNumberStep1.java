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
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.util.Resources;
import com.codename1.ui.validation.Validator;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.RegisterInitResponse;
import com.mykovol.takeandcharge.form.component.LoginField;
import com.mykovol.takeandcharge.service.RegisterStyle;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.tools.FabProgress;

import static com.codename1.ui.CN.getCurrentForm;

/**
 * Registering of a new user. Phone number check
 *
 * @author Vlad Mykol
 */
public class RegisterMobileNumberStep1 extends Form {

    private final Image logoImage = Resources.getGlobalResources().getImage("mobile-number.png");
    private final SpanLabel errorText = new SpanLabel("", RegisterStyle.ERROR_LABEL);
    private final LoginField loginField = new LoginField();
    private final Label errorTimeLabel = new Label("", RegisterStyle.ERROR_LABEL);
    private final Container errorContainer = BoxLayout.encloseX(errorText, errorTimeLabel);
    private final FloatingActionButton submitButton = FloatingActionButton.createFAB(FontImage.MATERIAL_ARROW_FORWARD);
    private final SpanLabel mobileNumber = new SpanLabel("We need your mobile number to send SMS with PIN code", RegisterStyle.LABEL);
//    private final Button alreadyHaveAccountButton = new Button("Already have an account", "AlreadyHaveAnAccountButton");


    public RegisterMobileNumberStep1() {
        super(BoxLayout.y());
        setToolbar(new Toolbar(false));
        getToolbar().setTitle("Step 1 from 3");
        getToolbar().addCommandToRightBar(getCloseCommand());

        initComponents();
        attachComponentsToForm();

        setEditOnShow(loginField.getTextField());
        Validator.setValidateOnEveryKey(false);
    }

    private void attachComponentsToForm() {
        add(BoxLayout.encloseXCenter(new Label(logoImage)));
        mobileNumber.setEnabled(false);
        add(mobileNumber);
        add(loginField);
        add(errorContainer);
//        add(BoxLayout.encloseXCenter(alreadyHaveAccountButton));
        submitButton.bindFabToContainer(this);
        setScrollableY(true);
    }

    private void initComponents() {
        ActionListener<?> submitAction = createSubmitAction();
        submitButton.addActionListener(submitAction);
        loginField.getTextField().addActionListener(submitAction);

        errorTimeLabel.setVisible(false);
        errorText.setVisible(false);
        errorText.getAllStyles().setPaddingRight(1);
        errorTimeLabel.getAllStyles().setPaddingLeft(0);

//        alreadyHaveAccountButton.addActionListener(evt -> {
//            new LoginForm().show();
//        });
    }

    private Command getCloseCommand() {
        FontImage mat = FontImage.createMaterial(FontImage.MATERIAL_CLOSE, "", 4.5f);
        return Command.create("", mat, e -> {

            setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, false, 300));
            Component currEditing = getCurrentForm().findCurrentlyEditingComponent();
            if (currEditing != null) {
                currEditing.stopEditing(() -> MainForm.get().show());
            } else {
                MainForm.get().show();
            }
        });
    }

    private ActionListener<?> createSubmitAction() {
        return e -> {
            if (FabProgress.isInProgress()) return;
            loginField.getTextField().stopEditing();
            Validator.setValidateOnEveryKey(true);

            errorText.setVisible(false);
            if (!loginField.isValid()) {
                showError(loginField.getErrorMessage());
                return;
            }
            FabProgress.bind(submitButton);
            String formattedPhoneNumber = loginField.getFullPhoneNumber();

            UserService.validateUserPhone(loginField.getFullPhoneNumber(), new Callback<RegisterInitResponse>() {
                @Override
                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                    if (errorCode == 409) {
                        LoginForm loginForm = new LoginForm();
                        loginForm.setPredefinedInfo(loginField.getPhoneNumber(), "This number is already registered. Please enter your password");
                        loginForm.show();
                    } else {
                        showError(errorMessage);
                    }
                    FabProgress.stop();
                }

                @Override
                public void onSucess(RegisterInitResponse response) {
                    RegisterVerificationCodeStep2 step2Form = new RegisterVerificationCodeStep2(getCurrentForm(),
                            formattedPhoneNumber, response);
                    step2Form.show();
                    FabProgress.stop();
                }
            });
        };
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.revalidateWithAnimationSafety();
        errorText.setVisible(true);
        errorText.animateLayoutFade(300, 0);
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
