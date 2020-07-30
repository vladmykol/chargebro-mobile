package com.mykovol.takeandcharge.form.component;

import com.codename1.ui.Container;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;

import static com.codename1.ui.util.Resources.getGlobalResources;
import static com.mykovol.takeandcharge.service.StyleConst.*;

public class BottomPanel extends Container {
    private final int minPanelHeight;

    public BottomPanel(int minPanelHeight) {
        super(BoxLayout.y());
        this.minPanelHeight = minPanelHeight;

    }

    @Override
    protected Dimension calcPreferredSize() {
        Dimension preferredSize = getLayout().getPreferredSize(this);
        preferredSize.setHeight(minPanelHeight);
        return preferredSize;
    }
}
