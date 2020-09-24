package com.mykovol.takeandcharge.form.component;


import com.codename1.googlemaps.MapContainer;
import com.codename1.io.Preferences;
import com.codename1.ui.*;
import com.codename1.ui.layouts.BoxLayout;
import com.mykovol.takeandcharge.form.MainForm;
import com.mykovol.takeandcharge.service.LocationService;
import com.mykovol.takeandcharge.tools.CommonCode;

public class ToolBox extends Container {
    private final MapContainer mapContainer;
    private final Button showMyLocationButton = new Button("", "ToolBoxButton");
    private final Button refreshButton = new Button("", "ToolBoxButton");
    private final Button reportErrorButton = new Button("", "ToolBoxButton");

    public ToolBox(MapContainer mapContainer) {
        super(BoxLayout.y());
        this.mapContainer = mapContainer;
        setUIID("ToolBox");

        refreshState();

        refreshButton.setMaterialIcon(FontImage.MATERIAL_LOOP);
        refreshButton.addActionListener(evt -> {
            MainForm.get().refreshRentContent();
            MainForm.get().refreshMarkersOnMap();
            Display.getInstance().vibrate(1);
        });
        reportErrorButton.setMaterialIcon(FontImage.MATERIAL_ERROR_OUTLINE);
        reportErrorButton.addActionListener(evt -> {
            CommonCode.sendSupportEmail();
        });

        showMyLocationButton.addActionListener(evt -> {
            boolean isUserNotifiedAboutLocationUse = Preferences.get("isUserNotifiedAboutLocationUse", false);
            boolean isUserAgreeToGiveLocationAccess = true;
            if (!isUserNotifiedAboutLocationUse) {
                isUserAgreeToGiveLocationAccess = Dialog.show("Permission required", "Please allow using of geolocation to show nearest stations", "OK", "Cancel");
            }
            if (isUserAgreeToGiveLocationAccess) {
                LocationService locationService = new LocationService();
                Preferences.set("isUserNotifiedAboutLocationUse", true);
                if (locationService.checkGpsEnabled()) {
                    locationService.moveToCurrentLocation(mapContainer);
                    enableShowMyLocation(mapContainer);
                } else {
                    disableShowMyLocation(mapContainer);
                }
            } else {
                disableShowMyLocation(mapContainer);
            }

            Preferences.set("showMyLocation", mapContainer.isShowMyLocation());
        });

        addAll(reportErrorButton, refreshButton, showMyLocationButton);
    }

    public void refreshState() {
        boolean isShowMyLocation = Preferences.get("showMyLocation", false);

        if (isShowMyLocation) {
            enableShowMyLocation(mapContainer);
        } else {
            disableShowMyLocation(mapContainer);
        }
    }

    private void enableShowMyLocation(MapContainer mapContainer) {
        mapContainer.setShowMyLocation(true);
        FontImage.setMaterialIcon(showMyLocationButton, FontImage.MATERIAL_LOCATION_ON);
    }

    private void disableShowMyLocation(MapContainer mapContainer) {
        mapContainer.setShowMyLocation(false);
        FontImage.setMaterialIcon(showMyLocationButton, FontImage.MATERIAL_LOCATION_OFF);
    }
}