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

package com.mykovol.takeandcharge.form.component;

import com.codename1.io.Log;
import com.codename1.ui.*;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.RentHistory;
import com.mykovol.takeandcharge.form.MainForm;
import com.mykovol.takeandcharge.service.RentService;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.service.WebSocketClient;

import java.util.List;
import java.util.Map;

import static com.codename1.ui.CN.*;
import static com.codename1.ui.util.Resources.getGlobalResources;


public class DraggablePanel extends Container {
    public static final int MIN_PANEL_HEIGHT_SCREEN_PERCENTAGE = 20;
    public static final int minPanelHeight = (int) Math.round(getDisplayHeight() * (MIN_PANEL_HEIGHT_SCREEN_PERCENTAGE / 100.0));
    private final Container contentHolder = new DraggablePanelContent(minPanelHeight);

    private final Button screenBlocking;
    private final Button bottomScreenBlocking;
    private final Form attachedForm;
    private final RentContent rentContent = new RentContent();
    private final Label topPanelTitle = new Label(rentContent.getTitleText(), "BottomPanelFoldedTopText");
    private final Container panelHeader;
    private int firstX = -1, firstY = -1;
    private boolean isDraggingBottomPanel;
    private Container topToolbarPanel;
    private volatile boolean isInMove = false;
    private volatile boolean isDragEnable = true;

    public DraggablePanel(Button screenBlocking,
                          Button bottomDraggablePanelScreenBlocker,
                          Form currentForm) {
        super(new BorderLayout());
        this.screenBlocking = screenBlocking;
        this.bottomScreenBlocking = bottomDraggablePanelScreenBlocker;
        this.attachedForm = currentForm;
        final Label draggableImage = new Label(getGlobalResources().getImage("vertical-draggable.png"), "DraggableIcon");
        panelHeader = BoxLayout.encloseYCenter(draggableImage);

        addSwipeListeners();
        initTopPanel();
        contentHolder.addAll(panelHeader, rentContent);
        contentHolder.setVisible(false);

        add(SOUTH, contentHolder);
    }

    public void show() {
        WebSocketClient.get();
        contentHolder.setVisible(true);
        contentHolder.getParent().revalidate();
        contentHolder.setY(getDisplayHeight());
        bottomScreenBlocking.setVisible(true);
        MainForm.get().hideScanButton();
        animateLayoutFadeAndWait(300, 100);
    }

    public void hide() {
        MainForm.get().showScanButton();
        contentHolder.setY(getDisplayHeight());
        bottomScreenBlocking.setVisible(false);
        animateUnlayout(300, 100, () -> {
            contentHolder.setVisible(false);
            revalidate();
        });
    }

    public void enableDrag() {
        isDragEnable = true;
        contentHolder.setScrollableY(isDragEnable);
        bottomScreenBlocking.setEnabled(isDragEnable);
    }

    public void disableDrag() {
        isDragEnable = false;
        contentHolder.setScrollableY(isDragEnable);
        bottomScreenBlocking.setEnabled(isDragEnable);
    }

