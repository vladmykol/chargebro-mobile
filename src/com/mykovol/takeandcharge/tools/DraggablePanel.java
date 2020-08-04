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

import com.codename1.components.SpanLabel;
import com.codename1.io.Log;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.util.UITimer;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.RentHistory;
import com.mykovol.takeandcharge.form.component.BottomPanel;
import com.mykovol.takeandcharge.form.component.RentBoard;
import com.mykovol.takeandcharge.form.component.RentContent;
import com.mykovol.takeandcharge.service.RentService;
import com.mykovol.takeandcharge.service.UserService;

import java.util.List;
import java.util.Map;

import static com.codename1.ui.CN.*;
import static com.codename1.ui.util.Resources.getGlobalResources;


public class DraggablePanel extends Container {
    public static final int MIN_PANEL_HEIGHT_SCREEN_PERCENTAGE = 15;
    public static final int minPanelHeight = (int) Math.round(getDisplayHeight() * (MIN_PANEL_HEIGHT_SCREEN_PERCENTAGE / 100.0));
    //    private final Container contentHolder = new Container(BoxLayout.y());
    private final Container defaultContent = new Container(BoxLayout.y());
    //    private final Container bottomPanel = new Container(BoxLayout.y());
    private final Container bottomPanel = new BottomPanel(minPanelHeight);
    private final Label draggableImage = new Label(getGlobalResources().getImage("vertical-draggable.png"), "DraggableIcon");
    private final SpanLabel errorLabel = new SpanLabel("something went wrong", "ErrorText");
    private final String defaultTopTitleText = "What's new?";
    private final Label topPanelTitle = new Label(defaultTopTitleText, "BottomPanelFoldedTopText");
    private final Container bottomPanelHeader = BoxLayout.encloseYCenter(draggableImage, errorLabel);
    private final Button screenBlocking;
    private final Button bottomScreenBlocking;
    private final Form attachedForm;
    private final Button scanButton;
    private RentContent rentContent = new RentContent();
    private int firstX = -1, firstY = -1;
    private boolean isDraggingBottomPanel;
    private Container topToolbarPanel;
    private volatile boolean isInMove = false;


    public DraggablePanel(Button screenBlocking,
                          Button bottomDraggablePanelScreenBlocker,
                          Button scanButton,
                          Form currentForm) {
        super(new BorderLayout());
        this.screenBlocking = screenBlocking;
        this.bottomScreenBlocking = bottomDraggablePanelScreenBlocker;
        this.scanButton = scanButton;
        this.attachedForm = currentForm;

        addSwipeListeners();
        initTopPanel();
        add(SOUTH, bottomPanel);
        initBottomPanel();
    }

    private void addNewsToDefaultPanel(String imageName, String headerText, String text) {
        if (defaultContent.getComponentCount()>0) {
            Label panelDelimiter = new Label("", "PanelDelimiter");
            panelDelimiter.setShowEvenIfBlank(true);
            defaultContent.addAll(panelDelimiter);
        }

        Label articlePhoto = new Label(getGlobalResources().getImage(imageName));
        articlePhoto.setUIID("PanelImage");

        SpanLabel articleHeaderText = new SpanLabel(headerText, "PanelHeader");
        articleHeaderText.setEnabled(false);
        SpanLabel articleText = new SpanLabel(text,"PanelText");
        articleText.setEnabled(false);

        defaultContent.addAll(articlePhoto, articleHeaderText, articleText);
    }

    private void initBottomPanel() {
//        bottomPanel.setUIID("UnderBottomPanel");
//        bottomPanel.setPreferredSize(new Dimension(getDisplayWidth(), minPanelHeight));

        addNewsToDefaultPanel("dont-spend-time.png", "Don't wait - Take&Charge",
                "Running out of charge? No need to look for a socket or wait while your gadget is charging. " +
                        "Just take our power bank and go. Free 60 min charging for new clients");

        addNewsToDefaultPanel("like-idea.png", "Like the idea? Then join us?",
                "We are a growing, open mind company and if you want to become part of a team or satisfy your clients with handy power banks, we can make it possible. " +
                        "Contact us now");


        bottomPanel.setUIID("BottomPanelUnfolded");
//        bottomPanel.setBlockLead(true);
//        setScrollableY(true);
//        setScrollVisible(false);
        bottomPanel.setScrollableY(true);
        bottomPanel.setShouldCalcPreferredSize(false);
        bottomPanel.setScrollVisible(false);
        errorLabel.setHidden(true, true);
        errorLabel.setEnabled(false);
        bottomPanel.addAll(bottomPanelHeader, defaultContent);
    }

