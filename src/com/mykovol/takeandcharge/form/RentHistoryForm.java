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
import com.codename1.l10n.SimpleDateFormat;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.util.Callback;
import com.mykovol.takeandcharge.dataobj.RentHistory;
import com.mykovol.takeandcharge.service.RentService;
import com.mykovol.takeandcharge.tools.FormCommand;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codename1.ui.CN.convertToPixels;


/**
 * Form to show user rent history
 *
 * @author Vlad Mykol
 */
public class RentHistoryForm extends Form {
    final SpanLabel notRentHistoryHint = new SpanLabel("You dont have rent history yet", "RentHistoryFormHint");
    private final Container cardContainer = new Container(BoxLayout.y());
    private final String DAYS_STRING = " " + getUIManager().localize("day", "day") + " ";
    private final String HOUR_STRING = " " + getUIManager().localize("h", "h") + " ";
    private final String MIN_STRING = " " + getUIManager().localize("min", "min") + " ";
    private final String SEC_STRING = " " + getUIManager().localize("sec", "sec") + " ";

    public RentHistoryForm() {
        super(BoxLayout.y());
        setToolbar(new Toolbar(false));
        setFormBottomPaddingEditingMode(true);
        setTransitionInAnimator(CommonTransitions.createEmpty());
        setTransitionOutAnimator(CommonTransitions.createEmpty());
        FormCommand.setCloseAction(MainForm.get(), this);

        notRentHistoryHint.setMaterialIcon(FontImage.MATERIAL_ERROR_OUTLINE);
        notRentHistoryHint.setEnabled(false);
        notRentHistoryHint.setHidden(true);
        cardContainer.setScrollableY(true);
        cardContainer.setScrollVisible(true);

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


        Label headerText = new Label("Rent History", "WalletFromHeader");
        addAll(
                headerText,
                spaceLabel,
                cardContainer,
                notRentHistoryHint
        );
        setScrollableY(false);

        addShowListener(evt -> {
            cardContainer.removeAll();
            RentService.getRentHistory(false, new Callback<List<com.mykovol.takeandcharge.dataobj.RentHistory>>() {
                @Override
                public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                    if (errorCode == 404) {
                        notRentHistoryHint.setHidden(false);
                        notRentHistoryHint.getParent().animateLayout(400);
                    } else {
                        ToastBar.showErrorMessage(errorMessage);
                    }
                }

                @Override
                public void onSucess(List<com.mykovol.takeandcharge.dataobj.RentHistory> rentHistoryList) {
                    for (RentHistory rentItem : rentHistoryList) {
                        if (rentItem.isReturned.get() > 0) {
                            cardContainer.add(new RentHistoryBoard(rentItem));
                        }
                    }
                    notRentHistoryHint.setHidden(true);
                    cardContainer.getParent().animateLayout(400);
                }
            });
        });
    }


    class RentHistoryBoard extends Container {
        private final Label costLabel = new Button("FREE", "RentHistoryFormPrice");
        private final Label timeLogo = new Label("", "RentHistoryFormTime");

        public RentHistoryBoard(RentHistory rentItem) {
            super(new BoxLayout(BoxLayout.Y_AXIS));
            setUIID("RentHistoryFormBoard");
            setName(rentItem.powerBankId.get());

            timeLogo.setIconUIID("RentHistoryFormTimeIcon");
            timeLogo.setGap(convertToPixels(1));
            timeLogo.setMaterialIcon(FontImage.MATERIAL_ACCESS_TIME);
            timeLogo.setText(getDurationBreakdown(rentItem.rentPeriodMs.getLong()));

            if (rentItem.rentPrice.getLong() > 0) {
                costLabel.setText(rentItem.rentPrice.getLong() / 100 + " ₴");
            } else {
                costLabel.getAllStyles().setFgColor(0XFF008000);
            }

            final Container headerContainer = BorderLayout.centerCenterEastWest(null, costLabel, timeLogo);

            add(headerContainer);
//            add(delimiter);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
            simpleDateFormat.applyPattern("dd.MM.yyyy - HH:mm");
            String dateStr = simpleDateFormat.format(new Date(rentItem.rentStartTime.getLong()));
            Label dateLabel = new Label(dateStr, "RentHistoryDate");
            add(dateLabel);
        }

        /**
         * Convert a millisecond duration to a string format
         *
         * @param millis A duration to convert to a string form
         * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
         */
        public String getDurationBreakdown(long millis) {
            if (millis < 0) {
                throw new IllegalArgumentException("Duration must be greater than zero!");
            }

            long days = TimeUnit.MILLISECONDS.toDays(millis);
            millis -= TimeUnit.DAYS.toMillis(days);
            long hours = TimeUnit.MILLISECONDS.toHours(millis);
            millis -= TimeUnit.HOURS.toMillis(hours);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
            millis -= TimeUnit.MINUTES.toMillis(minutes);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

            StringBuilder sb = new StringBuilder(64);
            if (days > 0) {
                sb.append(days);
                sb.append(DAYS_STRING);
            }
            if (hours > 0) {
                sb.append(hours);
                sb.append(HOUR_STRING);
            }
            if (minutes > 0) {
                sb.append(minutes);
                sb.append(MIN_STRING);
            }
            sb.append(seconds);
            sb.append(SEC_STRING);

            return (sb.toString());
        }
    }

}
