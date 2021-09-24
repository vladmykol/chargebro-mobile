/*
 * Copyright (c) 2012, Codename One and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Codename One designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Codename One through http://www.codenameone.com/ if you
 * need additional information or have any questions.
 */

package com.mykovol.takeandcharge.form;

import com.codename1.ui.*;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.mykovol.takeandcharge.service.GlobalConst;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.tools.CommonCode;
import com.mykovol.takeandcharge.tools.FormCommand;

import static com.codename1.ui.CN.convertToPixels;
import static com.mykovol.takeandcharge.service.GlobalConst.POLICY_URL;
import static com.mykovol.takeandcharge.tools.CommonCode.sendSupportEmail;

/**
 * @author Vlad Mykol
 */
public class InfoForm extends Form {
    final Button ourSiteButton = new Button("What is ChargeBro?", "InfoFormLink");
    private final Label headerText = new Label("About", "WalletFromHeader");

    public InfoForm() {
        super(new BoxLayout(BoxLayout.Y_AXIS));

        setScrollableY(false);
        setTensileDragEnabled(false);

        setFormBottomPaddingEditingMode(true);
        setToolbar(new Toolbar(false));
//        setTransitionInAnimator(CommonTransitions.createEmpty());
//        setTransitionOutAnimator(CommonTransitions.createSlide(CommonTransitions.SLIDE_HORIZONTAL, true, 300));

//        setTransitionOutAnimator(CommonTransitions.createEmpty());

//        getToolbar().addCommandToRightBar(CommonCode.getCloseCommand(MainForm.get()));
        FormCommand.setCloseAction(MainForm.get(), this);

        //        SwitchList switchList = new SwitchList(new DefaultListModel("Improve", "Show notification"));
//        switchList.addActionListener(e -> {
//            Dialog.show("Info", "You selected " + Arrays.toString(switchList.getMultiListModel().getSelectedIndices()), "Ok", null);
//        });
//        switchList.setScrollableY(true);
        final Label delimiter = new Label("", "SettingsFormDelimiter");
        delimiter.setShowEvenIfBlank(true);
        final Label delimiter1 = new Label("", "SettingsFormDelimiter");
        delimiter1.setShowEvenIfBlank(true);
        final Label delimiter2 = new Label("", "SettingsFormDelimiter");
        delimiter2.setShowEvenIfBlank(true);

        ourSiteButton.addActionListener(evt -> {
            Display.getInstance().execute("https://chargebro.com/");
        });

        Label spaceLabel = new Label(" ");
        if (!Display.getInstance().isTablet() && Display.getInstance().getDeviceDensity() < Display.DENSITY_HD) {
            spaceLabel = new Label();
            setTitle(headerText.getText());
            headerText.setHidden(true);
            spaceLabel.setHidden(true);
        }

        getContentPane().getAllStyles().setMarginUnit(Style.UNIT_TYPE_DIPS);
        getContentPane().getAllStyles().setMargin(0, 4, 3.5f, 3.5f);

        final Button termsLinkButton = new Button("Terms&Conditions", "InfoFormLink");
        final BrowserPopUp termsForm = new BrowserPopUp("Terms&Conditions");
        termsForm.setFadeBackDownTo(this);
        termsForm.serUrlNoReload(POLICY_URL);
        termsLinkButton.addActionListener(evt -> {
            CommonCode.removeTransitionsTemporarily(this);
            termsForm.show();
        });

        final Label versionLabelText = new Label("app version", "InfoFormText");
        final Button versionNum = new Button(Display.getInstance().getProperty("AppVersion", "unknown")
                + (GlobalConst.isRunningOnLocalHost() ? " (debug mode)" : ""), "InfoFormTextVersion");

        versionNum.addActionListener(evt -> {
            UserService.checkForNewVersion(true);
        });

        final Button reportABugCommand = new Button("Report a problem", "InfoFormReportIssue");
        reportABugCommand.setMaterialIcon(FontImage.MATERIAL_MAIL);
        reportABugCommand.setGap(convertToPixels(2));
        reportABugCommand.addActionListener(evt -> {
            sendSupportEmail();
        });


        addAll(headerText,
                spaceLabel,
                BorderLayout.centerEastWest(BoxLayout.encloseXRight(versionNum), null, versionLabelText),
                ourSiteButton,
                delimiter,
                termsLinkButton,
                delimiter1,
                reportABugCommand
        );
    }
}
