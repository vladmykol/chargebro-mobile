package com.mykovol.takeandcharge.form.component;


import com.codename1.googlemaps.MapContainer;
import com.codename1.io.Preferences;
import com.codename1.location.Location;
import com.codename1.location.LocationListener;
import com.codename1.location.LocationManager;
import com.codename1.maps.Coord;
import com.codename1.ui.Button;
import com.codename1.ui.Dialog;
import com.codename1.ui.FontImage;

public class ShowMyLocationButton extends Button {

    public ShowMyLocationButton(MapContainer mapContainer) {
        setUIID("ShowMeButton");
        LocationManager lm = LocationManager.getLocationManager();
        if (Preferences.get("showMyLocation", false)) {
            FontImage.setMaterialIcon(this, FontImage.MATERIAL_LOCATION_ON);
        } else {
            FontImage.setMaterialIcon(this, FontImage.MATERIAL_LOCATION_OFF);
        }
        addActionListener(evt -> {
            if (mapContainer.isShowMyLocation()) {
                mapContainer.setShowMyLocation(false);
                FontImage.setMaterialIcon(this, FontImage.MATERIAL_LOCATION_OFF);
            } else {
                if (lm.isGPSDetectionSupported()) {
                    if (!lm.isGPSEnabled()) {
                        Dialog.show("", "We need  access to your current location to show nearest PoweBank stations, please enable GPS in Settings.", "Ok", null);
                        return;
                    }
                }
                mapContainer.setShowMyLocation(true);
                FontImage.setMaterialIcon(this, FontImage.MATERIAL_LOCATION_ON);

                LocationManager.getLocationManager().setLocationListener(new LocationListener() {
                    @Override
                    public void locationUpdated(Location location) {
                        if (location.getAccuracy() < 100) {
                            mapContainer.setCameraPosition(new Coord(location.getLatitude(), location.getLongitude()));
                            LocationManager.getLocationManager().setLocationListener(null);
                        }
                    }

                    @Override
                    public void providerStateChanged(int newState) {
                    }
                });
            }
            Preferences.set("showMyLocation", mapContainer.isShowMyLocation());
        });
    }
}