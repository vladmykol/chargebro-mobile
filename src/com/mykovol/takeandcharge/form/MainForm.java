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
import com.codename1.io.Preferences;
import com.codename1.io.Util;
import com.codename1.location.Location;
import com.codename1.location.LocationListener;
import com.codename1.location.LocationManager;
import com.codename1.maps.Coord;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.Resources;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.form.component.ShowMyLocationButton;
import com.mykovol.takeandcharge.service.RentService;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.tools.CommonCode;
import com.mykovol.takeandcharge.tools.DraggablePanel;
import com.mykovol.takeandcharge.tools.MainGifLoader;

/**
 * The main form of the application containing the map code
 *
 * @author Shai Almog
 */
public class MainForm extends Form {
    private static final String MAP_JS_KEY = Util.xorDecode("QEt5ZVZ/RVpEYUpceVw+VSlDWzkjXWZ8bCJDSF5GRkZ4cm1DFVdE");
    private static final Coord ukraineCoord = new Coord(50.480471, 30.412376);
    private static MainForm instance;
    private final MapContainer mapContainer = new MapContainer(MAP_JS_KEY);
    //    private final InfiniteProgress infiniteProgress = new InfiniteProgress();
    private final ScanButton scanButton = new ScanButton("TakePowerBankButton");
    private final Button draggablePanelScreenBlocker = new Button();
    private final Button sideMenuScreenBlocker = new Button();
    private final DraggablePanel draggablePanel;

    private MainForm() {
        super(new LayeredLayout());

        setToolbar(new Toolbar(true));
        getToolbar().getMenuBar().setTactileTouch(true);
        getToolbar().setTactileTouch(true);

        draggablePanel = new DraggablePanel(draggablePanelScreenBlocker, this, getToolbar());
        setName("MapForm");
        setScrollableY(false);
        setTransitionOutAnimator(CommonTransitions.createEmpty());

        mapContainer.setShowMyLocation(false);
        add(mapContainer);
        initMap();

        ScaleImageLabel gradient = new ScaleImageLabel(Resources.getGlobalResources().getImage("gradient-overlay.png"));
        gradient.setBackgroundType(Style.BACKGROUND_IMAGE_SCALED_FILL);
        add(BorderLayout.south(gradient));
        add(BorderLayout.south(scanButton));


        add(BorderLayout.north(FlowLayout.encloseRightBottom(new ShowMyLocationButton(mapContainer))));

        draggablePanelScreenBlocker.setVisible(false);
        add(draggablePanelScreenBlocker);
        add(draggablePanel);
        setScrollableY(false);

        add(BorderLayout.centerAbsolute(MainGifLoader.get()));

        sideMenuScreenBlocker.setVisible(false);
        add(sideMenuScreenBlocker);
        CommonCode.constructSideMenu(getToolbar(), sideMenuScreenBlocker);

//        add(BorderLayout.centerAbsolute(MainGifLoader.get()));
    }

    public static MainForm get() {
        if (instance == null) {
            instance = new MainForm();
        }
        return instance;
    }

    @Override
    public void show() {
        super.show();
        mapContainer.setShowMyLocation(Preferences.get("showMyLocation", false));
        refreshRentContent();
    }

    public void suspend() {
        mapContainer.setShowMyLocation(false);
//        Preferences.set("preferredMapZoom", (int) mapContainer.getZoom());
    }

    private void initMap() {
        Coord lastCoord = ukraineCoord;
        LocationManager lm = LocationManager.getLocationManager();
        if (!Display.getInstance().isSimulator() && lm.isGPSEnabled()) {
            Location lastKnownLocation = lm.getLastKnownLocation();
            lastCoord = new Coord(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        }
        mapContainer.setCameraPosition(lastCoord);
        mapContainer.zoom(lastCoord, mapContainer.getMinZoom() + 13);

        if (!Display.getInstance().isSimulator()) {
            LocationManager.getLocationManager().setLocationListener(new LocationListener() {
                @Override
                public void locationUpdated(Location location) {
                    mapContainer.setCameraPosition(new Coord(location.getLatitude(), location.getLongitude()));
//                        mapContainer.zoom(new Coord(location.getLatitude(), location.getLongitude()), getDefaultZoom(mapContainer));
                    LocationManager.getLocationManager().setLocationListener(null);
                }

                @Override
                public void providerStateChanged(int newState) {
                }
            });
        }

        initStationsOnMap();
    }

    public void showErrorDraggablePanel(String error) {
        draggablePanel.showError(error);
    }

    public void refreshRentContent() {
        draggablePanel.refreshRentContent();
    }

    public void removeRentRow(String serialNumber) {
        draggablePanel.removeRentRow(serialNumber);
    }

    public void removeAllRentRows() {
        draggablePanel.removeAllRentRows();
    }


    private void initStationsOnMap() {
        Image placeImage = Resources.getGlobalResources().getImage("map-point.png");

        mapContainer.addMarker(
                EncodedImage.createFromImage(placeImage, false),
                ukraineCoord, null,
                "Station position on the map",
                evt -> {
                    StationInfoSheet sheet = new StationInfoSheet();
                    sheet.show();
                }
        );
    }

    public void refreshContext() {
        scanButton.refreshContext();
    }


    public class ScanButton extends Button {

        public ScanButton(String uiid) {
            super("", uiid);
            setTactileTouch(true);
            refreshContext();
            addActionListener(this::scanButtonAction);
            getAllStyles().setMarginUnit(Style.UNIT_TYPE_SCREEN_PERCENTAGE);
            getAllStyles().setMarginBottom(DraggablePanel.MIN_PANEL_HEIGHT_SCREEN_PERCENTAGE);
        }

        private void scanButtonAction(ActionEvent evt) {
            if (MainGifLoader.get().isVisible()) return;

            if (UserService.isLoggedIn()) {
                RentService.rent(new Callback<String>() {
                    @Override
                    public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                        draggablePanel.showError(errorMessage);
//                    Dialog.show("Error", errorCode + " " + errorMessage, "Ok", null);
                    }

                    @Override
                    public void onSucess(String powerBankId) {
                        draggablePanel.addRentRow(powerBankId, 0);
                    }
                });
            } else {
                new LoginForm().show();
            }
        }

        public void refreshContext() {
            if (UserService.isLoggedIn()) {
                setText(" Take&Charge");
                FontImage.setMaterialIcon(this, FontImage.MATERIAL_CROP_FREE);
            } else {
                setText("Login");
                FontImage.setMaterialIcon(this, FontImage.MATERIAL_PERSON);
            }
        }
    }


}

