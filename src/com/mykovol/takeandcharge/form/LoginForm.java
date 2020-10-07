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

import com.codename1.components.SpanLabel;
import com.codename1.social.LoginCallback;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.validation.Validator;
import com.mykovol.takeandcharge.form.component.PasswordFieldContainer;
import com.mykovol.takeandcharge.form.component.PhoneFieldContainer;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.tools.CommonCode;
import com.mykovol.takeandcharge.tools.InfinityProgressBlocking;


/**
 * The Login form
 *
 * @author Vlad Mykol
 */
public class LoginForm extends Form {

    private final Validator phoneValidator = new Validator();
    private final Validator passwordValidator = new Validator();
    private final PasswordFieldContainer passwordFieldContainer = new PasswordFieldContainer();
    private final SpanLabel errorLabel = new SpanLabel("Password error", "LoginError");
    private final Button loginButton = new Button("Log in", "LoginButton");
    private final PhoneFieldContainer phoneFieldContainer = new PhoneFieldContainer();
    private final Label headerText = new Label("Log in", "LoginHeader");


    public LoginForm() {
        super(BoxLayout.y());
        setToolbar(new Toolbar(false));
        setFormBottomPaddingEditingMode(true);
//        CommonCode.removeTransitionsTemporarily(previous);
        // We remove the extra space for low resolution devices so things fit better
        Label spaceLabel = new Label(" ");
        if (!Display.getInstance().isTablet() && Display.getInstance().getDeviceDensity() < Display.DENSITY_HD) {
            spaceLabel = new Label();
            setTitle(headerText.getText());
            headerText.setHidden(true);
            spaceLabel.setHidden(true);
        }


        getToolbar().addCommandToRightBar(CommonCode.getCloseCommand(MainForm.get()));

        getContentPane().getAllStyles().setMarginUnit(Style.UNIT_TYPE_DIPS);
        getContentPane().getAllStyles().setMargin(0, 4, 3.5f, 3.5f);
//        Image LogoImage = Resources.getGlobalResources().getImage("main-logo.png");
//        Label logoImageHolder = new ScaleImageLabel(LogoImage);
//        logoImageHolder.setUIID("TextAlignCenter");
//        logoImageHolder.getAllStyles().setMarginTop(10);
//        logoImageHolder.setName("LogoImageName");
//        Container welcomeText = FlowLayout.encloseCenter(
//                loginHeader,
//                welcomeLabel2
//        );

        Validator.setValidateOnEveryKey(true);
        phoneValidator.addSubmitButtons(loginButton);
        phoneValidator.setValidationFailureHighlightMode(Validator.HighlightMode.NONE);
        phoneFieldContainer.setValidator(phoneValidator);

        passwordValidator.setValidationFailureHighlightMode(Validator.HighlightMode.NONE);
        passwordFieldContainer.setValidator(passwordValidator);

        errorLabel.setHidden(true);
        loginButton.addActionListener(evt -> {

            errorLabel.setHidden(true);
            if (!isValid()) {
                return;
            }

            setEditOnShow(null);
            InfinityProgressBlocking.start();
            UserService.login(phoneFieldContainer, passwordFieldContainer.getValue(), new LoginCallback() {
                @Override
                public void loginSuccessful() {
                    setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, true, 300));
                    InfinityProgressBlocking.stop();
                    MainForm.get().show();
                }

                @Override
                public void loginFailed(String errorMessage) {
                    InfinityProgressBlocking.stop();
                    showError(errorMessage);
                }
            });
        });

        Button forgotPassButton = new Button("Forgot password?", "LoginForgotLabel");
        forgotPassButton.addActionListener(evt -> {
            final SingUpForm resetPasswordForm = new SingUpForm();
            resetPasswordForm.setHeader("Reset password");
            resetPasswordForm.show();
        });
        Button singUp = new Button("Sing Up", "LoginForgotLabel");
        singUp.addActionListener(evt -> {
            new SingUpForm().show();
        });
        Label dotLabel = new Label("", "LoginForgotLabel");
        dotLabel.setMaterialIcon(FontImage.MATERIAL_FIBER_MANUAL_RECORD, 1.5f);
        dotLabel.getAllStyles().setMarginUnit(Style.UNIT_TYPE_DIPS);
        dotLabel.getAllStyles().setMarginLeft(2);
        dotLabel.getAllStyles().setMarginRight(2);


        addAll(
                headerText,
                spaceLabel,
                phoneFieldContainer,
                passwordFieldContainer,
                errorLabel,
                loginButton,
                FlowLayout.encloseCenter(singUp, dotLabel, forgotPassButton)
        );
        setScrollableY(true);
        setScrollVisible(false);
        setTensileDragEnabled(false);

        setEditOnShow(phoneFieldContainer.getField());
        phoneFieldContainer.getField().setNextFocusDown(passwordFieldContainer.getField());
        passwordFieldContainer.getField().setNextFocusDown(loginButton);
    }

    public void predefinePhone(String phone){
        phoneFieldContainer.getField().setText(phone);
        setEditOnShow(passwordFieldContainer.getField());
    }

    private void showError(String errorMessage) {
        errorLabel.setText(errorMessage);
        errorLabel.setHidden(false);
        errorLabel.getParent().animateLayoutFade(300, 0);
    }

    private boolean isValid() {
        if (!phoneValidator.isValid()) {
            showError(phoneFieldContainer.getErrorMessage());
        } else if (!passwordValidator.isValid()) {
            showError(passwordFieldContainer.getErrorMessage());
        }

        return phoneValidator.isValid() && passwordValidator.isValid();
    }

}
