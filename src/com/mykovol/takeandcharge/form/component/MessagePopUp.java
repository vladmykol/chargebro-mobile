package com.mykovol.takeandcharge.form.component;


import com.codename1.components.SpanLabel;
import com.codename1.ui.*;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.UITimer;

import static com.codename1.ui.CN.callSerially;
import static com.codename1.ui.CN.getDisplayWidth;

public class MessagePopUp extends Container {
    private static final int MESSAGE_CODE_PAYMENT_ERROR = 402;
    private static final int MESSAGE_CODE_UNAUTHORIZED = 401;
    private static final int MESSAGE_CODE_GENERAL_ERROR = 500;
    private static final int MESSAGE_CODE_SERVICE_UNAVAILABLE_ERROR = 503;
    private String lastErrorTest;

    public MessagePopUp() {
        super(BoxLayout.y());
        Container topPlaceHolder = new Container();
        topPlaceHolder.stripMarginAndPadding();
        topPlaceHolder.setSafeArea(true);
        add(topPlaceHolder);
    }

    public void showError(String text) {
        showError(text, 500);
    }

    public void showError(String text, int type) {
        if (text == null) return;
        if (text.equals(lastErrorTest)) return;
        lastErrorTest = text;

        Container container = new Container(BoxLayout.x(), "ErrorMessageHolder");

        Label errorDotImage = new Label("", "ErrorMessageIcon");
        FontImage.setMaterialIcon(errorDotImage, FontImage.MATERIAL_ERROR_OUTLINE);

        SpanLabel errorMessageText = new SpanLabel(text, "ErrorMessageText");
        errorMessageText.setEnabled(false);
        Label errorMessageHeader = new Label("Error", "ErrorMessageHeader");
        if (type == MESSAGE_CODE_PAYMENT_ERROR) {
            errorMessageHeader.setText("Payment issue");
        } else if (type == MESSAGE_CODE_GENERAL_ERROR) {
            errorMessageHeader.setText("Unexpected error");
        }
        final Container errorMessageContainer = BoxLayout.encloseY(errorMessageHeader, errorMessageText);
        container.addAll(errorDotImage, errorMessageContainer);
        Container animatedContainer = BoxLayout.encloseY(container);

        callSerially(() -> {
            add(animatedContainer);
//            errorMessageContainer.revalidate();
            animatedContainer.revalidate();

            animatedContainer.setY(0);
            animateLayoutAndWait(500);
        });

        UITimer.timer(7000, false, getComponentForm(), () -> {
            callSerially(() -> {
                container.setX(getDisplayWidth());
                animatedContainer.animateUnlayout(700, 50, () -> {
                    animatedContainer.remove();
                    lastErrorTest = null;
                    animateLayoutAndWait(100);
                });
            });
        });
    }

    public void bindToComponent(Component parentComponent) {
        Form f = parentComponent.getComponentForm();
        if (f != null && (f.getContentPane() == parentComponent || f == parentComponent)) {
            // special case for content pane installs the button directly on the content pane
            Container layers = f.getLayeredPane(getClass(), true);
            layers.setLayout(BoxLayout.y());
            layers.add(this);
        }
    }

}