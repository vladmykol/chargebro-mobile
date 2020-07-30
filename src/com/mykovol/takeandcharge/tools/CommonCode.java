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

package com.mykovol.takeandcharge.tools;

import com.codename1.components.MultiButton;
import com.codename1.io.Log;
import com.codename1.messaging.Message;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.animations.Transition;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.util.Callback;
import com.codename1.util.SuccessCallback;
import com.mykovol.takeandcharge.form.LoginForm;
import com.mykovol.takeandcharge.form.MainForm;
import com.mykovol.takeandcharge.form.RegisterCreditCardStep3;
import com.mykovol.takeandcharge.form.RegisterMobileNumberStep1;
import com.mykovol.takeandcharge.service.RentService;
import com.mykovol.takeandcharge.service.UserService;

import java.io.IOException;

import static com.codename1.ui.CN.convertToPixels;
import static com.codename1.ui.CN.getCurrentForm;

/**
 * Common code for construction and initialization of various classes e.g. the side menu logic etc.
 *
 * @author Shai Almog
 */
public class CommonCode {
    private final static Command loginCommand = getLoginCommand();
    private final static Command registerCommand = getRegisterCommand();
    private final static Command addPaymentMethodCommand = getAddPaymentMethod();
    private final static Command supportCommand = getSupportCommand();
    private final static Command signOutCommandCommand = getSignOutCommand();
    private static Image avatar;

    public static Image getAvatar(SuccessCallback<Image> avatarChanged) {
        if (avatar == null) {
            int size = convertToPixels(10);
            Image temp = Image.createImage(size, size, 0xff000000);
            Graphics g = temp.getGraphics();
            g.setAntiAliased(true);
            g.setColor(0xffffff);
            g.fillArc(0, 0, size, size, 0, 360);
            Object mask = temp.createMask();
//            UserService.fetchAvatar(i -> {
//                avatar = i.fill(size, size).applyMask(mask);
//                avatarChanged.onSucess(avatar);
//            });
            if (avatar != null) {
                return avatar;
            }
            Style s = new Style();
            s.setFgColor(0xc2c2c2);
            s.setBgTransparency(255);
            s.setBgColor(0xe9e9e9);
            FontImage x = FontImage.createMaterial(FontImage.MATERIAL_PERSON, s, size);
            avatar = x.fill(size, size);
            if (avatar instanceof FontImage) {
                avatar = ((FontImage) avatar).toImage();
            }
            avatar = avatar.applyMask(mask);
        }
        return avatar;
    }

    public static Image setAvatar(String imageFile) {
        int size = convertToPixels(10);
        Image temp = Image.createImage(size, size, 0xff000000);
        Graphics g = temp.getGraphics();
        g.setAntiAliased(true);
        g.setColor(0xffffff);
        g.fillArc(0, 0, size, size, 0, 360);
        Object mask = temp.createMask();

        try {
            Image img = Image.createImage(imageFile);
            avatar = img.fill(size, size).applyMask(mask);
        } catch (IOException err) {
            // this is unlikely as we just grabbed the image...
            Log.e(err);
        }
        return avatar;
    }

    public static MultiButton createEntry(char icon, String title) {
        MultiButton b = new MultiButton(title);
        b.setUIID("Container");
        b.setUIIDLine1("WhereToButtonLine1");
        b.setIconUIID("WhereToButtonIcon");
        FontImage.setMaterialIcon(b, icon);
        return b;
    }

    public static MultiButton createEntry(char icon, String title, String subtitle) {
        MultiButton b = new MultiButton(title);
        b.setTextLine2(subtitle);
        b.setUIID("Container");
        b.setUIIDLine1("WhereToButtonLineNoBorder");
        b.setUIIDLine2("WhereToButtonLine2");
        b.setIconUIID("WhereToButtonIcon");
        FontImage.setMaterialIcon(b, icon);
        return b;
    }


    public static Label createSeparator() {
        Label sep = new Label("", "WhereSeparator");
        sep.setShowEvenIfBlank(true);
        return sep;
    }

    public static void constructSideMenu(Toolbar tb, Button screenBlocking) {
        Button userAndAvatar = new Button("Welcome Stranger", "AvatarBlock");
//        userAndAvatar.setIcon(getAvatar(i -> userAndAvatar.setIcon(i)));
//        userAndAvatar.setGap(convertToPixels(4));
//        userAndAvatar.addActionListener(e -> new EditAccountForm().show());
        tb.addComponentToSideMenu(userAndAvatar);

        refreshCommands(tb);


        Button legalButton = new Button("Legal", "Legal");
        Container legal = BorderLayout.centerCenterEastWest(null, new Label("v0.0.1", "Legal"), legalButton);
        legal.setLeadComponent(legalButton);
        legal.setUIID("SideNavigationPanel");
        tb.setComponentToSideMenuSouth(legal);
////
//        tb.getLeftSideMenuButton().addActionListener(evt -> {
//            Log.p(("screen blocking");
//            screenBlocking.setVisible(true);
//        });
//
//
//
//        screenBlocking.addActionListener(evt -> {
//            if (!tb.getMenuBar().isMenuShowing()) {
//                screenBlocking.setVisible(false);
//                Log.p(("screen blocking false");
//            }
//        });
    }

