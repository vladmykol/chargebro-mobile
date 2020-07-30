package com.mykovol.takeandcharge.form;

import com.codename1.components.ScaleImageLabel;
import com.codename1.components.SpanLabel;
import com.codename1.ui.*;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.plaf.RoundBorder;
import com.codename1.ui.util.Effects;
import com.codename1.ui.util.Resources;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.StationInfo;
import com.mykovol.takeandcharge.service.RentService;

public class StationInfoSheet extends Sheet {

    private final Label availablePowerBanksNumber = new Label("0", "StationsSheetNumberAvailable");
    private final Label cabBeReturnedPowerBanksNumber = new Label("0", "StationsSheetNumberCanBeReturned");
    private final SpanLabel addressLabel = new SpanLabel("", "StationsSheetAddress");
    private final SpanLabel title = new SpanLabel("", "StationsSheetTitle");
    private final SpanLabel errorLabel = new SpanLabel("something went wrong", "ErrorText");
    private final ScaleImageLabel placeLogoImageLabel;
    private final Container availableContainer;
    private String directionUrl;

    StationInfoSheet() {
        super(null, "");
        setPosition(BorderLayout.NORTH);
        Container cnt = getContentPane();
        errorLabel.setEnabled(false);
//        errorLabel.stripMarginAndPadding();
//        setLeadComponent(cnt);
//        cnt.setLayout(BoxLayout.y());
        cnt.setScrollableY(false);


        int size = Display.getInstance().convertToPixels(1f);
        Image placeImage = Effects.dropshadow(Resources.getGlobalResources().getImage("no-logo.png"), 10, 120, size, size);
        placeLogoImageLabel = new ScaleImageLabel(placeImage);
        placeLogoImageLabel.setUIID("StationsSheetImage");

        addressLabel.setEnabled(false);
        FontImage.setMaterialIcon(addressLabel, FontImage.MATERIAL_PLACE);

        Label availablePowerBanks = new Label("Available:", "StationsSheetText");
        Container availableHolder = FlowLayout.encloseIn(availablePowerBanks, availablePowerBanksNumber);


        Label cabBeReturnedPowerBanks = new Label("Can be returned:", "StationsSheetText");
        Container canBeReturnedHolder = FlowLayout.encloseIn(cabBeReturnedPowerBanks, cabBeReturnedPowerBanksNumber);

        availableContainer = BoxLayout.encloseY(availableHolder, canBeReturnedHolder);
        Container infoContainer = BoxLayout.encloseY(addressLabel, availableContainer);

        Button getDirectionButton = new Button("");
        getDirectionButton.setUIID("GetDirectionButton");

        FontImage.setMaterialIcon(getDirectionButton, FontImage.MATERIAL_DIRECTIONS);
        getDirectionButton.getAllStyles().setBorder(
                RoundBorder.create().color(0xffffffff).shadowOpacity(60)
        );

//        title.setSafeArea(true);
        title.setEnabled(false);

        cnt.addAll(BoxLayout.encloseX(placeLogoImageLabel, infoContainer),
                FlowLayout.encloseLeftMiddle(errorLabel),
                FlowLayout.encloseRightBottom(getDirectionButton));
        add(BorderLayout.NORTH, title);

//        cnt.addPointerPressedListener(this::getDirectionButtonAction);
//        infoContainer.addPointerPressedListener(this::getDirectionButtonAction);
//        scaleImageLabel.addPointerPressedListener(this::getDirectionButtonAction);
        getDirectionButton.addActionListener(this::getDirectionButtonAction);

    }

    private void getDirectionButtonAction(ActionEvent evt) {
        if (directionUrl != null) Display.getInstance().execute(directionUrl);
    }

    public void show(StationInfo stationInfo) {
        errorLabel.setVisible(false);
        directionUrl = stationInfo.mapUrl.get();
        title.setText(stationInfo.placeName.get());
        addressLabel.setText(stationInfo.address.get());
        getContentPane().revalidate();
        super.show();
        RentService.getRemainingPowerBanks(stationInfo.id.get(), new Callback<Integer>() {
            @Override
            public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
//                callSerially(() -> {
                if (errorCode == 401) {
                    errorLabel.setText("You must login first");
                } else {
                    errorLabel.setText(errorMessage);
                }
                availablePowerBanksNumber.setText("0");
                cabBeReturnedPowerBanksNumber.setText("0");
                errorLabel.setVisible(true);
                errorLabel.getParent().animateLayoutFade(200, 0);
//                });
            }

            @Override
            public void onSucess(Integer remainingPowerBanks) {
                String remainingPowerBanksString = String.valueOf(remainingPowerBanks);
                String canBeReturnedString = String.valueOf(stationInfo.maxCapacity.getInt() - remainingPowerBanks);
                if (!availablePowerBanksNumber.getText().equals(remainingPowerBanksString) ||
                        !cabBeReturnedPowerBanksNumber.getText().equals(canBeReturnedString)) {
//                    callSerially(() -> {
                    availablePowerBanksNumber.setText(remainingPowerBanksString);
                    cabBeReturnedPowerBanksNumber.setText(canBeReturnedString);
                    availableContainer.animateLayoutFadeAndWait(200, 0);
//                    });

                }
            }
        });
    }
}
