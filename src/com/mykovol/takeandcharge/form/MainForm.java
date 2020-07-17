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


import com.codename1.components.ScaleImageLabel;
import com.codename1.googlemaps.MapContainer;
import com.codename1.io.Util;
import com.codename1.location.Location;
import com.codename1.location.LocationListener;
import com.codename1.location.LocationManager;
import com.codename1.maps.Coord;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.Resources;
import com.codename1.ui.util.UITimer;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.service.RentService;
import com.mykovol.takeandcharge.service.RentSocketService;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.tools.BottomPanel;
import com.mykovol.takeandcharge.tools.CommonCode;
import com.mykovol.takeandcharge.tools.MainGifLoader;

/**
 * The main form of the application containing the map code
 *
 * @author Shai Almog
 */
public class MainForm extends Form {
    private static final String MAP_JS_KEY = Util.xorDecode("QEt5ZVZ/RVpEYUpceVw+VSlDWzkjXWZ8bCJDSF5GRkZ4cm1DFVdE");
    private static final Coord station1 = new Coord(50.481952, 30.412420);
    private static MainForm instance;
    private final Button scanButton = new ScanButton(" Take&Charge", "TakePowerBankButton");
    private final Container scabButtonHolder = BorderLayout.south(BoxLayout.encloseY(FlowLayout.encloseCenter(scanButton)));
    private final Container draggablePanelContainer = new Container();
    //    private final InfiniteProgress infiniteProgress = new InfiniteProgress();
//    private final Container scabButtonHolder = BorderLayout.south(BoxLayout.encloseY(FlowLayout.encloseCenter(n)));
    private final MapContainer mapContainer = new MapContainer(MAP_JS_KEY);
    private final Button screenBlocking = new Button();
    private BottomPanel bottomPanel;

    private MainForm() {
        super(new LayeredLayout());
        setName("MapForm");

//        FontImage mat = FontImage.createMaterial(FontImage.MATERIAL_MY_LOCATION, "", 4.5f);
//        getToolbar().addCommandToRightBar("", mat, e -> {});

        setScrollableY(false);
        setTransitionOutAnimator(CommonTransitions.createEmpty());
        showMeOnTheMap();
//        add(mapContainer);
        add(BorderLayout.center(mapContainer));
//        mapContainer.set


        ScaleImageLabel gradient = new ScaleImageLabel(Resources.getGlobalResources().getImage("gradient-overlay.png"));
        gradient.setBackgroundType(Style.BACKGROUND_IMAGE_SCALED_FILL);
        add(BorderLayout.south(gradient));
        add(scabButtonHolder);
        add(draggablePanelContainer);

        add(BorderLayout.centerAbsolute(MainGifLoader.get()));

        showStationsOnMap();

        screenBlocking.setVisible(false);
        add(screenBlocking);
    }

    public static MainForm get() {
        if (instance == null) {
            instance = new MainForm();
        }
        return instance;
    }

    public static void appInit() {
        if (instance != null) {
            instance.showMeOnTheMap();
        }
        if (UserService.isLoggedIn()) RentSocketService.get().reconnect();
    }

    private void showMeOnTheMap() {
        mapContainer.zoom(station1, mapContainer.getMinZoom() + 6);
        new UITimer(() -> {
            LocationManager lm = LocationManager.getLocationManager();
            if (lm.isGPSDetectionSupported()) {
                if (!lm.isGPSEnabled()) {
                    Dialog.show("", "We need  access to your current location to show nearest PoweBank stations, please enable GPS in Settings.", "Ok", null);
                }
            }
            mapContainer.setShowMyLocation(true);
            System.out.println("timer");
        }).schedule(7000, false, this);

        if (!Display.getInstance().isSimulator()) {
            LocationManager.getLocationManager().setLocationListener(new LocationListener() {
                @Override
                public void locationUpdated(Location location) {
                    if (location.getAccuracy() < 1000) {
                        mapContainer.zoom(new Coord(location.getLatitude(), location.getLongitude()), mapContainer.getMinZoom() + 6);
                        LocationManager.getLocationManager().setLocationListener(null);
                    }
                }

                @Override
                public void providerStateChanged(int newState) {
                }
            });
        }

    }

    @Override
    public void show() {
        super.show();
        if (bottomPanel == null) {
            Container draggablePanelContainer = new Container();
            add(draggablePanelContainer);
            bottomPanel = new BottomPanel(draggablePanelContainer, screenBlocking);
            bottomPanel.show();
        } else {
            bottomPanel.refreshRentContent();
        }
    }

    public BottomPanel getBottomPanel() {
        return bottomPanel;
    }

    private void showStationsOnMap() {
        Image placeImage = Resources.getGlobalResources().getImage("map-point.png");
        mapContainer.setCameraPosition(station1);
        mapContainer.addMarker(
                EncodedImage.createFromImage(placeImage, false),
                mapContainer.getCameraPosition(), null,
                "Station position on the map",
                evt -> {
                    StationInfoSheet sheet = new StationInfoSheet("Station 441");
                    sheet.show();
                }
        );
    }

    public void appClose() {
        mapContainer.setShowMyLocation(false);
        RentSocketService.get().close();
    }

    @Override
    protected void initGlobalToolbar() {
        setToolbar(new Toolbar(true));
        CommonCode.constructSideMenu(getToolbar());
    }

    public class ScanButton extends Button {
        public ScanButton(String text, String uiid) {
            super(text, uiid);
            FontImage.setMaterialIcon(this, FontImage.MATERIAL_CROP_FREE);
            addActionListener(this::scanButtonAction);
            getAllStyles().setMarginUnit(Style.UNIT_TYPE_SCREEN_PERCENTAGE);
            getAllStyles().setMarginBottom(BottomPanel.MIN_PANEL_HEIGHT_SCREEN_PERCENTAGE);
        }

        private void scanButtonAction(ActionEvent evt) {
            if (MainGifLoader.get().isVisible()) return;
            RentService.rent(new Callback<String>() {
                @Override
                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                    bottomPanel.showError(errorMessage);
//                    Dialog.show("Error", errorCode + " " + errorMessage, "Ok", null);
                }

                @Override
                public void onSucess(String powerBankId) {
                    bottomPanel.addRentRow(powerBankId, 0);
                }
            });
        }
    }


}

