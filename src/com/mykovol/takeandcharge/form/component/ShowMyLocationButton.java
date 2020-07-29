package com.mykovol.takeandcharge.form.component;


import com.codename1.googlemaps.MapContainer;
import com.codename1.io.Preferences;
import com.codename1.ui.Button;
import com.codename1.ui.Dialog;
import com.codename1.ui.FontImage;
import com.mykovol.takeandcharge.service.LocationService;

public class ShowMyLocationButton extends Button {
    private final MapContainer mapContainer;

    public ShowMyLocationButton(MapContainer mapContainer) {
        this.mapContainer = mapContainer;
        setUIID("ShowMeButton");
        refreshState();

        addActionListener(evt -> {
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
        FontImage.setMaterialIcon(this, FontImage.MATERIAL_LOCATION_ON);
    }

    private void disableShowMyLocation(MapContainer mapContainer) {
        mapContainer.setShowMyLocation(false);
        FontImage.setMaterialIcon(this, FontImage.MATERIAL_LOCATION_OFF);
    }
}