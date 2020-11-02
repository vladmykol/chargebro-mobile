/*
 * Copyright (c) 2016, Codename One
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.mykovol.takeandcharge.form;

import com.codename1.components.SpanLabel;
import com.codename1.components.ToastBar;
import com.codename1.io.Preferences;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.Resources;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.UserCardResponse;
import com.mykovol.takeandcharge.form.component.CustomDialog;
import com.mykovol.takeandcharge.service.RentService;
import com.mykovol.takeandcharge.service.UserService;
import com.mykovol.takeandcharge.tools.FormCommand;

import java.util.List;


/**
 * The Login form
 *
 * @author Vlad Mykol
 */
public class WalletForm extends Form {
    final SpanLabel noCardsHint = new SpanLabel("You must add payment card before taking a powerbank", "WalletFormHint");
    private final Container cardContainer = new Container(BoxLayout.y());

    public WalletForm() {
        super(BoxLayout.y());
        setToolbar(new Toolbar(false));
        setFormBottomPaddingEditingMode(true);
        setTransitionInAnimator(CommonTransitions.createEmpty());
        setTransitionOutAnimator(CommonTransitions.createEmpty());
        FormCommand.setCloseAction(MainForm.get(), this);

        noCardsHint.setMaterialIcon(FontImage.MATERIAL_ERROR_OUTLINE);
        noCardsHint.setEnabled(false);
        noCardsHint.setHidden(true);

//        CommonCode.removeTransitionsTemporarily(previous);
        // We remove the extra space for low resolution devices so things fit better
        Label spaceLabel = new Label(" ");
//        if (!Display.getInstance().isTablet() && Display.getInstance().getDeviceDensity() < Display.DENSITY_HD) {
//            spaceLabel = new Label();
//            setTitle(headerText.getText());
//            headerText.setHidden(true);
//            spaceLabel.setHidden(true);
//        }


        getContentPane().getAllStyles().setMarginUnit(Style.UNIT_TYPE_DIPS);
        getContentPane().getAllStyles().setMargin(0, 4, 3.5f, 3.5f);
//        Image LogoImage = Resources.getGlobalResources().getImage("main-logo.png");
//        Label logoImageHolder = new ScaleImageLabel(LogoImage);
//        logoImageHolder.setUIID("TextAlignCenter");
//        logoImageHolder.getAllStyles().setMarginTop(10);
//        logoImageHolder.setName("LogoImageName");
//        Container welcomeText = FlowLayout.encloseCenter(
//                loginHeader,
//                welcomeLabel2
//        );

        final Label delimiter = new Label("", "WalletFormDelimiter");
        delimiter.setShowEvenIfBlank(true);

        final Button addCardButton = new Button("Add Card", "WalletFromNewCardButton");
        addCardButton.setMaterialIcon(FontImage.MATERIAL_ADD);
        BrowserPopUp addCardForm = new BrowserPopUp("Card authorization");
        addCardForm.setBackAction(this);
        addCardForm.setTransitionInAnimator(CommonTransitions.createCover(CommonTransitions.SLIDE_HORIZONTAL, false, 300));
//        addCardForm.setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_HORIZONTAL, true, 300));
        addCardButton.addActionListener(evt -> {
            addCardForm.show();
            RentService.prepareCheckout(new Callback<String>() {
                @Override
                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                    MainForm.get().showErrorOnMainScreen(errorMessage, errorCode);
                }

                @Override
                public void onSucess(String checkoutUrl) {
                    addCardForm.setUrlForPayment(checkoutUrl);
                }
            });
        });

        Label headerText = new Label("Payment cards", "WalletFromHeader");
        addAll(
                headerText,
                spaceLabel,
                cardContainer,
                noCardsHint,
                delimiter,
                BoxLayout.encloseXCenter(addCardButton)
        );
        setScrollableY(false);

