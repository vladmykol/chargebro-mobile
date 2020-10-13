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

import com.codename1.components.SwitchList;
import com.codename1.io.Preferences;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.list.DefaultListModel;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.spinner.Picker;
import com.mykovol.takeandcharge.TakeAndChargeMain;
import com.mykovol.takeandcharge.tools.CommonCode;

/**
 * @author Vlad Mykol
 */
public class SettingsForm extends Form {
    private final Label headerText = new Label("Settings", "WalletFromHeader");

    public SettingsForm() {
        super(new BoxLayout(BoxLayout.Y_AXIS));

        setScrollableY(false);
        setTensileDragEnabled(false);

        setFormBottomPaddingEditingMode(true);
        setToolbar(new Toolbar(false));
        setTransitionInAnimator(CommonTransitions.createEmpty());
        setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, false, 300));

        getToolbar().addCommandToRightBar(CommonCode.getCloseCommand(MainForm.get()));

        Label spaceLabel = new Label(" ");
        if (!Display.getInstance().isTablet() && Display.getInstance().getDeviceDensity() < Display.DENSITY_HD) {
            spaceLabel = new Label();
            setTitle(headerText.getText());
            headerText.setHidden(true);
            spaceLabel.setHidden(true);
        }

        getContentPane().getAllStyles().setMarginUnit(Style.UNIT_TYPE_DIPS);
        getContentPane().getAllStyles().setMargin(0, 4, 3.5f, 3.5f);

        Picker landPicket = new Picker();
        landPicket.setType(Display.PICKER_TYPE_STRINGS);

        landPicket.setStrings("ua", "ru", "en", "default");
        landPicket.setSelectedString(">");

        landPicket.addActionListener(evt -> {
//            UITimer.timer(1000, false, this, () -> {
            if (landPicket.getSelectedString().equals("ua")) {
                Preferences.set("userLang", "uk");
            } else {
                Preferences.set("userLang", landPicket.getSelectedString());
            }
            TakeAndChargeMain.loadLocalization();
            Dialog.show("Warning", "In order to language changes take effect, you need to restart the application", "OK", null);
//            });
        });

        SwitchList switchList = new SwitchList(new DefaultListModel("Improve", "Show notification"));
//        switchList.addActionListener(e -> {
//            Dialog.show("Info", "You selected " + Arrays.toString(switchList.getMultiListModel().getSelectedIndices()), "Ok", null);
//        });
//        switchList.setScrollableY(true);

        final Label delimiter = new Label("", "SettingsFormDelimiter");
        delimiter.setShowEvenIfBlank(true);
        final Label delimiter2 = new Label("", "SettingsFormDelimiter");
        delimiter2.setShowEvenIfBlank(true);

        addAll(headerText,
                spaceLabel,
                delimiter,
                BorderLayout.centerEastWest(landPicket, null, new Label("Language", "SettingsFormText")),
                delimiter2
        );
    }
}