    private static void refreshCommands(Toolbar tb) {
        tb.removeCommand(loginCommand);
        tb.removeCommand(registerCommand);
        tb.removeCommand(addPaymentMethodCommand);
        tb.removeCommand(supportCommand);
        tb.removeCommand(signOutCommandCommand);

        if (UserService.isLoggedIn()) {
            tb.addCommandToLeftSideMenu(addPaymentMethodCommand);
            tb.addCommandToLeftSideMenu(supportCommand);
            tb.addCommandToLeftSideMenu(signOutCommandCommand);
        } else {
            tb.addCommandToLeftSideMenu(loginCommand);
            tb.addCommandToLeftSideMenu(registerCommand);
            tb.addCommandToLeftSideMenu(supportCommand);
        }
    }

    public static void refreshCommands() {
        refreshCommands(MainForm.get().getToolbar());
    }

    /**
     * Initializes a form with a black background title animation style
     *
     * @param f the form
     */
    public static void initBlackTitleForm(Form f, String title, SuccessCallback<String> searchResults) {
        Form backTo = getCurrentForm();
        f.getContentPane().setScrollVisible(false);
        Button back = new Button("", "TitleCommand");
        removeTransitionsTemporarily(backTo);
        back.addActionListener(e -> backTo.showBack());
        back.getAllStyles().setFgColor(0xffffff);
        FontImage.setMaterialIcon(back, FontImage.MATERIAL_ARROW_BACK);

        f.setBackCommand(new Command("") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                backTo.showBack();
            }
        });

        Container searchBack = null;
        if (searchResults != null) {
            Button search = new Button("", "TitleCommand");
            search.getAllStyles().setFgColor(0xffffff);
            FontImage.setMaterialIcon(search, FontImage.MATERIAL_SEARCH);
            search.addActionListener(e -> {

            });
            searchBack = BorderLayout.north(
                    BorderLayout.centerEastWest(null, search, back));
        } else {
            searchBack = BorderLayout.north(
                    BorderLayout.centerEastWest(null, null, back));
        }

        Label titleLabel = new Label(title, "WhiteOnBlackTitle");

        titleLabel.getAllStyles().setMarginTop(back.getPreferredH());
        titleLabel.getAllStyles().setMarginUnit(Style.UNIT_TYPE_PIXELS, Style.UNIT_TYPE_DIPS, Style.UNIT_TYPE_DIPS, Style.UNIT_TYPE_DIPS);

        f.getToolbar().setTitleComponent(LayeredLayout.encloseIn(searchBack, titleLabel));

        f.getAnimationManager().onTitleScrollAnimation(titleLabel.createStyleAnimation("WhiteOnBlackTitleLeftMargin", 200));

        f.setTransitionInAnimator(CommonTransitions.createCover(CommonTransitions.SLIDE_VERTICAL, false, 300));
        f.setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, true, 300));
    }

    public static void removeTransitionsTemporarily(final Form f) {
        final Transition originalOut = f.getTransitionOutAnimator();
        final Transition originalIn = f.getTransitionInAnimator();
        f.setTransitionOutAnimator(CommonTransitions.createEmpty());
        f.setTransitionInAnimator(CommonTransitions.createEmpty());
        f.addShowListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                f.setTransitionOutAnimator(originalOut);
                f.setTransitionInAnimator(originalIn);
                f.removeShowListener(this);
            }
        });
    }

    private static Command getLoginCommand() {
        return getCommand("Login", FontImage.MATERIAL_PERSON, evt -> {
            new LoginForm().show();
        });
    }

    private static Command getAddPaymentMethod() {
        return getCommand("Add Payment method", FontImage.MATERIAL_PERSON_ADD, e -> {
            RentService.prepareCheckout(new Callback<String>() {
                @Override
                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                    MainForm.get().showErrorDraggablePanel(errorCode + " " + errorMessage);
                }

                @Override
                public void onSucess(String checkoutUrl) {
                    new RegisterCreditCardStep3(checkoutUrl).show();
                }
            });
        });
    }


    private static Command getSupportCommand() {
        return getCommand("Support", FontImage.MATERIAL_CONTACT_SUPPORT, evt -> {
            String email = "support@your-domain.example.com";
            Message message = new Message("");
            Display.getInstance().sendMessage(new String[]{email}, "Take&Charge", message);
        });
    }


    private static Command getRegisterCommand() {
        return getCommand("Register", FontImage.MATERIAL_PERSON_ADD, evt -> {
            new RegisterMobileNumberStep1().show();
        });
    }

    private static Command getSignOutCommand() {
        return getCommand("Sign out", FontImage.MATERIAL_EXIT_TO_APP, e -> {
            UserService.logout();
        });
    }

    private static Command getCommand(String name, char materialIcon, final ActionListener evt) {
        Command cmd = Command.create(name, null, evt);
        cmd.setIconGapMM(2);
        cmd.setMaterialIcon(materialIcon);
        return cmd;
    }
}
