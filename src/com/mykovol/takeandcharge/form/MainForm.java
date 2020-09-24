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


import com.codename1.googlemaps.MapContainer;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.maps.Coord;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.RoundBorder;
import com.codename1.ui.util.Resources;
import com.codename1.ui.util.UITimer;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.StationInfo;
import com.mykovol.takeandcharge.form.component.DraggablePanel;
import com.mykovol.takeandcharge.form.component.MessagePopUp;
import com.mykovol.takeandcharge.form.component.ToolBox;
import com.mykovol.takeandcharge.service.RentService;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.tools.CommonCode;
import com.mykovol.takeandcharge.tools.MainGifLoader;

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
 * @author Shai Almog
 */
public class MainForm extends Form {
    private static final String MAP_JS_KEY = Util.xorDecode("QEt5ZVZ/RVpEYUpceVw+VSlDWzkjXWZ8bCJDSF5GRkZ4cm1DFVdE");
    private static final Coord ukraineCoord = new Coord(50.480471, 30.412376);
    private static MainForm instance;
    private final MapContainer mapContainer = new MapContainer(MAP_JS_KEY);
    private final ToolBox toolBox = new ToolBox(mapContainer);
    //    private final InfiniteProgress infiniteProgress = new InfiniteProgress();
    private final ScanButton scanButton = new ScanButton("TakePowerBankButton");
    private final Button sheetInfoScreenBlocker = new Button();
    private final DraggablePanel draggablePanel;
    private final StationInfoSheet stationInfoSheet = new StationInfoSheet(sheetInfoScreenBlocker, this);
    private final Image stationPointImage = Resources.getGlobalResources().getImage("map-point.png");
    private final Map<String, MapContainer.MapObject> mapMarkers = new HashMap<>();
    private final MessagePopUp messagePopUp = new MessagePopUp();
    private Coord previousCoord = new Coord(ukraineCoord.getLatitude(), ukraineCoord.getLongitude());

    private MainForm() {
        super(new LayeredLayout());
        setName("MapForm");
        setScrollableY(false);
        setToolbar(new Toolbar(true));
        setTransitionOutAnimator(CommonTransitions.createEmpty());

        Button draggablePanelScreenBlocker = new Button();
        Button draggablePanelScreenBottomBlocker = new Button();
        draggablePanel = new DraggablePanel(draggablePanelScreenBlocker,
                draggablePanelScreenBottomBlocker,
                this);

        mapContainer.setShowMyLocation(false);
        add(mapContainer);

        add(FlowLayout.encloseRightMiddle(toolBox));

        add(BorderLayout.south(
                FlowLayout.encloseCenter(scanButton)
        ));

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

        add(BorderLayout.centerAbsolute(MainGifLoader.get()));

        add(messagePopUp);

        CommonCode.constructSideMenu(getToolbar(), this);
        initMap();
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

    @Override
    public void show() {
        super.show();
        toolBox.refreshState();
        UITimer.timer(3000, false, getComponentForm(), () -> {
            refreshRentContent();
            refreshMarkersOnMap(ukraineCoord);
        });
    }

    private void initMap() {
        addMapListenerToDrawStationsOnMap();

        mapContainer.setCameraPosition(ukraineCoord);
        mapContainer.zoom(ukraineCoord, mapContainer.getMinZoom() + 15);

//        UITimer.timer(3000, false, getComponentForm(), () -> {
//            mapContainer.zoom(ukraineCoord, mapContainer.getMinZoom() + 15);
//            scanButton.setVisible(true);
//            revalidateWithAnimationSafety();
//        });
    }

    public void refreshRentContent() {
        draggablePanel.refreshRentContent();
    }

    public void hideScanButton() {
        scanButton.setVisible(false);
    }

    public void showScanButton() {
        scanButton.setVisible(true);
    }

    public void removeRentRow(String serialNumber) {
        draggablePanel.removeRentRow(serialNumber);
    }

    public void removeAllRentRows() {
        callSerially(draggablePanel::removeAllRentRows);
    }

    public void showError(String text, int type) {
        callSerially(() -> {
            messagePopUp.showError(text, type);
        });
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

    public void refreshMarkersOnMap() {
        refreshMarkersOnMap(ukraineCoord);
    }

    public void refreshMarkersOnMap(Coord position) {
        RentService.getStationsNearBy(position, new Callback<List<StationInfo>>() {
            @Override
            public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                Log.p("cannot get station location update - " + errorCode + errorMessage);
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

    public void addRentRow(String powerBankId) {
        draggablePanel.addRentRow(powerBankId, 0);
    }

    public class ScanButton extends Button {
        private Font fnt = Font.createTrueTypeFont("icomoon", "icomoon.ttf");

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
            addActionListener(this::scanButtonAction);
            updateBorder();
        }

        private void updateBorder() {
            getAllStyles().setBorder(RoundBorder.create().
                    color(getUnselectedStyle().getBgColor())
                    .shadowOpacity(90)
                    .rectangle(true));
        }

        private void scanButtonAction(ActionEvent evt) {
            if (MainGifLoader.get().isVisible()) return;

            if (UserService.isLoggedIn()) {
                RentService.prepareForRent(new Callback<String>() {
                    @Override
                    public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                        showError(errorMessage, errorCode);
//                    Dialog.show("Error", errorCode + " " + errorMessage, "Ok", null);
                    }

                    @Override
                    public void onSucess(String powerBankId) {
                        revalidate();
                    }
                });
            } else {
                new SingUpForm().show();
            }
        }

        public void refresh() {
            if (UserService.isLoggedIn()) {
                setText("Scan QR code");
                setFontIcon(fnt, '\ue900', 4);
            } else {
                setText("Register");
                setMaterialIcon(FontImage.MATERIAL_PERSON_ADD, 4);
            }
        }
    }


}

