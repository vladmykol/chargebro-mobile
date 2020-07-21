package com.mykovol.takeandcharge.form;

import com.codename1.components.SpanLabel;
import com.codename1.ui.Container;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.Sheet;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.util.Resources;

public class StationInfoSheet extends Sheet {
    StationInfoSheet(String title) {
        super(null, title);
        Container cnt = getContentPane();
        cnt.setLayout(BoxLayout.y());
//        Button gotoSheet2 = new Button("Goto Sheet 2");
//        gotoSheet2.addActionListener(e -> {
//            new MySheet2(this).show(300);
//        });
//        cnt.add(gotoSheet2);
        Image image = Resources.getGlobalResources().getImage("charging-station.png");
        SpanLabel spanLabel = new SpanLabel("Available power banks: 6");
        spanLabel.setIcon(image);
        cnt.add(spanLabel);
    }
}
