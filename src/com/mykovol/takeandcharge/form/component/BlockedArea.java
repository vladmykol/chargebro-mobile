package com.mykovol.takeandcharge.form.component;


import com.codename1.ui.Container;
import com.codename1.ui.Label;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.mykovol.takeandcharge.tools.BottomPanel;

import static com.codename1.ui.CN.getDisplayHeight;
import static com.codename1.ui.CN.getDisplayWidth;

public class BlockedArea {

    private final Label zone = new Label("");

    public BlockedArea() {
        zone.setShowEvenIfBlank(true);
        zone.setBlockLead(true);
        zone.getAllStyles().setBgColor(121212);
        zone.getAllStyles().setFgColor(333333);
        defaultSize();
    }

    public Container getComponent() {
        return BorderLayout.south(zone);
    }

    public void fullscreen(){
        zone.setPreferredSize(new Dimension(getDisplayWidth(), getDisplayHeight()));
    }

    public void defaultSize(){
        zone.setPreferredSize(new Dimension(getDisplayWidth(), BottomPanel.minPanelHeight + (int) Math.round(getDisplayHeight() * 0.4)));
    }


}