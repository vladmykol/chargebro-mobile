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
import com.codename1.ui.animations.MorphTransition;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.util.Resources;
import com.mykovol.takeandcharge.service.RentSocketService;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.tools.FabProgress;

import static com.codename1.ui.CN.callSerially;
import static com.codename1.ui.CN.getCurrentForm;

/**
 * The Login form
 *
 * @author Shai Almog
 */
public class LoginForm extends Form {

    private final TextField loginField = new TextField("", "380 (93) 123-45-56", 20, TextField.NUMERIC);
    private final TextField passwordField = new TextField("", "Password", 20, TextField.PASSWORD);

    public LoginForm() {
        super(new BorderLayout(BorderLayout.CENTER_BEHAVIOR_CENTER_ABSOLUTE));
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

        loginField.setUIID("CredentialsField");
        passwordField.setUIID("CredentialsField");

        loginField.getAllStyles().setMargin(LEFT, 0);
        passwordField.getAllStyles().setMargin(LEFT, 0);
        Label loginIcon = new Label("", "CredentialsField");
        loginIcon.setShowEvenIfBlank(true);
        Label passwordIcon = new Label("", "CredentialsField");
        passwordIcon.setShowEvenIfBlank(true);
        loginIcon.getAllStyles().setMargin(RIGHT, 0);
        passwordIcon.getAllStyles().setMargin(RIGHT, 0);
        FontImage.setMaterialIcon(loginIcon, FontImage.MATERIAL_PHONE, 3);
        FontImage.setMaterialIcon(passwordIcon, FontImage.MATERIAL_LOCK_OUTLINE, 3);

        SpanLabel error = new SpanLabel("Password error", "ErrorLabel");
        error.setVisible(false);

//        Validator validator = new Validator();
//        validator.addConstraint(login, new LengthConstraint(1, "cannot be blank"));
//        validator.addConstraint(password, new LengthConstraint(4, "at least 4 symbols"));

        Button forgot = new Button("Forgot password", "ForgotPasRegisterLabel");
        Button newAccountButton = new Button("Create new account", "ForgotPasRegisterLabel");
        Container registerOrForgot = BoxLayout.encloseY(forgot, newAccountButton);
        newAccountButton.getAllStyles().setMarginBottom(3);

        newAccountButton.addActionListener(evt -> {
            new RegisterMobileNumberStep1().show();
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
        ActionListener<?> loginButtonAction = loginButtonAction(loginField, passwordField, error, fab);
        fab.addActionListener(loginButtonAction);
        passwordField.addActionListener(loginButtonAction);

        Container mainContainer = BoxLayout.encloseY(
                logoImageHolder,
                welcomeText,
                spaceLabel,
                BorderLayout.center(loginField).
                        add(BorderLayout.WEST, loginIcon),
                BorderLayout.center(passwordField).
                        add(BorderLayout.WEST, passwordIcon),
                error
        );
        mainContainer.setScrollableY(true);
        add(BorderLayout.NORTH, mainContainer);
        add(BorderLayout.SOUTH, registerOrForgot);

        setEditOnShow(loginField);
        loginField.setNextFocusDown(passwordField);
        mainContainer.setScrollableY(false);
    }

    private Command constructCloseCommand() {
        FontImage mat = FontImage.createMaterial(FontImage.MATERIAL_CLOSE, "", 4.5f);
        return Command.create("", mat, e -> {

            MorphTransition morph = MorphTransition.create(400);
            setTransitionOutAnimator(morph);
            Component currEditing = this.findCurrentlyEditingComponent();
            if (currEditing != null) {
                currEditing.stopEditing(() -> MainForm.get().show());
            } else {
                MainForm.get().show();
            }
        });
    }

    private ActionListener<?> loginButtonAction(TextField login, TextField password, SpanLabel error, FloatingActionButton fab) {
        return evt -> {
            FabProgress.bind(fab);
            setEditOnShow(null);

            UserService.login(login.getText(), password.getText(), new LoginCallback() {
                @Override
                public void loginSuccessful() {
                    setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, true, 300));
                    RentSocketService.get().reconnect();
                    MainForm.get().show();
                    FabProgress.stop(fab);
                }

                @Override
                public void loginFailed(String errorMessage) {
                    error.setText(errorMessage);
                    error.setVisible(true);
                    error.getParent().revalidateWithAnimationSafety();
                    FabProgress.stop(fab);
                }
            });
        };
    }

}
