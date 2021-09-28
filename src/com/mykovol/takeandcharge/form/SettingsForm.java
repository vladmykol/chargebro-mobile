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

import com.codename1.io.Preferences;
import com.codename1.l10n.L10NManager;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.spinner.Picker;
import com.mykovol.takeandcharge.TakeAndChargeMain;
import com.mykovol.takeandcharge.form.component.CustomDialog;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.tools.FormCommand;

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
//        setTransitionInAnimator(CommonTransitions.createCover(CommonTransitions.SLIDE_HORIZONTAL, false, 200));
//        setTransitionOutAnimator(CommonTransitions.createSlide(CommonTransitions.SLIDE_HORIZONTAL, true, 300));

        FormCommand.setCloseAction(MainForm.get(), this);

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
        landPicket.setSelectedString(getLandPrefTranslated());
        landPicket.addActionListener(evt -> {
            if (!getLandPrefTranslated().equals(landPicket.getSelectedString())) {
                CustomDialog customDialog = new CustomDialog("Warning", "In order to language changes take effect, you need to restart the application. Exit now?", false);
                customDialog.addYesCancelButtons("OK", ev -> {
                    setLandPref(landPicket.getSelectedString());
                    TakeAndChargeMain.loadLocalization();
                    Display.getInstance().exitApplication();
                });
                customDialog.showWithAnimationSafety();
//            });
            }
        });

//        SwitchList switchList = new SwitchList(new DefaultListModel("Improve", "Show notification"));
//        switchList.addActionListener(e -> {
//            Dialog.show("Info", "You selected " + Arrays.toString(switchList.getMultiListModel().getSelectedIndices()), "Ok", null);
//        });
//        switchList.setScrollableY(true);

        final Label delimiter = new Label("", "SettingsFormDelimiter");
        delimiter.setShowEvenIfBlank(true);
        final Label delimiter2 = new Label("", "SettingsFormDelimiter");
        delimiter2.setShowEvenIfBlank(true);

        Button existButton = new Button("Sign out", "SettingsFormText");
        existButton.setMaterialIcon(FontImage.MATERIAL_EXIT_TO_APP);
        final CustomDialog customDialog = new CustomDialog("Are you sure you want to logout?", "");
        customDialog.addYesCancelButtons("Yes", evt1 -> {
            UserService.onUserLogout();
            setTransitionOutAnimator(CommonTransitions.createEmpty());
            MainForm.get().show();
        });
        existButton.addActionListener(evt -> {
            customDialog.showWithAnimationSafety();
        });


        addAll(headerText,
                spaceLabel,
                BorderLayout.centerEastWest(landPicket, null, new Label("Language", "SettingsFormText")),
                delimiter2,
                existButton
        );

        addShowListener(evt -> {
            if (UserService.isLoggedIn()) {
                existButton.setVisible(true);
            } else {
                existButton.setVisible(false);
            }
        });
    }

    public static String getLandPref() {
        final String userLang = Preferences.get("userLang", "default");
        if (userLang.equals("default")) {
            return L10NManager.getInstance().getLanguage();
        } else {
            return userLang;
        }
    }

    public static void setLandPref(String lang) {
        if (lang.equals("ua")) {
            Preferences.set("userLang", "uk");
        } else {
            Preferences.set("userLang", lang);
        }
    }

    public static String getLandPrefTranslated() {
        final String userLang = Preferences.get("userLang", "default");
        if (userLang.equals("uk")) {
            return "ua";
        } else {
            return userLang;
        }
    }
}
