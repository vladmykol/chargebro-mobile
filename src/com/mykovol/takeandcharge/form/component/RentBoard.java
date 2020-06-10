package com.mykovol.takeandcharge.form.component;

import com.codename1.ui.Container;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;

import static com.codename1.ui.util.Resources.getGlobalResources;
import static com.mykovol.takeandcharge.service.StyleConst.*;

public class RentBoard extends Container {
    private final TimeLabel rentTime;
    private long startTime;
    private long lastRenderedTime = 0;

    public RentBoard(String serialNumber, long elapsedTime) {
        super(BoxLayout.x());
        setUIID(RENT_BORDER);
        setName(serialNumber);
        startTime = System.currentTimeMillis() - elapsedTime;
        rentTime = new TimeLabel(RENT_BORDER_TEXT);
        Container timeContainer = BoxLayout.encloseY(new Label("Time", RENT_BORDER_SUB_HEADER),
                rentTime);
        Container serialNumberContainer = BoxLayout.encloseY(new Label("Serial number", RENT_BORDER_SUB_HEADER),
                new Label(serialNumber, RENT_BORDER_TEXT));
        Image rentImage = getGlobalResources().getImage("power-bank-photo.png");
        add(new Label(rentImage));
        add(FlowLayout.encloseMiddle(serialNumberContainer, timeContainer));
    }

    public void updateElapsedTime(long timeElapsed) {
        setStartTime(System.currentTimeMillis() - timeElapsed);
        revalidate();
    }

    @Override
    public boolean animate() {
        if (System.currentTimeMillis() > lastRenderedTime + 60000) {
            lastRenderedTime = System.currentTimeMillis();
            int min = (int) (System.currentTimeMillis() - startTime) / 1000 / 60;
            rentTime.setMin(min);
            System.out.println("animate -" + min);
            return true;
        }
        return false;
    }


    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }


    public static class TimeLabel extends Label {
        private final String MIN_STRING = " " + getUIManager().localize("min", "min");
        private final String HOUR_STRING = " " + getUIManager().localize("h", "h") + " ";


        public TimeLabel(String style) {
            super("", style);
//            setText(formatMin(0));
        }

        public void setMin(int min) {
            setText(formatMin(min));
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
