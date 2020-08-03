package com.mykovol.takeandcharge.form.component;


import com.codename1.ui.*;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.validation.RegexConstraint;
import com.codename1.ui.validation.Validator;
import com.codename1.util.StringUtil;
import com.mykovol.takeandcharge.service.RegisterStyle;

public class LoginField extends Container {
    private final TextField textField = new TextField("", "(93) 123-45-67", 40, TextField.PHONENUMBER);
    private final Button countryCodeButton = new Button("+380", RegisterStyle.TEXT_FIELD);
    private final Validator validator = new Validator();

    public LoginField() {
        super(new BorderLayout());
        Label loginIcon = new Label("", RegisterStyle.TEXT_FIELD);
        loginIcon.setShowEvenIfBlank(true);
        loginIcon.getAllStyles().setMargin(RIGHT, 0);
        FontImage.setMaterialIcon(loginIcon, FontImage.MATERIAL_PHONE, 3);
        textField.setMaxSize(10);
        textField.setUIID(RegisterStyle.TEXT_FIELD);
        countryCodeButton.getAllStyles().setMargin(RIGHT, 0);
        countryCodeButton.getAllStyles().setMargin(LEFT, 0);
        textField.getAllStyles().setMargin(LEFT, 0);
        loginIcon.getAllStyles().setMarginRight(0);


        Style ps = textField.getUnselectedStyle();
        Style cs = countryCodeButton.getUnselectedStyle();
        int pl = cs.getPaddingLeft(isRTL());
        int pr = cs.getPaddingRight(isRTL());
        countryCodeButton.getAllStyles().setPaddingUnit(Style.UNIT_TYPE_PIXELS);
        countryCodeButton.getAllStyles().setPadding(ps.getPaddingTop(), ps.getPaddingBottom(), pl, pr);

        addValidations();

        add(BorderLayout.CENTER, textField);
        add(BorderLayout.WEST, BoxLayout.encloseX(loginIcon, countryCodeButton));

    }

    public TextField getTextField() {
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


}