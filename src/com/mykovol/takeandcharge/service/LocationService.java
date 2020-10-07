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

package com.mykovol.takeandcharge.service;

import com.codename1.googlemaps.MapContainer;
import com.codename1.location.Location;
import com.codename1.location.LocationListener;
import com.codename1.location.LocationManager;
import com.codename1.maps.Coord;
import com.codename1.ui.Dialog;

/**
 * A generic service class that handles login/creation etc.
 *
 * @author Vlad Mykol
 */
public class LocationService {
    private final LocationManager lm = LocationManager.getLocationManager();

    public boolean checkGpsEnabled() {
        if (lm.isGPSDetectionSupported()) {
            if (!lm.isGPSEnabled()) {
                Dialog.show("", "Please enable GPS in your settings", "OK", null);
                return false;
            }
        }
        return true;
    }

    public void moveToCurrentLocation(MapContainer mapContainer) {
        LocationManager.getLocationManager().setLocationListener(new LocationListener() {
            @Override
            public void locationUpdated(Location location) {
                Coord crd = new Coord(location.getLatitude(), location.getLongitude());
                mapContainer.setCameraPosition(crd);
//                mapContainer.zoom(crd, mapContainer.getMinZoom() + 13);
                LocationManager.getLocationManager().setLocationListener(null);
            }

            @Override
            public void providerStateChanged(int newState) {
            }
        });
    }
}
