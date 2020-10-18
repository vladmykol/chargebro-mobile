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

import com.codename1.capture.Capture;
import com.codename1.components.InteractionDialog;
import com.codename1.components.ScaleImageLabel;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Preferences;
import com.codename1.io.Storage;
import com.codename1.io.Util;
import com.codename1.messaging.Message;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.animations.Transition;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.util.ImageIO;
import com.codename1.ui.util.Resources;
import com.mykovol.takeandcharge.form.*;
import com.mykovol.takeandcharge.service.GlobalConst;
import com.mykovol.takeandcharge.service.UserService;

import java.io.IOException;
import java.io.OutputStream;

import static com.codename1.ui.CN.convertToPixels;
import static com.codename1.ui.CN1Constants.GALLERY_IMAGE;
import static com.codename1.ui.ComponentSelector.$;
import static com.codename1.ui.plaf.Style.BACKGROUND_IMAGE_SCALED;
import static com.codename1.ui.plaf.Style.BACKGROUND_IMAGE_SCALED_FILL;

/**
 * Common code for construction and initialization of various classes e.g. the side menu logic etc.
 *
 * @author Vlad Mykol
 */
public class CommonCode {
    private final static ScaleImageLabel waveMask = new ScaleImageLabel(Resources.getGlobalResources().getImage("wave.png"));
    private final static Image defaultAvatarImage = Resources.getGlobalResources().getImage("defaultAvatar.png");
    private final static Container avatarBackground = new Container();
    private static final String SIDE_MENU_SWIPE_START_X = "sideMenuSwipeStartX";
    private final static Label avatarText = new Label("ChargeBro", "AvatarText");
    private final static Button avatarButton = new Button("");
    private final static Label avatarSubText = new Label("", "AvatarSubText");
    private final static Button signOutButton = getSignOutButton();
    private final static Button supportButton = getSupportButton();
    private static InteractionDialog sideMenu;
    private final static Button loginButton = getLoginButton();
    private final static Button registerButton = getRegisterButton();
    private final static Button historyButton = getHistoryButton();
    private final static Button promoCodeButton = getPromoCodeButton();
    private final static Button creditCardButton = getCreditCards();
    private final static Button stationsAround = getStationsAround();
    private final static Button settingsButton = getSettingsButton();
    private final static Button infoButton = getInfoButton();
    private static Container menuItemsContainer;


    private static Label avatarPenImage;

    public static void refreshUserInfo() {
        if (UserService.isLoggedIn()) {
//            avatarText.setHidden(true);;
            avatarSubText.setHidden(false);
            loadAndSetAvatar();
        } else {
//            avatarText.setHidden(false);
            avatarSubText.setHidden(true);
            setAvatar(defaultAvatarImage);
        }
        avatarSubText.setText(Preferences.get("phoneNumber", "* * * *"));
        avatarText.getParent().revalidateWithAnimationSafety();
    }

    public static void setAvatar(Image image) {
        if (image.equals(defaultAvatarImage)) {
            avatarPenImage.setVisible(true);
        } else {
            avatarPenImage.setVisible(false);
        }

        Image maskedImage = applyMaskToAvatar(image);
        avatarButton.setIcon(maskedImage);
    }

