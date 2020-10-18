package com.mykovol.takeandcharge.form;

import com.codename1.components.SpanLabel;
import com.codename1.io.Preferences;
import com.codename1.ui.*;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.RoundBorder;
import com.codename1.ui.util.Effects;
import com.codename1.ui.util.Resources;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.StationInfo;
import com.mykovol.takeandcharge.service.RentService;

import static com.codename1.ui.CN.callSerially;

public class StationInfoSheet extends Sheet {

    private static final String BOTTOM_PANEL_START_Y = "bottomPanelStartY";
    private final Label availablePowerBanksNumber = new Label("0", "StationsSheetNumberAvailable");
    private final Label cabBeReturnedPowerBanksNumber = new Label("0", "StationsSheetNumberCanBeReturned");
    private final SpanLabel addressLabel = new SpanLabel("", "StationsSheetText");
    private final SpanLabel title = new SpanLabel("", "StationsSheetTitle");
    private final SpanLabel errorLabel = new SpanLabel("something went wrong", "SheetErrorText");
    private final Label placeLogoImageLabel;
    private final Container availableContainer;
    private final Button screenBlocker;
    private final SpanLabel workingHoursLabel = new SpanLabel("8:30 - 21:00", "StationsSheetText");
    private String directionUrl;
    private volatile boolean isShown = false;

    StationInfoSheet(Button screenBlocker, Form attachedForm) {
        super(null, "");
        this.screenBlocker = screenBlocker;
//        setPosition(BorderLayout.SOUTH);
        Container cnt = getContentPane();
        cnt.setLayout(BoxLayout.y());
        errorLabel.setEnabled(false);
        errorLabel.setHidden(true);
//        errorLabel.stripMarginAndPadding();
//        setLeadComponent(cnt);
//        cnt.setLayout(BoxLayout.y());


        int size = Display.getInstance().convertToPixels(1f);
        Image placeImage = Effects.dropshadow(Resources.getGlobalResources().getImage("no-logo.png"), 10, 120, size, size);
        placeLogoImageLabel = new Label(placeImage);
        placeLogoImageLabel.setUIID("StationsSheetImage");

        addressLabel.setEnabled(false);
        addressLabel.setIconUIID("StationsSheetTextIcon");
        FontImage.setMaterialIcon(addressLabel, FontImage.MATERIAL_PLACE);
//        addressLabel.getIcon().s(convertToPixels(2));
//        addressLabel.getAllStyles().setFgColor(0xFF000000);
        workingHoursLabel.setEnabled(false);
//        workingHoursLabel.setGap(convertToPixels(1.3f));
        workingHoursLabel.setIconUIID("StationsSheetTextIcon");
        FontImage.setMaterialIcon(workingHoursLabel, FontImage.MATERIAL_ACCESS_TIME);

        Label availablePowerBanks = new Label("Available:", "StationsSheetAvailableText");
        FontImage.setIcon(availablePowerBanks, FontImage.MATERIAL_ARROW_UPWARD, 3);
        Label cabBeReturnedPowerBanks = new Label("Can be returned:", "StationsSheetAvailableText");
        FontImage.setIcon(cabBeReturnedPowerBanks, FontImage.MATERIAL_ARROW_DOWNWARD, 3);
        Container availableText = BoxLayout.encloseY(availablePowerBanks, cabBeReturnedPowerBanks);
        Container availableNumbers = BoxLayout.encloseY(availablePowerBanksNumber, cabBeReturnedPowerBanksNumber);
        availableContainer = BoxLayout.encloseX(availableText, availableNumbers);
        Container infoContainer = BoxLayout.encloseY(addressLabel, workingHoursLabel);

        Button showDirectionButton = new Button("");
        showDirectionButton.setUIID("GetDirectionButton");

        FontImage.setMaterialIcon(showDirectionButton, FontImage.MATERIAL_DIRECTIONS);
        showDirectionButton.getAllStyles().setBorder(
                RoundBorder.create().color(0x0479f5).shadowOpacity(60)
        );

//        title.setSafeArea(true);
        title.setEnabled(false);

//        cnt.addAll(
//                BoxLayout.encloseX(placeLogoImageLabel, infoContainer),
//                FlowLayout.encloseLeftMiddle(errorLabel),
//                FlowLayout.encloseRightBottom(showDirectionButton)
//        );

        final Container availableAndDirection;
        if (!Display.getInstance().isTablet() && Display.getInstance().getDeviceDensity() < Display.DENSITY_HD) {
            availableAndDirection = BoxLayout.encloseY(availableContainer, BorderLayout.east(showDirectionButton));
        } else {
            availableAndDirection = BorderLayout.centerEastWest(availableContainer, showDirectionButton, null);
        }


        add(BorderLayout.NORTH, BoxLayout.encloseY(errorLabel, title));
        add(BorderLayout.CENTER, BoxLayout.encloseY(
                BoxLayout.encloseX(placeLogoImageLabel, infoContainer),
//                FlowLayout.encloseLeftMiddle(errorLabel),
                availableAndDirection
                )
        );
//        add(BorderLayout.NORTH, title);
//        cnt.addPointerPressedListener(this::getDirectionButtonAction);
//        infoContainer.addPointerPressedListener(this::getDirectionButtonAction);
//        scaleImageLabel.addPointerPressedListener(this::getDirectionButtonAction);
        showDirectionButton.addActionListener(this::getDirectionButtonAction);

        addCloseListener(evt -> {
            screenBlocker.setVisible(false);
        });

        int startX;
        int startY;
        attachedForm.addPointerPressedListener(evt -> {
            Preferences.set(BOTTOM_PANEL_START_Y, evt.getY());
        });

        attachedForm.addPointerReleasedListener(evt -> {
            int draggedLength = evt.getY() - Preferences.get(BOTTOM_PANEL_START_Y, evt.getY());
            Preferences.set(BOTTOM_PANEL_START_Y, 0);
            if (draggedLength > 100) {
                Component draggedCmp = attachedForm.getComponentAt(evt.getX(), evt.getY());
                if (draggedCmp != null && draggedCmp.isChildOf(this) && isShown) {
                    isShown = false;
                    back();
                }
            }
        });

    }

