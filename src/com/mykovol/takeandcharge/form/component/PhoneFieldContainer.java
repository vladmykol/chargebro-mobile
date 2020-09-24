package com.mykovol.takeandcharge.form.component;


import com.codename1.ui.Button;
import com.codename1.ui.Container;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.validation.RegexConstraint;
import com.codename1.ui.validation.Validator;
import com.codename1.util.StringUtil;

public class PhoneFieldContainer extends Container {
    private final TextField textField = new TextField("", "(93) 123-45-67", 40, TextField.PHONENUMBER);
    private final Button countryCodeButton = new Button("+380", "LoginText");
    private final Label textLabel;
    private Validator validator = new Validator();

    public PhoneFieldContainer() {
        super(BoxLayout.y());
        textField.setMaxSize(10);
        textField.setUIID("LoginText");
        countryCodeButton.getAllStyles().setMarginRight(2);
        textField.getAllStyles().setMarginLeft(0);

        setScrollableY(false);

        textLabel = new Label("Phone number", "LoginLabel");
        add(textLabel);
        add(BorderLayout.centerCenterEastWest(textField, null, countryCodeButton));
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
        addValidations();
    }

    @Override
    public void setHidden(boolean isHidden) {
        super.setHidden(isHidden);
        textField.setHidden(isHidden);
        countryCodeButton.setHidden(isHidden);
    }

    public TextField getField() {
        return textField;
    }

    private void addValidations() {
        String phoneRegExp = "^[0-9][0-9.-]{7,8}[0-9]$";
        validator.addConstraint(textField, new RegexConstraint(phoneRegExp,
                "Please enter a valid phone number"));
    }

    public boolean isValid() {
        return validator.isValid();
    }

    public String getErrorMessage() {
        return validator.getErrorMessage(textField);
    }

    public String getFullPhoneNumber() {
        return StringUtil.replaceAll(countryCodeButton.getText(), "+", "") + getPhoneNumber();
    }

    public String getPhoneNumber() {
        if (textField.getText().startsWith("0")) {
            return textField.getText().substring(1);
        }
        return textField.getText();
    }

    public void hideLabel() {
        textLabel.setVisible(false);
    }

    public String getFormattedPhoneNumber() {
        return formattedPhoneNumber(getFullPhoneNumber());
    }

    public static String formattedPhoneNumber(String phone) {
        StringBuilder stringBuffer = new StringBuilder(phone);
        stringBuffer.insert(0, "+");
        stringBuffer.insert(4, " (");
        stringBuffer.insert(8, ") ");
        stringBuffer.insert(13, "-");
        stringBuffer.insert(16, "-");
        return stringBuffer.toString();
    }


}