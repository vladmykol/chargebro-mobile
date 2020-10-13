package com.mykovol.takeandcharge.form.component;


import com.codename1.googlemaps.MapContainer;
import com.codename1.io.NetworkManager;
import com.codename1.ui.Button;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.layouts.BoxLayout;
import com.mykovol.takeandcharge.form.ComingSoonForm;
import com.mykovol.takeandcharge.form.MainForm;
import com.mykovol.takeandcharge.service.WebSocketClient;

public class ToolBox extends Container {
    private final MapContainer mapContainer;
    private final Button showNearestStationsButton = new Button("", "ToolBoxButton");
    private final Button refreshButton = new Button("", "ToolBoxButton");
    private final Button reportErrorButton = new Button("", "ToolBoxButton");

    public ToolBox(MapContainer mapContainer) {
        super(BoxLayout.yCenter());
        this.mapContainer = mapContainer;
        setUIID("ToolBox");

        refreshButton.setMaterialIcon(FontImage.MATERIAL_LOOP);
        refreshButton.addActionListener(evt -> {
            Display.getInstance().vibrate(1);
            NetworkManager.getInstance().shutdownSync();
            NetworkManager.getInstance().start();
            WebSocketClient.disconnect();
            WebSocketClient.ensureConnection();
        });
        reportErrorButton.setMaterialIcon(FontImage.MATERIAL_SUPPORT_AGENT);
        reportErrorButton.addActionListener(evt -> {
            new ComingSoonForm("Support", MainForm.get()).show();
        });
        showNearestStationsButton.addActionListener(evt -> {
            new ComingSoonForm("Nearest stations", MainForm.get()).show();
        });

        FontImage.setMaterialIcon(showNearestStationsButton, FontImage.MATERIAL_STOREFRONT);

        addAll(reportErrorButton, refreshButton, showNearestStationsButton);
    }

}