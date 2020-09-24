package com.mykovol.takeandcharge.form.component;

import com.codename1.ui.Container;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;

import static com.codename1.ui.util.Resources.getGlobalResources;
import static com.mykovol.takeandcharge.service.StyleConst.*;

public class DraggablePanelContent extends Container {
    private final int minPanelHeight;

    public DraggablePanelContent(int minPanelHeight) {
        super(BoxLayout.y());
        this.minPanelHeight = minPanelHeight;

        setUIID("BottomPanelUnfolded");
//        bottomPanel.setBlockLead(true);
//        setScrollableY(true);
//        setScrollVisible(false);
        setScrollableY(true);
        setScrollVisible(false);
//        setShouldCalcPreferredSize(false);
    }

    @Override
    protected Dimension calcPreferredSize() {
        Dimension preferredSize = getLayout().getPreferredSize(this);
        preferredSize.setHeight(minPanelHeight);
        return preferredSize;
    }
}
