package com.mykovol.takeandcharge.form;

import com.codename1.components.ScaleImageLabel;
import com.codename1.components.SpanButton;
import com.codename1.components.SpanLabel;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.util.Resources;

import java.util.ArrayList;

/**
 * A swipe tutorial for the application
 *
 * @author Shai Almog
 */
public class WalkthruForm extends Form {
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

        Button skipButton = new Button("Skip tutorial ", "WalkthrSkipButton");
        Button skipButtonIcon = new Button("", "WalkthrSkipButton");
        skipButtonIcon.setMaterialIcon(FontImage.MATERIAL_ARROW_FORWARD);
        skipButtonIcon.getAllStyles().setMarginLeft(0);
        skipButtonIcon.getAllStyles().setPaddingLeft(0);
        skipButton.getAllStyles().setMarginRight(0);

        skipButton.addActionListener(evt -> {
            MainForm.get().show();
        });
        skipButtonIcon.addActionListener(evt -> {
            MainForm.get().show();
        });

        Container southLayout = BoxLayout.encloseY(
                radioContainer,
                FlowLayout.encloseRight(skipButton, skipButtonIcon)
        );

        add(walkthruTabs);
        add(BorderLayout.south(southLayout));

        for (TabPage page : pages) {
            Component.setSameWidth(page.getSpaceLabel(), southLayout);
        }

    }


    public TabPage getFirstTab() {
        return buildTab("walkthru1.png",
                "Locate charging station",
                "Find stations around you, see available powerbanks " +
                        "and get directions in Google Maps.",
                "WalkthruTab1");
    }

    public TabPage getSecondTab() {
        return buildTab("walkthru2.png",
                "Pick up a powerbank",
                "Use app to scan QR code and get your powerbank. Track you rent progress and balance.",
                "WalkthruTab2");
    }

    public TabPage getThirdTab() {
        return buildTab("walkthru3.png",
                "Let others to power up",
                "Charge you gadget as long as you want and return back in any charging station.",
                "WalkthruTab3");
    }

    public TabPage buildTab(String imageName, String text, String subText, String tabId) {
        Label spaceLabel = new Label();
        ScaleImageLabel scaleImageLabel = new ScaleImageLabel(Resources.getGlobalResources().getImage(imageName));
        scaleImageLabel.setUIID("WalkthruPic");
        Container container = BorderLayout.centerAbsolute(BoxLayout.encloseY(
                scaleImageLabel,
                new Label(text, "WalkthruWhiteText"),
                new SpanLabel(subText, "WalkthruSubText"),
                spaceLabel
        ));
        container.setUIID(tabId);

        return new TabPage(container, spaceLabel);
    }

    private static class TabPage {
        private final Container tabContainer;
        private final Label spaceLabel;
        private RadioButton radioButton;

        public TabPage(Container tabContainer, Label spaceLabel) {
            this.tabContainer = tabContainer;
            this.spaceLabel = spaceLabel;
        }

        public Container getTabContainer() {
            return tabContainer;
        }

        public Label getSpaceLabel() {
            return spaceLabel;
        }

        public RadioButton getRadioButton() {
            return radioButton;
        }

        public void setRadioButton(RadioButton radioButton) {
            this.radioButton = radioButton;
        }
    }
}
