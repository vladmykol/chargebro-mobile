package com.mykovol.takeandcharge.form;

import com.codename1.components.ScaleImageLabel;
import com.codename1.components.SpanButton;
import com.codename1.components.SpanLabel;
import com.codename1.ui.*;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.util.Effects;
import com.codename1.ui.util.Resources;

public class StationInfoSheet extends Sheet {
    StationInfoSheet() {
        super(null, "Kvitka cafe and bar");
        setPosition(BorderLayout.NORTH);
        Container cnt = getContentPane();
        setLeadComponent(cnt);
        cnt.setLayout(new BoxLayout(BoxLayout.X_AXIS));
//        Button gotoSheet2 = new Button("Goto Sheet 2");
//        gotoSheet2.addActionListener(e -> {
//            new MySheet2(this).show(300);
//        });
//        cnt.add(gotoSheet2);

        int size = Display.getInstance().convertToPixels(1f);
        Image placeImage = Effects.dropshadow(Resources.getGlobalResources().getImage("sova.jpg"), 15, 70, size, size);
        ScaleImageLabel scaleImageLabel = new ScaleImageLabel(placeImage);
        scaleImageLabel.setUIID("StationsSheetImage");
        cnt.add(scaleImageLabel);

        SpanLabel address = new SpanLabel("Tiraspolska 60, Misto Kvitiv", "StationsSheetAddress");
        address.setEnabled(false);
        FontImage.setMaterialIcon(address, FontImage.MATERIAL_PLACE);

        Label availablePowerBanks = new Label("Available powerbanks:", "StationsSheetText");
        Label availablePowerBanksNumber = new Label("4", "StationsSheetNumberAvailable");
        Container availableHolder = FlowLayout.encloseIn(availablePowerBanks, availablePowerBanksNumber);

        Label cabBeReturnedPowerBanks = new Label("Can be returned:", "StationsSheetText");
        Label cabBeReturnedPowerBanksNumber = new Label("2", "StationsSheetNumberCanBeReturned");
        Container canBeReturnedHolder = FlowLayout.encloseIn(cabBeReturnedPowerBanks, cabBeReturnedPowerBanksNumber);

        Container InfoContainer = BoxLayout.encloseY(address, availableHolder, canBeReturnedHolder);
        cnt.add(InfoContainer);
        cnt.addPointerPressedListener(evt -> {
            Display.getInstance().execute("https://www.google.com/maps/search/?api=1&query=50.480471,30.412376&query_place_id=ChIJjaZH6F3N1EAR6TlBdWc1IDI");
        });
        InfoContainer.addPointerPressedListener(evt -> {
            Display.getInstance().execute("https://www.google.com/maps/search/?api=1&query=50.480471,30.412376&query_place_id=ChIJjaZH6F3N1EAR6TlBdWc1IDI");
        });
        scaleImageLabel.addPointerPressedListener(evt -> {
            Display.getInstance().execute("https://www.google.com/maps/search/?api=1&query=50.480471,30.412376&query_place_id=ChIJjaZH6F3N1EAR6TlBdWc1IDI");
        });
    }
}
