package com.mykovol.takeandcharge.form.component;

import com.codename1.charts.util.ColorUtil;
import com.codename1.components.SpanLabel;
import com.codename1.ui.*;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.RoundBorder;
import com.codename1.ui.plaf.Style;
import com.mykovol.takeandcharge.dataobj.RentHistory;

import static com.codename1.ui.CN.callSerially;
import static com.codename1.ui.util.Resources.getGlobalResources;
import static com.mykovol.takeandcharge.service.StyleConst.RENT_BORDER_SUB_HEADER;
import static com.mykovol.takeandcharge.service.StyleConst.RENT_BORDER_TEXT;

public class RentBoard extends Container {
    private final TimeLabel rentTime;
    private final Container errorMessageContainer;
    private final SpanLabel errorMessageText = new SpanLabel("", "RentBorderErrorText");
    private long startTime;
    private long lastRenderedTime = 0;
    private final SpanLabel errorMessageHeader = new SpanLabel("Error", "RentBorderErrorHeader");

    public RentBoard(RentHistory rentHistory) {
        super(BorderLayout.center());
        final String name = rentHistory.powerBankId.get();
        setName(name);
        rentTime = new TimeLabel(RENT_BORDER_TEXT);
        Container timeContainer = BoxLayout.encloseY(new Label("Time", RENT_BORDER_SUB_HEADER),
                rentTime);
        final String shortName = "STW-" + name.substring(name.length() - 4);
        Container serialNumberContainer = BoxLayout.encloseY(new Label("Serial number", RENT_BORDER_SUB_HEADER),
                new Label(shortName, RENT_BORDER_TEXT));
        Image rentImage = getGlobalResources().getImage("power-bank-photo.png");

        Container mainInfo = BoxLayout.encloseX(new Label(rentImage), BoxLayout.encloseXCenter(serialNumberContainer, timeContainer));

        Button button = new Button();
        FontImage.setIcon(button, FontImage.MATERIAL_ARROW_DROP_DOWN, 4);

        add(BorderLayout.NORTH, mainInfo);

        final Label panelDelimiterLabel = new Label("", "RentConfirmationDelimiter");
        panelDelimiterLabel.setShowEvenIfBlank(true);

        errorMessageText.setEnabled(false);
        errorMessageHeader.setEnabled(false);
        errorMessageContainer = BoxLayout.encloseY(panelDelimiterLabel, errorMessageHeader, errorMessageText);
        errorMessageContainer.getAllStyles().setMarginUnit(Style.UNIT_TYPE_DIPS);
        errorMessageContainer.getAllStyles().setMargin(0, 2, 2f, 2f);
        setUpdatableRentInfo(rentHistory);

        add(BorderLayout.SOUTH, errorMessageContainer);
    }

    public void updateExisting(RentHistory rentHistory) {
        callSerially(() -> {
            setUpdatableRentInfo(rentHistory);
        });
    }

    public void setUpdatableRentInfo(RentHistory rentHistory) {
        if (rentHistory.errorCode.get() > 0) {
            setUIID("RentBorderError");
            Stroke borderStroke = new Stroke(2, Stroke.CAP_SQUARE, Stroke.JOIN_MITER, 1);
            getAllStyles().setBorder(RoundBorder
                    .create()
                    .color(getAllStyles().getBgColor())
                    .strokeColor(ColorUtil.rgb(205,92,92))
                    .strokeOpacity(120)
                    .stroke(borderStroke)
                    .rectangle(true)
            );
            errorMessageText.setText(rentHistory.errorMessage.get());
            errorMessageHeader.setText(MessagePopUp.errorCodeToString(rentHistory.errorCode.getInt()));
            if (getParent() != null) {
                getParent().revalidate();
            }
        } else {
            errorMessageContainer.setHidden(true);
            setUIID("RentBorder");
            Stroke borderStroke = new Stroke(2, Stroke.CAP_SQUARE, Stroke.JOIN_MITER, 1);
            getAllStyles().setBorder(RoundBorder
                    .create()
                    .color(getAllStyles().getBgColor())
                    .strokeColor(ColorUtil.GRAY)
                    .strokeOpacity(120)
                    .stroke(borderStroke)
                    .rectangle(true)
            );

        }

        startTime = System.currentTimeMillis() - rentHistory.rentPeriodMs.getLong();
        updateTimer();
    }

    @Override
    public boolean animate() {
        if (System.currentTimeMillis() > lastRenderedTime + 60000) {
            updateTimer();
            return true;
        }
        return false;
    }

    private void updateTimer() {
        lastRenderedTime = System.currentTimeMillis();
        int min = (int) (System.currentTimeMillis() - startTime) / 1000 / 60;
        rentTime.setMin(min);
    }

    public static class TimeLabel extends Label {
        private final String MIN_STRING = " " + getUIManager().localize("min", "min");
        private final String HOUR_STRING = " " + getUIManager().localize("h", "h") + " ";


        public TimeLabel(String style) {
            super("0", style);
        }

        public void setMin(int min) {
            String formatMin = formatMin(min);
            if (!formatMin.equals(getText())) {
                setText(formatMin);
                getParent().revalidate();
            }
        }

        private String formatMin(int minutes) {
            if (minutes < 60) {
                return minutes + MIN_STRING;
            } else {
                return twoDigits(minutes / 60) + HOUR_STRING + twoDigits(minutes % 60) + MIN_STRING;
            }
        }

        private String twoDigits(int t) {
            if (t < 10) {
                return "0" + t;
            }
            return "" + t;
        }
    }
}
