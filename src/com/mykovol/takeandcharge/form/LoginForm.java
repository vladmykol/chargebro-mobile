/*
 * Copyright (c) 2016, Codename One
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.mykovol.takeandcharge.form;

import com.codename1.components.FloatingActionButton;
import com.codename1.components.ScaleImageLabel;
import com.codename1.components.SpanLabel;
import com.codename1.social.LoginCallback;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.util.Resources;
import com.codename1.ui.validation.LengthConstraint;
import com.codename1.ui.validation.Validator;
import com.mykovol.takeandcharge.form.component.LoginField;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.tools.CommonCode;
import com.mykovol.takeandcharge.tools.FabProgress;


/**
 * The Login form
 *
 * @author Shai Almog
 */
public class LoginForm extends Form {

    private final Validator validator = new Validator();
    private final LoginField loginField = new LoginField();
    private final TextField passwordField = new TextField("", "Password", 20, TextField.PASSWORD);
    private final SpanLabel errorLabel = new SpanLabel("Password error", "ErrorLabel");


    public LoginForm() {
        super(new BorderLayout());
//        CommonCode.removeTransitionsTemporarily(previous);
        setToolbar(new Toolbar(true));
        getToolbar().addCommandToRightBar(constructCloseCommand());
        Image LogoImage = Resources.getGlobalResources().getImage("main-logo.png");
        Label logoImageHolder = new ScaleImageLabel(LogoImage);
        logoImageHolder.setUIID("TextAlignCenter");
        logoImageHolder.getAllStyles().setMarginTop(10);
        logoImageHolder.setName("LogoImageName");

        Container welcomeText = FlowLayout.encloseCenter(
                new Label("Welcome to", "WelcomeText"),
                new Label(" Take&Charge", "WelcomeText2")
        );

        passwordField.setUIID("CredentialsField");

        passwordField.getAllStyles().setMargin(LEFT, 0);
        Label passwordIcon = new Label("", "CredentialsField");
        passwordIcon.setShowEvenIfBlank(true);
        passwordIcon.getAllStyles().setMargin(RIGHT, 0);
        FontImage.setMaterialIcon(passwordIcon, FontImage.MATERIAL_LOCK_OUTLINE, 3);

        errorLabel.setVisible(false);

        validator.addConstraint(passwordField, new LengthConstraint(4, "Password should contain at least 4 symbols"));
        Validator.setValidateOnEveryKey(true);

        Button forgot = new Button("Forgot password", "ForgotPasRegisterLabel");
//        Button newAccountButton = new Button("Create new account", "ForgotPasRegisterLabel");
        Container registerOrForgot = BoxLayout.encloseY(forgot);
        forgot.getAllStyles().setMarginBottom(3);
//        newAccountButton.getAllStyles().setMarginBottom(3);

//        newAccountButton.addActionListener(evt -> {
//            new RegisterMobileNumberStep1().show();
//        });

        forgot.addActionListener(evt -> {

        });

        // We remove the extra space for low resolution devices so things fit better
        Label spaceLabel;
        if (!Display.getInstance().isTablet() && Display.getInstance().getDeviceDensity() < Display.DENSITY_VERY_HIGH) {
            spaceLabel = new Label();
        } else {
            spaceLabel = new Label(" ");
        }

        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_ARROW_FORWARD);
//        validator.addSubmitButtons(fab);
        fab.bindFabToContainer(this);
        ActionListener<?> loginButtonAction = loginButtonAction(fab);
        fab.addActionListener(loginButtonAction);
        passwordField.addActionListener(loginButtonAction);

        Container mainContainer = BoxLayout.encloseY(
                logoImageHolder,
                welcomeText,
                spaceLabel,
                loginField,
                BorderLayout.center(passwordField).
                        add(BorderLayout.WEST, passwordIcon),
                errorLabel
        );
        add(BorderLayout.CENTER, mainContainer);
        add(BorderLayout.SOUTH, registerOrForgot);
//        mainContainer.setScrollableY(true);
//        mainContainer.setScrollVisible(false);
        setScrollableY(true);

        setEditOnShow(loginField.getTextField());
        loginField.getTextField().setNextFocusDown(passwordField);

    }

    public void setPredefinedInfo(String phoneNumber, String message) {
        loginField.getTextField().setText(phoneNumber);
        loginField.getTextField().setEditable(false);
        setEditOnShow(passwordField);
        showError(message);
    }

    private Command constructCloseCommand() {
        FontImage mat = FontImage.createMaterial(FontImage.MATERIAL_CLOSE, "", 4.5f);
        return Command.create("", mat, e -> {

            setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, false, 300));
            Component currEditing = this.findCurrentlyEditingComponent();
            if (currEditing != null) {
                currEditing.stopEditing(() -> MainForm.get().show());
            } else {
                MainForm.get().show();
            }
        });
    }

    private ActionListener<?> loginButtonAction(FloatingActionButton fab) {
        return evt -> {
            if (FabProgress.isInProgress()) return;
            Validator.setValidateOnEveryKey(true);

            errorLabel.setVisible(false);
            if (!loginField.isValid()) {
                showError(loginField.getErrorMessage());
                return;
            }
            if (!validator.isValid()) {
                showError(validator.getErrorMessage(passwordField));
                return;
            }

            FabProgress.bind(fab);

            UserService.login(loginField.getFullPhoneNumber(), passwordField.getText(), new LoginCallback() {
                @Override
                public void loginSuccessful() {
                    setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, true, 300));

                    MainForm.get().show();
                    FabProgress.stop();
                }

                @Override
                public void loginFailed(String errorMessage) {
                    FabProgress.stop();
                    showError(errorMessage);
                }
            });
        };
    }

    private void showError(String errorMessage) {
        errorLabel.setText(errorMessage);
        errorLabel.revalidateWithAnimationSafety();
        errorLabel.setVisible(true);
        errorLabel.animateLayoutFade(300, 0);
    }

}