    public void refreshRentContent() {
        if (UserService.isLoggedIn()) {
            RentService.getRentHistory(true, new Callback<List<RentHistory>>() {
                @Override
                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                    if (errorCode == 404) {
                        removeAllRentRows();
                    } else {
                        MainForm.get().showError(errorMessage, errorCode);
                    }
                }

                @Override
                public void onSucess(List<RentHistory> rentHistoryList) {
                    syncWithRentBoard(rentHistoryList);
                }
            });
        }
    }

    public void syncWithRentBoard(List<RentHistory> rentHistoryList) {
        Map<String, RentBoard> showedRents = rentContent.getShowedRents();
        for (RentHistory rentHistory : rentHistoryList) {
            String serialNumber = rentHistory.powerBankId.get();
            RentBoard existingRentRow = showedRents.remove(serialNumber);
            if (existingRentRow == null) {
                addRentRow(rentHistory);
            } else {
                setAnimationForTimeCounter(rentHistory.isReturned.getInt(), existingRentRow);
                existingRentRow.updateExisting(rentHistory);
            }
        }
        for (RentBoard showedRent : showedRents.values()) {
            removeRentRow(showedRent);
        }
    }

    public void setAnimationForTimeCounter(int isReturned, RentBoard rentBoard) {
        if (isReturned == 0) {
            registerAnimationForTimeCounter(rentBoard);
        } else {
            deregisterAnimationForTimeCounter(rentBoard);
        }
    }

    public void addRentRow(RentHistory rentHistory) {
        callSerially(() -> {
            RentBoard rentBoard = rentContent.addRow(rentHistory);
            setAnimationForTimeCounter(rentHistory.isReturned.getInt(), rentBoard);

            if (rentContent.getRentRows() == 1) {
                show();
            } else {
                rentContent.animateRentContent(500);
            }
        });
    }

    public void deregisterAnimationForTimeCounter(RentBoard existingRentRow) {
        getCurrentForm().deregisterAnimated(existingRentRow);
    }

    public void registerAnimationForTimeCounter(RentBoard rentBoard) {
        getCurrentForm().registerAnimated(rentBoard);
    }

    public void removeRentRow(RentBoard rentBoard) {
        callSerially(() -> {
            deregisterAnimationForTimeCounter(rentBoard);
            rentBoard.remove();

            if (rentContent.getRentRows() == 0) {
                hide();
            } else {
                rentContent.animateRentContent(500);
            }
        });
    }

    public void removeRentRow(String serialNumber) {
        Log.p("remove rent row" + serialNumber);
        RentBoard rentBoard = rentContent.findRentBoardByName(serialNumber);
        if (rentBoard != null) removeRentRow(rentBoard);
    }

    public void removeAllRentRows() {
        callSerially(() -> {
            hide();
            rentContent.removeAllRows();
        });
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
        panelHeader.setHidden(false);
        contentHolder.setY(getDisplayHeight() - minPanelHeight);
        animateUnlayoutAndWait(200, 255);
        topToolbarPanel.remove();
        contentHolder.remove();
        contentHolder.setUIID("BottomPanelUnfolded");
        add(SOUTH, contentHolder);
//        bottomPanel.setPreferredSize(new Dimension(getDisplayWidth(), minPanelHeight));
        screenBlocking.setVisible(false);
        animateLayoutAndWait(100);
        addSwipeListeners();
        refreshRentContent();
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
                if (contentHolder.getHeight() > minPanelHeight + 3) {
                    removeSwipeListeners();
                    panelHeader.setHidden(true);
                    contentHolder.remove();
                    rentContent.hideTitle();
                    screenBlocking.setVisible(true);
                    getCurrentForm().getToolbar().setHidden(true);
//                    mainContainer.revalidateWithAnimationSafety();
                    contentHolder.setUIID("BottomPanelFolded");
                    add(NORTH, topToolbarPanel);
                    add(CENTER, contentHolder);
                    animateLayoutAndWait(100);
                    addSwipeListeners();
                } else {
                    contentHolder.setPreferredSize(new Dimension(getDisplayWidth(), minPanelHeight));
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
        return getLayout().getComponentConstraint(contentHolder);
    }

    private synchronized void processDragEvent(ActionEvent e) {
        if (isDraggingBottomPanel) {
            e.consume();
            boolean isDruggingUp = SOUTH.equals(getBottomPanelPosition());
            if (isDruggingUp) {
                contentHolder.setPreferredSize(new Dimension(getDisplayWidth(), firstY - e.getY() + minPanelHeight));
                revalidateWithAnimationSafety();
            }
        } else {
            Component draggedCmp = attachedForm.getComponentAt(e.getX(), e.getY() + 100);
            if (draggedCmp != null && isDragEnable &&
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
