/*
 * Copyright (c) 2012, Codename One and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Codename One designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Codename One through http://www.codenameone.com/ if you
 * need additional information or have any questions.
 */

package com.mykovol.takeandcharge.form;


import com.codename1.components.ToastBar;
import com.codename1.googlemaps.MapContainer;
import com.codename1.io.Preferences;
import com.codename1.io.Util;
import com.codename1.maps.Coord;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.RoundBorder;
import com.codename1.ui.util.Resources;
import com.codename1.ui.util.UITimer;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.BeforeRentInfo;
import com.mykovol.takeandcharge.dataobj.StationInfo;
import com.mykovol.takeandcharge.form.component.CustomDialog;
import com.mykovol.takeandcharge.form.component.DraggablePanel;
import com.mykovol.takeandcharge.form.component.MessagePopUp;
import com.mykovol.takeandcharge.form.component.ToolBox;
import com.mykovol.takeandcharge.service.LocationService;
import com.mykovol.takeandcharge.service.RentService;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.service.WebSocketClient;
import com.mykovol.takeandcharge.tools.MainNoBlockingLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codename1.ui.CN.*;
import static com.codename1.ui.ComponentSelector.$;
import static com.codename1.ui.plaf.Style.BACKGROUND_IMAGE_SCALED;
import static com.codename1.ui.plaf.Style.UNIT_TYPE_SCREEN_PERCENTAGE;

/**
 * The main form of the application containing the map code
 *
 * @author Vlad Mykol
 */
public class MainForm extends Form {
    private static final String MAP_JS_KEY = Util.xorDecode("QEt5ZVZ/RU1uOnlqYGF9ZmF7enFvckdXTHxTW2VrfGoYWmt1FB9A");
    private static MainForm instance;
    private final MapContainer mapContainer = new MapContainer(MAP_JS_KEY);
    private final ToolBox toolBox = new ToolBox(mapContainer);
    private final ScanButton scanButton = new ScanButton("TakePowerBankButton");
    private final Button sheetInfoScreenBlocker = new Button();
    private final Button draggablePanelScreenBottomBlocker = new Button();
    private final Button draggablePanelScreenBlocker = new Button();
    private final DraggablePanel draggablePanel = new DraggablePanel(draggablePanelScreenBlocker,
            draggablePanelScreenBottomBlocker,
            this);
    private final StationInfoSheet stationInfoSheet = new StationInfoSheet(sheetInfoScreenBlocker, this);
    private final Image stationPointImage = Resources.getGlobalResources().getImage("map-point.png");
    private final Map<String, MapContainer.MapObject> mapMarkers = new HashMap<>();
    private final MessagePopUp messagePopUp = new MessagePopUp();
    private Coord previousCoord;

    private MainForm() {
        super(new LayeredLayout());
        setName("MapForm");
        setScrollableY(false);
        setToolbar(new Toolbar(true));
        setTransitionOutAnimator(CommonTransitions.createEmpty());
        setTransitionInAnimator(CommonTransitions.createEmpty());

        mapContainer.setShowMyLocation(false);
        add(BorderLayout.center(mapContainer));

        scanButton.setVisible(false);
        add(BorderLayout.south(
                BoxLayout.encloseXCenter(scanButton)
        ));

        add(FlowLayout.encloseRightMiddle(toolBox));

        $(draggablePanelScreenBottomBlocker)
                .setUIID("Container")
                .setBackgroundType(BACKGROUND_IMAGE_SCALED)
                .setBgImage(Resources.getGlobalResources().getImage("gradient-overlay.png"))
                .stripMarginAndPadding()
                .setVisible(false)
                .setPreferredSize(new Dimension(getDisplayWidth(), DraggablePanel.minPanelHeight));
        add(BorderLayout.south(draggablePanelScreenBottomBlocker));

        $(draggablePanelScreenBlocker)
                .setUIID("Container")
                .setVisible(false)
                .stripMarginAndPadding();
        add(draggablePanelScreenBlocker);

        $(sheetInfoScreenBlocker)
                .setUIID("Container")
                .setVisible(false)
                .stripMarginAndPadding();
        add(sheetInfoScreenBlocker);

        add(draggablePanel);

        add(MainNoBlockingLoader.get());

//        add(messagePopUp);
        messagePopUp.bindToComponent(this);
//
        initMap();

        addPointerDraggedListener(evt -> {
            Component draggedCmp = getComponentAt(evt.getX(), evt.getY());
            if (draggedCmp.isChildOf(mapContainer)) {
                toolBox.setEnabled(false);
                draggablePanel.disableDrag();
            }
        });

        addPointerReleasedListener(evt -> {
            Component draggedCmp = getComponentAt(evt.getX(), evt.getY());
            if (draggedCmp.isChildOf(mapContainer)) {
                toolBox.setEnabled(true);
                draggablePanel.enableDrag();
            }
        });

//        addShowListener(evt -> {
//            showMeOnMap();
//            refreshRentContent(true);
//        });
    }

