package com.mykovol.takeandcharge.form.component;


import com.codename1.ui.*;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.validation.LengthConstraint;
import com.codename1.ui.validation.Validator;

public class PasswordFieldContainer extends Container {
    private final TextField textField = new TextField("", "****", 20, TextField.PASSWORD);
    private final Button showHidePassButton = new Button(FontImage.MATERIAL_VISIBILITY_OFF, "LoginTextIcon");
    private final Label textLabel = new Label("Password", "LoginLabel");
    private Validator validator = new Validator();


    public PasswordFieldContainer() {
        super(BoxLayout.y());
        textField.setUIID("LoginText");
        textField.getAllStyles().setMarginRight(0);
        showHidePassButton.getAllStyles().setMarginLeft(0);

        showHidePassButton.addActionListener(evt -> {
            if (textField.getConstraint() == TextField.PASSWORD) {
                textField.setConstraint(TextField.ANY);
                showHidePassButton.setMaterialIcon(FontImage.MATERIAL_VISIBILITY);
            } else {
                textField.setConstraint(TextField.PASSWORD);
                showHidePassButton.setMaterialIcon(FontImage.MATERIAL_VISIBILITY_OFF);
            }
//            if (textField.isEditing()) {
//                textField.stopEditing();
//                textField.startEditingAsync();
//            } else {
                textField.getParent().revalidate();
//            }
        });

        setScrollableY(false);


        add(textLabel);
        add(BorderLayout.centerCenterEastWest(textField, showHidePassButton, null));
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
        validator.addConstraint(textField, new LengthConstraint(4, "Password should contain at least 4 symbols"));
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

    public void setLabelText(String text) {
        textLabel.setText(text);
    }
}