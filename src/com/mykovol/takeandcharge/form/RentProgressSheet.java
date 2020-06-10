package com.mykovol.takeandcharge.form;

import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Label;
import com.codename1.ui.Sheet;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;

public class RentProgressSheet extends Sheet {
    public RentProgressSheet(String title) {
        super(null, title);
        Container cnt = getContentPane();
        cnt.setLayout(BoxLayout.y());
        setPosition(BorderLayout.NORTH);
//        Button gotoSheet2 = new Button("Goto Sheet 2");
//        gotoSheet2.addActionListener(e -> {
//            new MySheet2(this).show(300);
//        });
//        cnt.add(gotoSheet2);
        cnt.add(new Label("Please wait a bit...",
                FontImage.createMaterial(FontImage.MATERIAL_AUTORENEW, "AvailablePowerBanks", 6)));
    }
}
