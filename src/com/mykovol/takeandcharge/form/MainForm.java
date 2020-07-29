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
import com.codename1.io.Log;
import com.codename1.io.Util;
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
import com.mykovol.takeandcharge.dataobj.StationInfo;
import com.mykovol.takeandcharge.form.component.ShowMyLocationButton;
import com.mykovol.takeandcharge.service.RentService;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.tools.CommonCode;
import com.mykovol.takeandcharge.tools.DraggablePanel;
import com.mykovol.takeandcharge.tools.MainGifLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final ShowMyLocationButton showMyLocationButton = new ShowMyLocationButton(mapContainer);
    //    private final InfiniteProgress infiniteProgress = new InfiniteProgress();
    private final ScanButton scanButton = new ScanButton("TakePowerBankButton");
    private final Button draggablePanelScreenBlocker = new Button();
    private final Button sideMenuScreenBlocker = new Button();
    private final DraggablePanel draggablePanel;
    private final StationInfoSheet stationInfoSheet = new StationInfoSheet();
    private final Image stationPointImage = Resources.getGlobalResources().getImage("map-point2.png");
    private final Map<String, MapContainer.MapObject> mapMarkers = new HashMap<>();
    private Coord previousCoord = new Coord(ukraineCoord.getLatitude(), ukraineCoord.getLongitude());

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


        add(BorderLayout.north(FlowLayout.encloseRightBottom(showMyLocationButton)));

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
        showMyLocationButton.refreshState();

        refreshRentContent();
    }

    public void suspend() {
        mapContainer.setShowMyLocation(false);
//        Preferences.set("preferredMapZoom", (int) mapContainer.getZoom());
    }

    private void initMap() {
        Coord defaultLocation = ukraineCoord;

        mapContainer.setCameraPosition(defaultLocation);
        mapContainer.zoom(defaultLocation, mapContainer.getMinZoom() + 10);

        addMapListenerToDrawStationsOnMap();
        refreshMarkersOnMap(defaultLocation);
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


    private void addMapListenerToDrawStationsOnMap() {
        mapContainer.addMapListener((source, zoom, center) -> {
            double x = previousCoord.getLatitude() - center.getLatitude();
            double y = previousCoord.getLongitude() - center.getLongitude();
            double size = Math.abs(x * y);
            if (size > 0.1) {
                previousCoord = center;

                refreshMarkersOnMap(center);
            }
        });
    }

    private void refreshMarkersOnMap(Coord position) {
        Log.p("Station update");
        RentService.getStationsNearBy(position, new Callback<List<StationInfo>>() {
            @Override
            public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                Log.p("cannot get station location update - " + errorCode + errorMessage);
            }

            @Override
            public void onSucess(List<StationInfo> stations) {
                for (StationInfo station : stations) {
                    if (!mapMarkers.containsKey(station.id.get())) {
                        mapMarkers.put(station.id.get(),
                                mapContainer.addMarker(
                                        EncodedImage.createFromImage(stationPointImage, false),
                                        new Coord(station.locationX.get(), station.locationY.get()), "some text here",
                                        "and some long text here",
                                        evt -> {
                                            stationInfoSheet.show(station);
                                        }));
                    }
                }
            }
        });
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
                new RegisterMobileNumberStep1().show();
            }
        }

        public void refreshContext() {
            if (UserService.isLoggedIn()) {
                setText(" Take&Charge");
                FontImage.setMaterialIcon(this, FontImage.MATERIAL_CROP_FREE);
            } else {
                setText("Register");
                FontImage.setMaterialIcon(this, FontImage.MATERIAL_PERSON_ADD);
            }
        }
    }


}