        addShowListener(evt -> {
            if (cardContainer.getComponentCount() == 0) {
                noCardsHint.setHidden(true);
                UserService.fetchUserCards(new Callback<List<UserCardResponse>>() {
                    @Override
                    public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                        if (errorCode == 404) {
                            noCardsHint.setHidden(false);
                            Preferences.set("noPaymentMethod", "true");
                            noCardsHint.getParent().animateLayout(400);
                        } else {
                            ToastBar.showErrorMessage(errorMessage);
                        }
                    }

                    @Override
                    public void onSucess(List<UserCardResponse> cardList) {
                        for (UserCardResponse cardDetails : cardList) {
                            cardContainer.add(new CardBoard(cardDetails));
                        }
                        getToolbar().addCommandToRightBar(getEditCommand());
                        Preferences.set("noPaymentMethod", "false");
                        cardContainer.getParent().animateLayout(400);
                    }
                });
            }
        });
    }


    public static boolean isUserHasCard() {
        return (Preferences.get("noPaymentMethod", "true")).equals("false");
    }

    public Command getEditCommand() {
        return new Command("Remove") {
            private volatile boolean isEdit;

            @Override
            public void actionPerformed(ActionEvent evt) {
                if (isEdit) {
                    setDoneRegime();
                    isEdit = false;
                } else {
                    setEditRegime();
                    isEdit = true;
                }
                revalidateWithAnimationSafety();
            }

            public void setDoneRegime() {
                for (int i = 0; i < cardContainer.getComponentCount(); i++) {
                    ((CardBoard) cardContainer.getComponentAt(i)).hideRemoveButton();
                }
            }

            public void setEditRegime() {
                for (int i = 0; i < cardContainer.getComponentCount(); i++) {
                    ((CardBoard) cardContainer.getComponentAt(i)).showRemoveButton();
                }
            }
        };
    }

    static class CardBoard extends Container {
        private final Button removeButton = new Button("", "WalletFormCardRemoveLabel");

        public CardBoard(UserCardResponse card) {
            super(new BorderLayout());
            setUIID("WalletFormCardBoard");
            setName(card.id.get());

            Label cardLogo;
            if ("VISA".equals(card.type.get())) {
                cardLogo = new Label(Resources.getGlobalResources().getImage("visa-logo.png"));
            } else {
                cardLogo = new Label(Resources.getGlobalResources().getImage("mastercard-logo.png"));
            }

            removeButton.setMaterialIcon(FontImage.MATERIAL_REMOVE_CIRCLE_OUTLINE);
            removeButton.setShowEvenIfBlank(true);
            removeButton.setHidden(true);

            add(BorderLayout.WEST, BoxLayout.encloseX(
                    cardLogo,
                    new Label(card.maskedNum.get())
                    )
            );
            add(BorderLayout.EAST, BorderLayout.centerAbsolute(removeButton));

            removeButton.addActionListener(evt -> {
                final CustomDialog customDialog = new CustomDialog("Are you sure you want to delete this card from your account?", "");
                customDialog.addYesCancelButtons(evt1 -> {
//                    UserService.removeUserCard(getName(), new FailureCallback<String>() {
//                        @Override
//                        public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
//                            MainForm.get().showErrorOnMainScreen(errorMessage, errorCode);
//                        }
//                    });
//
//                    setX(getDisplayWidth());
//                    cardContainer.animateUnlayout(400, 255, () -> {
//                        cardContainer.removeComponent(this);
//                        cardContainer.animateLayout(200);
//                    });
//                    if (cardContainer.getComponentCount() == 0) {
//                        noCardsHint.setHidden(false);
//                    }
                });
                customDialog.show();
            });

        }


        public void showRemoveButton() {
            removeButton.setHidden(false);
            removeButton.getParent().getParent().animateLayout(200);
        }

        public void hideRemoveButton() {
            removeButton.setHidden(true);
            removeButton.getParent().getParent().animateLayout(200);
        }
    }

}