    public static void saveAndSetAvatar(String imageFile) {
        try {
            String pathToBeStored = FileSystemStorage.getInstance().getAppHomePath() + "userAvatar.jpg";
            Image img = Image.createImage(imageFile);
            OutputStream os = FileSystemStorage.getInstance().openOutputStream(pathToBeStored);
            ImageIO.getImageIO().save(img, os, ImageIO.FORMAT_JPEG, 0.9f);
            os.close();
            setAvatar(img);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadAndSetAvatar() {
        String pathToImage = FileSystemStorage.getInstance().getAppHomePath() + "userAvatar.jpg";
        try {
            Image img = Image.createImage(FileSystemStorage.getInstance().openInputStream(pathToImage));
            setAvatar(img);
        } catch (Exception ex) {
            setAvatar(defaultAvatarImage);
        }
    }

    public static Image applyMaskToAvatar(Image image) {
        int size = convertToPixels(12);
        Image temp = Image.createImage(size, size, 0xff000000);
        Graphics g = temp.getGraphics();
        g.setAntiAliased(true);
        g.setColor(0xffffff);
        g.fillArc(0, 0, size, size, 0, 360);
        Object mask = temp.createMask();

        return image.fill(size, size).applyMask(mask);
    }

    public static Label createSeparator() {
        Label sep = new Label("", "WhereSeparator");
        sep.setShowEvenIfBlank(true);
        return sep;
    }


    public static void constructSideMenu(Toolbar tb, Form parentForm) {
        avatarButton.setUIID("AvatarButton");

        avatarButton.addActionListener(e -> {
//            new EditAccountForm().show();
            if (UserService.isLoggedIn()) {
                chooseNewAvatar();
            } else {
                new LoginForm().show();
            }
        });

        avatarBackground.setUIID("AvatarBackground");
        waveMask.setUIID("AvatarWave");
        if (!Display.getInstance().isTablet() && Display.getInstance().getDeviceDensity() < Display.DENSITY_HD) {
            waveMask.setBackgroundType(BACKGROUND_IMAGE_SCALED_FILL);
        } else {
            waveMask.setBackgroundType(BACKGROUND_IMAGE_SCALED);
        }

        int size = convertToPixels(4);
        Image penImage = Resources.getGlobalResources().getImage("avatarPen.png").fill(size, size);
        avatarPenImage = new Label(penImage, "AvatarPen");

        Label spaceHolder = new Label();
        spaceHolder.setShowEvenIfBlank(true);
        spaceHolder.getAllStyles().setMarginBottom(100);

        final Container avatarAndPen = LayeredLayout.encloseIn(avatarButton, avatarPenImage);
        Container avatarContainer = BoxLayout.encloseY(avatarAndPen,
                avatarText, avatarSubText, spaceHolder);
        avatarContainer.setSafeAreaRoot(false);
        avatarContainer.setSafeArea(true);

        loadAndSetAvatar();

        Container menuTopPartHolder = LayeredLayout.encloseIn(avatarBackground,
                avatarContainer,
                BorderLayout.south(waveMask));
        tb.addComponentToSideMenu(menuTopPartHolder);

        menuTopPartHolder.getParent().setTensileDragEnabled(false);
//        if (Display.getInstance().getDeviceDensity() >= Display.DENSITY_HD) {
//            menuTopPartHolder.getParent().setScrollableY(false);
//        }

        menuItemsContainer = BorderLayout.west(BoxLayout.encloseY(
                loginButton,
                registerButton,
                stationsAround,
                creditCardButton,
                historyButton,
                promoCodeButton,
                supportButton,
                settingsButton,
                infoButton
        ));

        tb.addComponentToSideMenu(menuItemsContainer);
        menuItemsContainer.setScrollableY(true);
        menuItemsContainer.setTensileDragEnabled(true);
        menuItemsContainer.setScrollVisible(false);

        Container bottomContainer = BorderLayout.centerAbsolute(signOutButton);
        bottomContainer.setUIID("SideNavigationPanel");
        tb.setComponentToSideMenuSouth(bottomContainer);

        sideMenu = (InteractionDialog) bottomContainer.getParent().getParent();

        refreshMenuItems();

        final Container layeredPane = parentForm.getLayeredPane(parentForm.getClass(), true);
        final Button blockingButton = new Button();

        $(blockingButton)
                .setUIID("Container")
                .setVisible(false)
                .stripMarginAndPadding();

        layeredPane.setLayout(new BorderLayout());
        layeredPane.add(BorderLayout.CENTER, blockingButton);

        blockingButton.addPointerDraggedListener(evt -> {
            Integer startX = (Integer) parentForm.getClientProperty(SIDE_MENU_SWIPE_START_X);
            if (startX == null) {
                parentForm.putClientProperty(SIDE_MENU_SWIPE_START_X, evt.getX());
            }
        });

        parentForm.addPointerPressedListener(evt -> {
            if (blockingButton.isVisible()) {
                Boolean menuIsShowed = (Boolean) parentForm.getClientProperty("cn1$sidemenuCharged");
                if (menuIsShowed != null && menuIsShowed) {
                    parentForm.putClientProperty(SIDE_MENU_SWIPE_START_X, evt.getX());
                    parentForm.putClientProperty("cn1$sidemenuCharged", Boolean.FALSE);
                } else {
                    blockingButton.setVisible(false);
                }
            }
        });

        sideMenu.addStateChangeListener(evt -> {
            if (sideMenu.isShowing()) {
                parentForm.putClientProperty(SIDE_MENU_SWIPE_START_X, null);
                blockingButton.setVisible(false);
            }
        });

        parentForm.addPointerReleasedListener(evt -> {
            if (sideMenu.isShowing()) {
                Integer startX = (Integer) parentForm.getClientProperty(SIDE_MENU_SWIPE_START_X);
                if (startX != null) {
                    int draggedLength = startX - evt.getX();
                    parentForm.putClientProperty(SIDE_MENU_SWIPE_START_X, null);
                    if (draggedLength > 100) {
                        tb.closeLeftSideMenu();
                        blockingButton.setVisible(false);
                    }
                }
            }
        });

        tb.getLeftSideMenuButton().addActionListener(evt -> {
            parentForm.putClientProperty("cn1$sidemenuCharged", Boolean.FALSE);
            blockingButton.setVisible(true);
        });


    }

    public static void chooseNewAvatar() {
        if (Dialog.show("Confirmation", "Would you like to use the camera or the gallery?", "Camera", "Gallery")) {
            String pic = Capture.capturePhoto();
            if (pic != null) {
                saveAndSetAvatar(pic);
            }
        } else {
            CN.openGallery(ee -> {
                if (ee != null && ee.getSource() != null) {
                    saveAndSetAvatar((String) ee.getSource());
                } else {
                    MainForm.get().showErrorOnMainScreen("Not possible to open a gallery. Please check application permissions", 500);
                }
            }, GALLERY_IMAGE);
        }
    }

    public static void refreshMenuItems() {
        refreshUserInfo();

        if (UserService.isLoggedIn()) {
            loginButton.setHidden(true);
            registerButton.setHidden(true);

            historyButton.setHidden(false);
            creditCardButton.setHidden(false);
            signOutButton.setHidden(false);
        } else {
            loginButton.setHidden(false);
            registerButton.setHidden(false);

            historyButton.setHidden(true);
            creditCardButton.setHidden(true);
            signOutButton.setHidden(true);
        }
        historyButton.getParent().revalidate();
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

    private static Button getLoginButton() {
        return constructSideMenuButton("Login", FontImage.MATERIAL_PERSON, new LoginForm());
    }

    private static Button getStationsAround() {
        final BrowserPopUp price = new BrowserPopUp("Nearest stations");
        return constructSideMenuButton("Nearest stations", FontImage.MATERIAL_EV_STATION, new ComingSoonForm("Nearest stations", MainForm.get()));
    }


//    private static Button getCreditCards() {
//        return constructSideMenuButton("ADD CARD WEB", FontImage.MATERIAL_CREDIT_CARD, e -> {
//            MainGifLoader.get().start();
//            RentService.prepareCheckout(new Callback<String>() {
//                @Override
//                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
//                    MainGifLoader.get().stop();
//                    MainForm.get().showError(errorMessage, errorCode);
//                }
//
//                @Override
//                public void onSucess(String checkoutUrl) {
//                    MainGifLoader.get().stop();
//                    Display.getInstance().execute(checkoutUrl);
//                }
//            });
//        });
//    }

    private static Button getCreditCards() {
        return constructSideMenuButton("Payment", FontImage.MATERIAL_CREDIT_CARD, new WalletForm());
    }

    private static Button getHistoryButton() {
        return constructSideMenuButton("History", FontImage.MATERIAL_HISTORY, new ComingSoonForm("Rent history", MainForm.get()));
    }

    private static Button getPromoCodeButton() {
        return constructSideMenuButton("Promocode", FontImage.MATERIAL_LOCAL_OFFER, new ComingSoonForm("Promocode", MainForm.get()));
    }

//    private static Button getPayForPbButton() {
//        return constructSideMenuButton("Pay for rent", FontImage.MATERIAL_CREDIT_CARD, e -> {
//            RentService.prepareCheckout(new Callback<String>() {
//                @Override
//                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
//                    MainForm.showError(errorMessage, errorCode);
//                }
//
//                @Override
//                public void onSucess(String checkoutUrl) {
//                    Display.getInstance().execute(checkoutUrl, evt -> {
//                        MainForm.showError("All good! Error is just for test", 0);
//                    });
//                }
//            });
//        });
//    }


    private static Button getSupportButton() {
        Button sideMenuButton = new Button("Contact us", "SideMenuButton");
        sideMenuButton.addActionListener(evt -> {
            MainForm.get().getToolbar().closeLeftSideMenu();
            sendSupportEmail();
        });
        sideMenuButton.setIconUIID("SideMenuButtonIcon");
        sideMenuButton.setMaterialIcon(FontImage.MATERIAL_EMAIL);
        sideMenuButton.setGap(convertToPixels(2));
        return sideMenuButton;
    }


    private static Button getRegisterButton() {
        return constructSideMenuButton("Register", FontImage.MATERIAL_PERSON_ADD, new RegistrationForm());
    }


    private static Button getSignOutButton() {
        Button sideMenuButton = new Button("Sign out", "SideMenuButtonSignOut");
        sideMenuButton.addActionListener(evt -> {
            UserService.onUserLogout();
        });
        return sideMenuButton;
    }

    private static Button getSettingsButton() {
        return constructSideMenuButton("Settings", FontImage.MATERIAL_SETTINGS, new SettingsForm());
    }

    private static Button getInfoButton() {
        return constructSideMenuButton("About", FontImage.MATERIAL_INFO, new InfoForm());
    }

    private static Button constructSideMenuButton(String name, char materialIcon, Form form) {
        Button sideMenuButton = new Button(name, "SideMenuButton");
        sideMenuButton.setIconUIID("SideMenuButtonIcon");
        sideMenuButton.setMaterialIcon(materialIcon);
        sideMenuButton.setGap(convertToPixels(2));
        form.addShowListener(evt1 -> {
            sideMenu.setAnimateShow(false);
            MainForm.get().getToolbar().closeLeftSideMenu();
        });
        sideMenuButton.addActionListener(evt -> {
            form.show();
        });

        return sideMenuButton;
    }


    public static void sendSupportEmail() {
        MainNoBlockingLoader.get().start();

        final String email = "info@chargebro.com";
        String logText = "";
        try {
            byte[] read = Util.readInputStream(Storage.getInstance().createInputStream("CN1Log__$"));
            logText = new String(read);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final String userPhone = Preferences.get("phoneNumber", "not defined");
        final String appVersion = Display.getInstance().getProperty("AppVersion", "0.1");
        final String content = "\n \n \n -------------- user info (do not delete) ------------------ \n" +
                "User: " + userPhone + "\n" +
                "OS: " + Display.getInstance().getPlatformName() + "\n" +
                "App version: " + appVersion + (GlobalConst.LOCAL ? "(debug mode)" : "") + "\n" +
                "Log: " + logText + "\n";
        Message message = new Message(content);

        MainNoBlockingLoader.get().stop();
        Display.getInstance().sendMessage(new String[]{email}, "Support request", message);
    }
}
