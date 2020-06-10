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

    public RentBoard(String serialNumber, long elapsedTime) {
        super(BoxLayout.x());
        setUIID(RENT_BORDER);
        setName(serialNumber);
        rentTime = new TimeLabel(elapsedTime, RENT_BORDER_TEXT);
        Container timeContainer = BoxLayout.encloseY(new Label("Time", RENT_BORDER_SUB_HEADER),
                rentTime);
        Container serialNumberContainer = BoxLayout.encloseY(new Label("Serial number", RENT_BORDER_SUB_HEADER),
                new Label(serialNumber, RENT_BORDER_TEXT));
        Image rentImage = getGlobalResources().getImage("power-bank-photo.png");
        add(new Label(rentImage));
        add(FlowLayout.encloseMiddle(serialNumberContainer, timeContainer));
    }

    public void updateElapsedTime(long timeElapsed) {
        rentTime.setStartTime(System.currentTimeMillis() - timeElapsed);
        animateLayout(200);
    }

    public static class TimeLabel extends Label {
        private final String MIN_STRING = " " + getUIManager().localize("min", "min");
        private final String HOUR_STRING = " " + getUIManager().localize("h", "h") + " ";
        private long startTime;
        private long lastRenderedTime;

        public TimeLabel(long elapsedTime, String style) {
            super("", style);
//            setText(formatMin(0));
            startTime = System.currentTimeMillis() - elapsedTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        @Override
        public boolean animate() {
            if (System.currentTimeMillis() / 1000 / 60 != lastRenderedTime / 1000 / 60) {
                lastRenderedTime = System.currentTimeMillis();
                int min = (int) (System.currentTimeMillis() - startTime) / 1000 / 60;
                setText(formatMin(min));
                return true;
            }
            return false;
        }

        @Override
        protected void initComponent() {
            this.getComponentForm().registerAnimated(this);
        }

        @Override
        protected void deinitialize() {
            this.getComponentForm().deregisterAnimated(this);
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