    public static MainForm get() {
        if (instance == null) {
            instance = new MainForm();
        }
        return instance;
    }

    public static void suspend() {
        if (instance != null) {
            instance.mapContainer.setShowMyLocation(false);
        }
    }

    public static void showError(String text, int type) {
        if (Display.getInstance().getCurrent().equals(MainForm.get())) {
            instance.messagePopUp.showError(type, text);
        } else {
            ToastBar.showErrorMessage(text);
        }
    }

    @Override
    public void show() {
        super.show();
        showMeOnMap();
        refreshRentContent(true);
    }

    public void showNoUpdate() {
        super.show();
    }

    public void showErrorOnMainScreen(String text, int type) {
        callSerially(() -> {
                    showNoUpdate();
                    messagePopUp.showError(type, text);
                }
        );
    }

    public void initWithStartingArg(String stationId) {
        scanButton.action(stationId);
    }

    private void initMap() {
        if (Display.getInstance().isSimulator()) {
            final Coord coord = new Coord(50.476473, 30.416910);
            mapContainer.setCameraPosition(coord);
            mapContainer.zoom(coord, mapContainer.getMinZoom() + 11);
        } else {
            final double lastPositionX = Preferences.get("lastPositionX", 0d);
            final double lastPositionY = Preferences.get("lastPositionY", 0d);
            if (lastPositionX > 0 && lastPositionY > 0) {
                final Coord coord = new Coord(lastPositionX, lastPositionY);
                mapContainer.setCameraPosition(coord);
                mapContainer.zoom(coord, mapContainer.getMinZoom() + 12);
            }
        }

        addMapListenerToDrawStationsOnMap();
    }

    public void showMeOnMap() {
        boolean isShowMyLocation = Preferences.get("showMyLocation", false);
        if (isShowMyLocation) {
            mapContainer.setShowMyLocation(true);
            LocationService locationService = new LocationService();
            locationService.moveToCurrentLocation(mapContainer);
        } else {
            UITimer.timer(5000, false, getComponentForm(), () -> {
                final String userLocationProp = "isUserNotifiedAboutLocationUse";
                boolean isUserNotifiedAboutLocationUse = Preferences.get(userLocationProp, false);
                if (!isUserNotifiedAboutLocationUse) {
                    new CustomDialog("Permission required", "Please allow using of geolocation to show nearest stations").showOk();
                    Preferences.set(userLocationProp, true);
                }

                LocationService locationService = new LocationService();
                if (locationService.checkGpsEnabled()) {
                    locationService.moveToCurrentLocation(mapContainer);
                }
                mapContainer.setShowMyLocation(true);

                Preferences.set("showMyLocation", mapContainer.isShowMyLocation());
            });
        }
    }

    public void refreshRentContent(boolean isShowImmediately) {
        if (UserService.isLoggedIn()) {
            draggablePanel.refreshRentContent(isShowImmediately);
        } else {
            showScanButton();
        }
    }

    public void addRentRowOffline(String powerBankId) {
        draggablePanel.addRentRowOffline(powerBankId);
    }

    public void hideScanButton() {
        scanButton.setVisible(false);
    }

    public void showScanButton() {
        if (!scanButton.isVisible()) {
            callSerially(() -> {
                scanButton.setY(getDisplayHeight());
                scanButton.setVisible(true);
                scanButton.getParent().animateLayout(500);
            });
        }
    }

    public boolean isRentInProgress() {
        return !scanButton.isVisible();
    }

    public void removeRentRow(String serialNumber) {
        if (draggablePanel.removeRentRow(serialNumber)) {

        }
    }