    public void refreshRentContent() {
        if (UserService.isLoggedIn()) {
            Log.p("refresh rent content");
            RentService.getRentHistory(true, new Callback<List<RentHistory>>() {
                @Override
                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                    if (errorCode != 404) showError(errorMessage);
                }

                @Override
                public void onSucess(List<RentHistory> rentHistoryList) {
                    Map<String, RentBoard> visibleRentBoards = rentContent.getVisibleRentBoards();
                    for (RentHistory rentHistory : rentHistoryList) {
                        String serialNumber = rentHistory.powerBankId.get();
                        long timeElapsed = rentHistory.rentPeriodMs.getLong();
                            RentBoard existingRentRow = visibleRentBoards.remove(serialNumber);
                            if (existingRentRow == null) {
                                addRentRow(serialNumber, timeElapsed);
                            } else {
                                existingRentRow.updateElapsedTime(timeElapsed);
                            }
                    }
                    for (RentBoard showedButNotExistingRentRow : visibleRentBoards.values()) {
                            removeRentRow(showedButNotExistingRentRow);
                    }
                }
            });
        }
    }

    public void addRentRow(String serialNumber, long elapsedTime) {
        RentBoard rentBoard = rentContent.addRow(serialNumber, elapsedTime);
        getCurrentForm().registerAnimated(rentBoard);
        if (!rentContent.isChildOf(bottomPanel)) {
            topPanelTitle.setText(rentContent.getTitleText());
            bottomPanel.replaceAndWait(defaultContent, rentContent, CommonTransitions.createCover(CommonTransitions.SLIDE_VERTICAL, false, 500));
        } else {
            rentContent.animateRentContent(500);
        }
    }

    public void removeRentRow(RentBoard rentBoard) {
        getCurrentForm().deregisterAnimated(rentBoard);
        rentBoard.remove();
//        if (rentContent.isChildOf(bottomPanel)) {
        if (rentContent.noRentRows()) {
            setDefaultContent();
        } else {
            rentContent.animateRentContent(500);
        }
    }

    public void removeRentRow(String serialNumber) {
        Log.p("remove rent row" + serialNumber);
        RentBoard rentBoard = rentContent.findRentBoardByName(serialNumber);
        if (rentBoard != null) removeRentRow(rentBoard);
    }

    private void setDefaultContent() {
        topPanelTitle.setText(defaultTopTitleText);
        if (rentContent.isChildOf(bottomPanel)) {
            bottomPanel.replaceAndWait(rentContent, defaultContent, CommonTransitions.createCover(CommonTransitions.SLIDE_VERTICAL, false, 500));
        }
//        revalidateWithAnimationSafety();
    }


    public void removeAllRentRows() {
        setDefaultContent();
        rentContent = new RentContent();
    }


    public void showError(String errorText) {
        if (errorText == null) return;
        if (!errorText.equals(errorLabel.getText())) errorLabel.setText(errorText.trim());
        if (errorLabel.isHidden()) {
            errorLabel.setHidden(false, false);
            bottomPanel.animateLayoutAndWait(700);
            UITimer.timer(5000, false, getComponentForm(), () -> {
                errorLabel.setHidden(true, false);
                bottomPanel.animateLayoutAndWait(700);
            });
        }
    }

    private void initTopPanel() {
        Button back = new Button("", "BottomPanelFoldedTopText");
        float size = Float.parseFloat(getUIManager().getThemeConstant("menuImageSize", "4.5"));
        FontImage.setMaterialIcon(back, FontImage.MATERIAL_ARROW_BACK, size);
        topToolbarPanel = BoxLayout.encloseX(back, topPanelTitle);
        topToolbarPanel.setUIID("BottomPanelFoldedTop");
        topToolbarPanel.setSafeArea(true);

        back.addActionListener(this::backButtonAction);
    }

    private void backButtonAction(ActionEvent evt) {
            if (isInMove) return;
            isInMove = true;
            removeSwipeListeners();
            attachedForm.getToolbar().setHidden(false);
            revalidateWithAnimationSafety();
            topToolbarPanel.setY(-topToolbarPanel.getHeight());
            rentContent.showTitle();
            bottomPanel.addComponent(0, draggableImage);
            bottomPanel.setY(getDisplayHeight() - minPanelHeight);
            animateUnlayoutAndWait(200, 255);
            topToolbarPanel.remove();
            bottomPanel.remove();
            bottomPanel.setUIID("BottomPanelUnfolded");
            add(SOUTH, bottomPanel);
//        bottomPanel.setPreferredSize(new Dimension(getDisplayWidth(), minPanelHeight));
            screenBlocking.setVisible(false);
            animateLayoutAndWait(100);
            addSwipeListeners();
            isInMove = false;
    }


    public void addSwipeListeners() {
        attachedForm.addPointerDraggedListener(this::processDragEvent);
        attachedForm.addPointerReleasedListener(this::processReleaseEvent);
    }

    public void removeSwipeListeners() {
        attachedForm.removePointerDraggedListener(this::processDragEvent);
        attachedForm.removePointerReleasedListener(this::processReleaseEvent);
    }

    private synchronized void processReleaseEvent(ActionEvent e) {
        if (isInMove) return;
        isInMove = true;
        if (isDraggingBottomPanel) {
            e.consume();
            boolean isDruggingUp = SOUTH.equals(getBottomPanelPosition());
            if (isDruggingUp) {
                if (bottomPanel.getHeight() > minPanelHeight + 3) {
                    removeSwipeListeners();
                    draggableImage.remove();
                    bottomPanel.remove();
                    rentContent.hideTitle();
                    screenBlocking.setVisible(true);
                    getCurrentForm().getToolbar().setHidden(true);
//                    mainContainer.revalidateWithAnimationSafety();
                    bottomPanel.setUIID("BottomPanelFolded");
                    add(NORTH, topToolbarPanel);
                    add(CENTER, bottomPanel);
                    animateLayoutAndWait(100);
                    addSwipeListeners();
                } else {
                    bottomPanel.setPreferredSize(new Dimension(getDisplayWidth(), minPanelHeight));
                    revalidateWithAnimationSafety();
                }
            } else {
                if (firstY < e.getY()) {
                    isInMove = false;
                    backButtonAction(e);
                }
            }
            firstX = -1;
            firstY = -1;
            isDraggingBottomPanel = false;
//            mainContainer.animateLayoutAndWait(200);
        }
        isInMove = false;
    }

    private Object getBottomPanelPosition() {
        return getLayout().getComponentConstraint(bottomPanel);
    }

    private synchronized void processDragEvent(ActionEvent e) {
        if (isDraggingBottomPanel) {
            e.consume();
            boolean isDruggingUp = SOUTH.equals(getBottomPanelPosition());
            if (isDruggingUp) {
                bottomPanel.setPreferredSize(new Dimension(getDisplayWidth(), firstY - e.getY() + minPanelHeight));
                revalidateWithAnimationSafety();
            }
        } else {
            Component draggedCmp = attachedForm.getComponentAt(e.getX(), e.getY());
            if (draggedCmp != null &&
                    (draggedCmp.isChildOf(this)
                            || draggedCmp == screenBlocking
//                            || draggedCmp == scanButton
                            || draggedCmp == bottomScreenBlocking)) {
                if (firstX == -1) {
                    firstX = e.getX();
                    firstY = e.getY();
                }
                e.consume();
                isDraggingBottomPanel = true;
            }
        }
    }


}
