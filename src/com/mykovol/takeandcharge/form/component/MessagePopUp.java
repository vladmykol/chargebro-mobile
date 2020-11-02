package com.mykovol.takeandcharge.form.component;


import com.codename1.components.SpanLabel;
import com.codename1.ui.*;
import com.codename1.ui.geom.Rectangle;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.UITimer;
import com.mykovol.takeandcharge.form.MainForm;

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
        Container topPlaceHolder = new Container(new FlowLayout());
        Rectangle rectOfSafeArea = Display.getInstance().getDisplaySafeArea(new Rectangle());
        int topMargin = rectOfSafeArea.getY();
        if (topMargin == 0) {
            topPlaceHolder.getAllStyles().setMarginUnit(Style.UNIT_TYPE_SCREEN_PERCENTAGE);
            topMargin = 3;
        } else {
            topPlaceHolder.getAllStyles().setMarginUnit(Style.UNIT_TYPE_PIXELS);
        }
        topPlaceHolder.getAllStyles().setMarginTop(topMargin);
        add(topPlaceHolder);
    }

    public static String errorCodeToString(int errorCode) {
        if (errorCode == MESSAGE_CODE_PAYMENT_ERROR) {
            return "Payment issue";
        } else if (errorCode == MESSAGE_CODE_GENERAL_ERROR) {
            return "Oops... something went wrong";
        } else {
            return "Error";
        }
    }

    public void showError(int type, String text) {
        showMessage(text, type, null);
    }

    public void showInfo(String title, String text) {
        showMessage(text, 0, title);
    }

    public void showMessage(String text, int errorType, String title) {
        if (text == null) return;
        if (text.equals(lastErrorTest)) return;
        lastErrorTest = text;

        Container container = new Container(BoxLayout.x(), "ErrorMessageHolder");

        Label errorDotImage = new Label("", "ErrorMessageIcon");
        if (errorType == 0) {
            errorDotImage.setUIID("InfoMessageIcon");
            FontImage.setMaterialIcon(errorDotImage, FontImage.MATERIAL_CHARGING_STATION);
        } else {
            FontImage.setMaterialIcon(errorDotImage, FontImage.MATERIAL_ERROR_OUTLINE);
        }

        SpanLabel errorMessageText = new SpanLabel(text, "ErrorMessageText");
        errorMessageText.setEnabled(false);
        SpanLabel errorMessageHeader = new SpanLabel("", "ErrorMessageHeader");
        if (errorType == 0) {
            errorMessageHeader.setText(title);
        } else {
            errorMessageHeader.setText(errorCodeToString(errorType));
        }
        errorMessageHeader.setEnabled(false);
        final Container errorMessageContainer = BoxLayout.encloseY(errorMessageHeader, errorMessageText);
        container.addAll(errorDotImage, errorMessageContainer);
        Container animatedContainer = BoxLayout.encloseY(container);

        Form f = CN.getCurrentForm();
        if (f.getAnimationManager().isAnimating()) {
            f.getAnimationManager().flushAnimation(() -> {
                showMsg(animatedContainer);
            });
        } else {
            showMsg(animatedContainer);
        }

        UITimer.timer(7000, false, MainForm.get(), () -> {
            lastErrorTest = null;
            Form currentForm = CN.getCurrentForm();
            if (currentForm.getAnimationManager().isAnimating()) {
                currentForm.getAnimationManager().flushAnimation(() -> {
                    hideMsg(container, animatedContainer);
                });
            } else {
                hideMsg(container, animatedContainer);
            }
        });
    }

    public void showMsg(Container animatedContainer) {
        add(animatedContainer);
//            errorMessageContainer.revalidate();
        animatedContainer.revalidate();

        animatedContainer.setY(-100);
        animateLayoutAndWait(300);
    }

    public void hideMsg(Container container, Container animatedContainer) {
        container.setX(getDisplayWidth());
        animatedContainer.animateUnlayout(700, 50, () -> {
            animatedContainer.remove();
            animateLayoutAndWait(100);
            getParent().revalidate();
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