package com.mykovol.takeandcharge.form;

import com.codename1.components.ScaleImageLabel;
import com.codename1.components.SpanLabel;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.util.Resources;

import java.util.ArrayList;

import static com.codename1.ui.CN.getDisplayHeight;
import static com.codename1.ui.plaf.Style.UNIT_TYPE_PIXELS;
import static com.codename1.ui.plaf.Style.UNIT_TYPE_SCREEN_PERCENTAGE;

/**
 * A swipe tutorial for the application
 *
 * @author Shai Almog
 */
public class WalkthruForm extends Form {
    private final Button skipButton = new Button("Get Started", "WalkthrSkipButton");

    public WalkthruForm() {
        super(new LayeredLayout());
        setToolbar(new Toolbar(true));
        setScrollableY(false);

        setTransitionOutAnimator(CommonTransitions.createFade(400));

        ArrayList<TabPage> pages = new ArrayList<>();
        pages.add(getFirstTab());
        pages.add(getSecondTab());
        pages.add(getThirdTab());


        Tabs walkthruTabs = new Tabs();
        walkthruTabs.setUIID("Container");
        walkthruTabs.getContentPane().setUIID("Container");
        walkthruTabs.getTabsContainer().setUIID("Container");
        walkthruTabs.hideTabs();

        ButtonGroup bg = new ButtonGroup();
        Image unselectedWalkthru = Resources.getGlobalResources().getImage("unselected-walkthru.png");
        Image selectedWalkthru = Resources.getGlobalResources().getImage("selected-walkthru.png");
        FlowLayout flow = new FlowLayout(CENTER);
        flow.setValign(CENTER);
        Container radioContainer = new Container(flow);

        for (TabPage page : pages) {
            RadioButton toggle = RadioButton.createToggle(unselectedWalkthru, bg);
            toggle.setPressedIcon(selectedWalkthru);
            toggle.setEnabled(false);
            toggle.setUIID("Label");
            page.setRadioButton(toggle);

            radioContainer.add(toggle);
            walkthruTabs.addTab("", page.getTabContainer());
        }

        pages.get(0).getRadioButton().setSelected(true);
        walkthruTabs.addSelectionListener((i, ii) -> {
            if (!pages.get(ii).getRadioButton().isSelected()) {
                pages.get(ii).getRadioButton().setSelected(true);
            }
        });

//        Button skipButtonIcon = new Button("", "WalkthrSkipButton");
//        skipButtonIcon.setMaterialIcon(FontImage.MATERIAL_ARROW_FORWARD);
//        skipButtonIcon.getAllStyles().setMarginLeft(0);
//        skipButtonIcon.getAllStyles().setPaddingLeft(0);
//        skipButton.getAllStyles().setMarginRight(0);

        skipButton.addActionListener(evt -> {
            MainForm.get().show();
        });
        radioContainer.getAllStyles().setMarginUnit(UNIT_TYPE_SCREEN_PERCENTAGE);
        if (Display.getInstance().getDeviceDensity() > Display.DENSITY_VERY_HIGH) {
            skipButton.getAllStyles().setMarginBottom(10);
        } else {
            skipButton.getAllStyles().setMarginBottom(5);
        }
//        skipButtonIcon.addActionListener(evt -> {
//            MainForm.get().show();
//        });

//        Container southLayout = BorderLayout.south(BoxLayout.encloseY(
//                radioContainer,
//                skipButton
//        ));

        add(walkthruTabs);
        add(radioContainer);
        revalidate();

        int lastElementOnSlideEndingY = 0;
        for (TabPage page : pages) {
            if (lastElementOnSlideEndingY < page.getLastComponentEndingY()) {
                lastElementOnSlideEndingY = page.getLastComponentEndingY();
            }
        }
        int centerBetweenSlideAndBottom = lastElementOnSlideEndingY + ((getDisplayHeight() - lastElementOnSlideEndingY) / 10);
        radioContainer.getAllStyles().setMarginUnit(UNIT_TYPE_PIXELS);
        radioContainer.getAllStyles().setMarginTop(centerBetweenSlideAndBottom);

    }


    public TabPage getFirstTab() {
        return buildTab("walkthru1.png",
                "Locate charging station",
                "Find stations around you, see available powerbanks " +
                        "and get directions in Google Maps.",
                "WalkthruTab1",
                false);
    }

    public TabPage getSecondTab() {
        return buildTab("walkthru2.png",
                "Pick up a powerbank",
                "Use app to scan QR code and get your powerbank. Track you rent progress and balance.",
                "WalkthruTab2",
                false);
    }

    public TabPage getThirdTab() {
        return buildTab("walkthru3.png",
                "Let others to power up",
                "Charge you gadget as long as you want and return back in any charging station.",
                "WalkthruTab3",
                true);
    }

    public TabPage buildTab(String imageName, String text, String subText, String tabId, boolean isSkipButton) {
        ScaleImageLabel imageLabel = new ScaleImageLabel(Resources.getGlobalResources().getImage(imageName));
        imageLabel.setUIID("WalkthruPic");

        SpanLabel walkthruSubText = new SpanLabel(subText, "WalkthruSubText");
        Container container = BorderLayout.centerAbsolute(BoxLayout.encloseY(
                imageLabel,
                new Label(text, "WalkthruWhiteText"),
                walkthruSubText
        ));
        container.setUIID(tabId);
        if (isSkipButton) {
            container = LayeredLayout.encloseIn(container, BorderLayout.south(skipButton));
        }

        return new TabPage(container, walkthruSubText);
    }

    private static class TabPage {
        private final Container tabContainer;
        private final Component lastComponent;
        private RadioButton radioButton;

        public TabPage(Container tabContainer, Component component) {
            this.tabContainer = tabContainer;
            this.lastComponent = component;
        }

        public Container getTabContainer() {
            return tabContainer;
        }

        public int getLastComponentEndingY() {
            return lastComponent.getY() + lastComponent.getHeight();
        }

        public RadioButton getRadioButton() {
            return radioButton;
        }

        public void setRadioButton(RadioButton radioButton) {
            this.radioButton = radioButton;
        }
    }
}
