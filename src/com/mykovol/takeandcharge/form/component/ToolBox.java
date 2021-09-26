package com.mykovol.takeandcharge.form.component;


import com.codename1.googlemaps.MapContainer;
import com.codename1.io.NetworkManager;
import com.codename1.ui.*;
import com.codename1.ui.layouts.BoxLayout;
import com.mykovol.takeandcharge.form.ComingSoonForm;
import com.mykovol.takeandcharge.form.MainForm;
import com.mykovol.takeandcharge.service.WebSocketClient;
import com.mykovol.takeandcharge.tools.MainNoBlockingLoader;

import static com.codename1.ui.CN.callSerially;

public class ToolBox extends Container {
    private final MapContainer mapContainer;
    private final Button showNearestStationsButton = new Button("", "ToolBoxButton");
    private final Button refreshButton = new Button("", "ToolBoxButton");
    private final Button reportErrorButton = new Button("", "ToolBoxButton");
    private final Font fnt = Font.createTrueTypeFont("icomoon", "icomoon.ttf");

    public ToolBox(MapContainer mapContainer) {
        super(BoxLayout.yCenter());
        this.mapContainer = mapContainer;
        setUIID("ToolBox");

        refreshButton.setMaterialIcon(FontImage.MATERIAL_LOOP);
        refreshButton.addActionListener(evt -> {
            MainNoBlockingLoader.get().startTimeout(250);
            Display.getInstance().vibrate(1);
            NetworkManager.getInstance().shutdownSync();
            NetworkManager.getInstance().start();
            WebSocketClient.disconnect();
            WebSocketClient.ensureConnection();
            MainForm.get().refreshRentContent(true);
            MainForm.get().revalidate();
        });
        reportErrorButton.setMaterialIcon(FontImage.MATERIAL_SUPPORT_AGENT);
        reportErrorButton.addActionListener(evt -> {
                Display.getInstance().execute("https://t.me/ChargeBro_Bot");
        });
        showNearestStationsButton.addActionListener(evt -> {
            new ComingSoonForm("Nearest stations", MainForm.get()).show();
        });

        showNearestStationsButton.setFontIcon(fnt, '\ue900', 4);

        addAll(reportErrorButton, refreshButton, showNearestStationsButton);
    }

}