    private void getDirectionButtonAction(ActionEvent evt) {
        if (directionUrl != null) Display.getInstance().execute(directionUrl);
    }

    public void show(StationInfo stationInfo) {
        isShown = true;
        directionUrl = stationInfo.mapUrl.get();
        title.setText(stationInfo.placeName.get());
        addressLabel.setText(stationInfo.address.get());
        addressLabel.setText(stationInfo.address.get());
        workingHoursLabel.setText(stationInfo.workingHours.get());
        errorLabel.setHidden(true);
        super.show();
        RentService.getRemainingPowerBanks(stationInfo.id.get(), new Callback<Integer>() {
            @Override
            public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                callSerially(() -> {
                    if (errorCode == 401) {
                        errorLabel.setText("You must login first");
                    } else {
                        errorLabel.setText(errorMessage);
                    }
                    availablePowerBanksNumber.setText("0");
                    cabBeReturnedPowerBanksNumber.setText("0");
                    errorLabel.setHidden(false);
                    errorLabel.getParent().revalidateWithAnimationSafety();
                });
            }

            @Override
            public void onSucess(Integer remainingPowerBanks) {
                String remainingPowerBanksString = String.valueOf(remainingPowerBanks);
                String canBeReturnedString = String.valueOf(stationInfo.maxCapacity.getInt() - remainingPowerBanks);
                if (!availablePowerBanksNumber.getText().equals(remainingPowerBanksString) ||
                        !cabBeReturnedPowerBanksNumber.getText().equals(canBeReturnedString)) {
                    callSerially(() -> {
                        availablePowerBanksNumber.setText(remainingPowerBanksString);
                        cabBeReturnedPowerBanksNumber.setText(canBeReturnedString);
                        availableContainer.animateLayoutFadeAndWait(200, 0);
                    });

                }
            }
        });
    }
}
