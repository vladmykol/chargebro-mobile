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
import com.codename1.ui.*;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.validation.Validator;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.RegisterInitResponse;
import com.mykovol.takeandcharge.form.component.PhoneFieldContainer;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.tools.CommonCode;
import com.mykovol.takeandcharge.tools.FormCommand;
import com.mykovol.takeandcharge.tools.InfinityProgressBlocking;

import static com.codename1.ui.CN.SOUTH;
import static com.codename1.ui.CN.getCurrentForm;
import static com.mykovol.takeandcharge.service.GlobalConst.POLICY_URL;


/**
 * The Login form
 *
 * @author Vlad Mykol
 */
public class RegistrationForm extends Form {

    private final Validator phoneValidator = new Validator();
    private final SpanLabel errorLabel = new SpanLabel("Password error", "LoginError");
    private final PhoneFieldContainer phoneFieldContainer = new PhoneFieldContainer();
    private final Label headerText = new Label("Log in", "LoginHeader");

    public RegistrationForm() {
        super(new BorderLayout());
//        CommonCode.removeTransitionsTemporarily(previous);
        setFormBottomPaddingEditingMode(true);
        setToolbar(new Toolbar(false));
        Label spaceLabel = new Label(" ");
        if (!Display.getInstance().isTablet() && Display.getInstance().getDeviceDensity() < Display.DENSITY_HD) {
            spaceLabel = new Label();
            setTitle(headerText.getText());
            headerText.setHidden(true);
            spaceLabel.setHidden(true);
        }

        FormCommand.setCloseAction(MainForm.get(), this);

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
        //    private final SpanLabel infoLabel = new SpanLabel("Your phone number will be used to send varification SMS and won’t be forwarded to third parties"
        //            , "LoginInfo");
        Button submitButton = new Button("OK", "LoginButton");
        phoneValidator.addSubmitButtons(submitButton);
        phoneValidator.setValidationFailureHighlightMode(Validator.HighlightMode.NONE);
        phoneFieldContainer.setValidator(phoneValidator);

        errorLabel.setEnabled(false);
        errorLabel.setHidden(true);
        submitButton.addActionListener(evt -> {
            errorLabel.setHidden(true);
            if (!phoneValidator.isValid()) {
                showValidatorError();
                return;
            }

            InfinityProgressBlocking.get().start(this);
            UserService.validateUserPhone(phoneFieldContainer.getFullPhoneNumber(), new Callback<RegisterInitResponse>() {
                @Override
                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                    InfinityProgressBlocking.get().stop();
                    showError(errorMessage);
                }

                @Override
                public void onSucess(RegisterInitResponse response) {
                    RegisterFormConfirmation step2Form = new RegisterFormConfirmation(getCurrentForm(),
                            phoneFieldContainer, response);
                    step2Form.show();
                }
            });
        });

        final Container mainContainer = BoxLayout.encloseY(
                headerText,
                spaceLabel,
                phoneFieldContainer,
                errorLabel,
                submitButton
        );
        mainContainer.setScrollableY(true);
        mainContainer.setScrollVisible(false);
        mainContainer.setTensileDragEnabled(false);
        add(CENTER, mainContainer);

        final Label termsLabel = new Label("By continuing you are", "LoginTermsText");
        final Label termsLabelSpace = new Label(" ", "LoginTermsText");
        final Label termsLabel2 = new Label("indicating that you are", "LoginTermsText");
        final Label termsLabel2Space = new Label(" ", "LoginTermsText");
        final Label termsLabel3 = new Label("agree to the", "LoginTermsText");
        final Label termsLabel3Space = new Label(" ", "LoginTermsText");
        final Button termsLinkButton = new Button("Terms", "LoginTermsLink");
        final BrowserPopUp termsForm = new BrowserPopUp("Terms&Conditions");
        termsForm.setFadeBackDownTo(this);
        termsLinkButton.addActionListener(evt -> {
            CommonCode.removeTransitionsTemporarily(this);
            termsForm.show();
            termsForm.serUrlNoReload(POLICY_URL);
        });
//        final Label termsLinkButtonSpace = new Label(" ", "LoginTermsText");
//        final Label andLabel = new Label("and", "LoginTermsText");
//        final Label andLabelSpace = new Label(" ", "LoginTermsText");
//        final Button privacyLinkButton = new Button("Privacy Policy", "LoginTermsLink");
//        privacyLinkButton.addActionListener(evt -> {
//            CommonCode.removeTransitionsTemporarily(this);
//            termsForm.show();
//        });

        final Container termsContainer = FlowLayout.encloseCenter(
                termsLabel,
                termsLabelSpace,
                termsLabel2,
                termsLabel2Space,
                termsLabel3,
                termsLabel3Space,
                termsLinkButton
//                termsLinkButtonSpace,
//                andLabel,
//                andLabelSpace,
//                privacyLinkButton
        );
        add(SOUTH, termsContainer);
        termsContainer.setScrollableY(false);

        setEditOnShow(phoneFieldContainer.getField());
        phoneFieldContainer.getField().setNextFocusDown(submitButton);
    }

    public void setHeader(String text) {
        if (headerText.isHidden()) {
            setTitle(text);
        } else {
            headerText.setText(text);
        }
    }

    private void showError(String errorMessage) {
        errorLabel.setText(errorMessage);
        errorLabel.revalidateWithAnimationSafety();
        errorLabel.setHidden(false);
        errorLabel.getParent().animateLayoutFade(300, 0);
    }

    private void showValidatorError() {
        final String errorMessage1 = phoneFieldContainer.getErrorMessage();
        if (errorMessage1 != null) {
            showError(errorMessage1);
        }
    }
}
