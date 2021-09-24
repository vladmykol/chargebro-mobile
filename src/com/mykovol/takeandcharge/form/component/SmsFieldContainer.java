package com.mykovol.takeandcharge.form.component;


import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.util.UITimer;
import com.codename1.ui.validation.RegexConstraint;
import com.codename1.ui.validation.Validator;

public class SmsFieldContainer extends Container {
    private final TextField textField = new TextField("", "- - - -", 4, TextField.NUMERIC);
    private final Label resentLabel = new Label("code is valid for", "LoginLabel");
    private final Button resendButton = new Button("Resend code", "LoginForgotLabel");
    private Validator validator = new Validator();
    private String registerCode;
    private int validForSeconds;
    private UITimer timer;


    public SmsFieldContainer(String registerCode, int validForSeconds, Form currentForm, Form previousForm) {
        super(BoxLayout.y());
        this.registerCode = registerCode;
        this.validForSeconds = validForSeconds;

        textField.setUIID("LoginText");
        textField.setMaxSize(registerCode.length());

        FontImage.setMaterialIcon(resendButton, FontImage.MATERIAL_REPLAY);
        Label resentTimeLabel = new Label("", "LoginLabel");
        resentLabel.getAllStyles().setPaddingRight(0.7f);
        resentTimeLabel.getAllStyles().setPaddingLeft(0);
        Container resendContainer = FlowLayout.encloseRight(resentLabel, resentTimeLabel);

        startResendTimer(resendContainer, resentTimeLabel, currentForm);

        setScrollableY(false);

        addAll(new Label("SMS code", "LoginLabel"),
                textField,
                resendContainer);
        resendButton.addActionListener(evt -> {
            previousForm.show();
        });
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
        addValidations();
    }

    @Override
    public void setHidden(boolean isHidden) {
        super.setHidden(isHidden);
        textField.setHidden(isHidden);
    }

    public String getValue() {
        return textField.getText();
    }

    private void addValidations() {
        validator.addConstraint(textField, new RegexConstraint("^" + registerCode + "$", "SMS code is incorrect"));
    }

    public boolean isValid() {
        return validator.isValid();
    }

    public String getErrorMessage() {
        return validator.getErrorMessage(textField);
    }

    public TextField getField() {
        return textField;
    }

    public void startResendTimer(Container resendContainer, Label resentTimeLabel, Form currentForm) {
        resentTimeLabel.setText(formatSeconds(validForSeconds));
        timer = UITimer.timer(1000, true, currentForm, () -> {
            if (validForSeconds > 0) {
                validForSeconds--;
                resentTimeLabel.setText(formatSeconds(validForSeconds));
                return;
            }
            timer.cancel();
            resentTimeLabel.setHidden(true, true);
            resendContainer.replace(resentLabel, resendButton, CommonTransitions.createFade(50));
        });
    }

    private String formatSeconds(int time) {
        return twoDigits(time / 60) + ":" + twoDigits(time % 60) + " ";
    }

    private String twoDigits(int t) {
        if (t < 10) {
            return "0" + t;
        }
        return "" + t;
    }


}