    public void showRentIsOver() {
        if (!UserService.isLoggedIn()) return;
        final CustomDialog customDialog = new CustomDialog("Rent is over",
                "Would you like to rate your ChargeBro experience in Telegram Bot?");
        customDialog.addRatingStarts();
        customDialog.addYesCancelButtons("Yes", evt -> {
            Display.getInstance().execute("https://t.me/chargebro_bot?start=survey");
        });
        customDialog.showWithAnimationSafety();
    }

    public void removeAllRentRows() {
        callSerially(draggablePanel::removeAllRentRows);
        showScanButton();
    }

    private void addMapListenerToDrawStationsOnMap() {
        mapContainer.addMapListener((source, zoom, center) -> {
            if (previousCoord != null) {
                double x = previousCoord.getLatitude() - center.getLatitude();
                double y = previousCoord.getLongitude() - center.getLongitude();
                double size = Math.abs(x * y);
                if (size > 0.1) {
                    refreshMarkersOnPoint(center);
                }
            } else {
                refreshMarkersOnPoint(center);
            }
        });
    }

    private void refreshMarkersOnPoint(Coord center) {
        previousCoord = center;
        Preferences.set("lastPositionX", center.getLatitude());
        Preferences.set("lastPositionY", center.getLongitude());
        refreshMarkersOnMap(center);
    }

    public void refreshMarkersOnMap(Coord position) {
        RentService.getStationsNearBy(position, new Callback<List<StationInfo>>() {
            @Override
            public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                if (errorCode != 404) {
                    showError(errorMessage, errorCode);
                }
            }

            @Override
            public void onSucess(List<StationInfo> stations) {
                for (StationInfo station : stations) {
                    if (!mapMarkers.containsKey(station.id.get())) {
                        callSerially(() -> {
                            mapMarkers.put(station.id.get(),
                                    mapContainer.addMarker(
                                            EncodedImage.createFromImage(stationPointImage, false),
                                            new Coord(station.locationX.get(), station.locationY.get()), "some text here",
                                            "and some long text here",
                                            evt -> {
//                                                new Sheet(null, station.placeName.get()).show();
                                                stationInfoSheet.show(station);
                                            }));
                        });
                    }
                }
                mapContainer.revalidateWithAnimationSafety();
            }
        });
    }

    public void refreshScanButton() {
        scanButton.refresh();
    }


    public class ScanButton extends Button {
        private final Font fnt = Font.createTrueTypeFont("icomoon", "icomoon.ttf");

        public ScanButton(String uiid) {
            super("", uiid);
            getAllStyles().setMarginUnit(UNIT_TYPE_SCREEN_PERCENTAGE);
            if (Display.getInstance().getDeviceDensity() > Display.DENSITY_VERY_HIGH) {
                getAllStyles().setMarginBottom(7);
            } else {
                getAllStyles().setMarginBottom(5);
            }
            setTactileTouch(true);
            setGap(convertToPixels(1));
            refresh();
            addActionListener(evt -> action());
            updateBorder();
        }

        private void updateBorder() {
            getAllStyles().setBorder(RoundBorder.create().
                    color(getUnselectedStyle().getBgColor())
                    .shadowOpacity(90)
                    .rectangle(true));
        }

        private void action() {
            action(null);
        }

        public void action(String predefinedStationId) {
            if (MainNoBlockingLoader.get().isVisible()) return;

            if (UserService.isLoggedIn()) {
                MainNoBlockingLoader.get().start();
                RentService.prepareForRent(predefinedStationId, new Callback<BeforeRentInfo>() {
                    @Override
                    public void onSucess(BeforeRentInfo value) {
                        RentConfirmation rentConfirmation = new RentConfirmation(value);
                        WebSocketClient.ensureConnection();
                        rentConfirmation.show();
                        MainNoBlockingLoader.get().stop();
                    }

                    @Override
                    public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                        showErrorOnMainScreen(errorMessage, errorCode);
                        MainNoBlockingLoader.get().stop();
                    }
                });
            } else {
                new LoginForm().show();
            }
        }

        public void refresh() {
            if (UserService.isLoggedIn()) {
                setText("Scan QR code");
                setFontIcon(fnt, '\ue901', 4);
            } else {
                if (WalletForm.isUserHasCard()) {
                    setText("Log in");
                    setMaterialIcon(FontImage.MATERIAL_PERSON, 4);
                } else {
                    setText("Register");
                    setMaterialIcon(FontImage.MATERIAL_PERSON_ADD, 4);
                }
            }
        }
    }


}

