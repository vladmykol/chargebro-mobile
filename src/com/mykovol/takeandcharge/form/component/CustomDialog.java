package com.mykovol.takeandcharge.form.component;


import com.codename1.components.SpanLabel;
import com.codename1.ui.*;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.Style;

import static com.codename1.ui.CN.getCurrentForm;

public class CustomDialog {
    private final Dialog dlg = new Dialog("");
    private Container mainContainer;

    public CustomDialog(String titleText, String bodyText) {
        this(titleText, bodyText, true);
    }

    public CustomDialog(String titleText, String bodyText, boolean disposeOnOutOfBounds) {
        dlg.setDialogUIID("CustomDialog");
        dlg.setDisposeWhenPointerOutOfBounds(disposeOnOutOfBounds);

        Style dlgStyle = dlg.getDialogStyle();
        dlgStyle.setBorder(Border.createEmpty());
        dlgStyle.setBgTransparency(0);
//        dlgStyle.setBgColor(0xffffff);

        SpanLabel customDialogTitle = new SpanLabel("", "CustomDialogTitle");
        customDialogTitle.setText(titleText);
        customDialogTitle.setEnabled(false);

        SpanLabel body = new SpanLabel(bodyText, "CustomDialogBody");
        body.setEnabled(false);

        mainContainer = BoxLayout.encloseYCenter(customDialogTitle, body);
        dlg.add(mainContainer);
    }

    public void addOkButton() {
        addButton("OK", evt -> {
            dlg.dispose();
        });
    }

    public void addButton(String text, ActionListener<?> actionListener) {
        Button button = new Button(text, "CustomDialogButton");
        button.addActionListener(evt -> dlg.dispose());
        button.addActionListener(actionListener);

        final Label panelDelimiterLabel = new Label("", "CustomDialogDelimiter");
        panelDelimiterLabel.setShowEvenIfBlank(true);
        mainContainer.add(panelDelimiterLabel);
        mainContainer.add(button);
    }

    public void addYesCancelButtons(String okButtonText, ActionListener<?> okAction) {
        Button okButton = new Button(okButtonText, "CustomDialogButtonRight");
        okButton.addActionListener(evt -> {
            dlg.dispose();
        });
        okButton.addActionListener(okAction);
        Button exitButton = new Button("Cancel", "CustomDialogButtonLeft");
        exitButton.addActionListener(evt -> {
            dlg.dispose();
        });

        final Label panelDelimiterLabel = new Label("", "CustomDialogDelimiter");
        panelDelimiterLabel.setShowEvenIfBlank(true);

        mainContainer.add(panelDelimiterLabel);

        final Label verticalDelimiterLabel = new Label("", "CustomDialogVerticalDelimiter");
        verticalDelimiterLabel.setShowEvenIfBlank(true);

        mainContainer.add(GridLayout.encloseIn(2, okButton, exitButton));
    }


    public void showWithAnimationSafety() {
        Form form = getCurrentForm();
        if (form.getAnimationManager().isAnimating()) {
            form.getAnimationManager().flushAnimation(dlg::show);
        } else {
            dlg.show();
        }
    }

    public void addRatingStarts() {
        Slider rate = createStarRankSlider();
        mainContainer.add(FlowLayout.encloseCenterMiddle(rate));
    }

    private Slider createStarRankSlider() {
        Slider starRank = new Slider();
        starRank.setEditable(true);
        starRank.setMinValue(0);
        starRank.setMaxValue(10);
        starRank.setProgress(10);
        Font fnt = Font.createTrueTypeFont("native:MainLight", "native:MainLight").
                derive(Display.getInstance().convertToPixels(5, true), Font.STYLE_PLAIN);
        Style s = new Style(0xffff33, 0, fnt, (byte) 0);
        Image fullStar = FontImage.createMaterial(FontImage.MATERIAL_STAR, s).toImage();
        s.setOpacity(100);
        s.setFgColor(0);
        Image emptyStar = FontImage.createMaterial(FontImage.MATERIAL_STAR, s).toImage();
        initStarRankStyle(starRank.getSliderEmptySelectedStyle(), emptyStar);
        initStarRankStyle(starRank.getSliderEmptyUnselectedStyle(), emptyStar);
        initStarRankStyle(starRank.getSliderFullSelectedStyle(), fullStar);
        initStarRankStyle(starRank.getSliderFullUnselectedStyle(), fullStar);
        starRank.setPreferredSize(new Dimension((int) (fullStar.getWidth() * 4.5f), fullStar.getHeight()));
        return starRank;
    }

    private void initStarRankStyle(Style s, Image star) {
        s.setBackgroundType(Style.BACKGROUND_IMAGE_TILE_BOTH);
        s.setBorder(Border.createEmpty());
        s.setBgImage(star);
        s.setBgTransparency(0);
    }


    public void showOk() {
        addOkButton();
        showWithAnimationSafety();
    }
}