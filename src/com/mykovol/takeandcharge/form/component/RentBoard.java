package com.mykovol.takeandcharge.form.component;

import com.codename1.ui.*;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;

import static com.codename1.ui.CN.callSerially;
import static com.codename1.ui.util.Resources.getGlobalResources;
import static com.mykovol.takeandcharge.service.StyleConst.*;

public class RentBoard extends Container {
    private final TimeLabel rentTime;
    private long startTime;
    private long lastRenderedTime = 0;

    public RentBoard(String name, long elapsedTime) {
        super(BorderLayout.center());
        setUIID(RENT_BORDER);
        setName(name);
        startTime = System.currentTimeMillis() - elapsedTime;
        rentTime = new TimeLabel(RENT_BORDER_TEXT);
        Container timeContainer = BoxLayout.encloseY(new Label("Time", RENT_BORDER_SUB_HEADER),
                rentTime);
        updateTimer();
        final String shortName = "STW-" + name.substring(name.length() - 4);
        Container serialNumberContainer = BoxLayout.encloseY(new Label("Serial number", RENT_BORDER_SUB_HEADER),
                new Label(shortName, RENT_BORDER_TEXT));
        Image rentImage = getGlobalResources().getImage("power-bank-photo.png");

        Container mainInfo = BoxLayout.encloseX(new Label(rentImage), BoxLayout.encloseXCenter(serialNumberContainer, timeContainer));

        Button button = new Button();
        FontImage.setIcon(button, FontImage.MATERIAL_ARROW_DROP_DOWN, 4);

        add(BorderLayout.NORTH, mainInfo);
    }

    public void updateExisting(long timeElapsed) {
        callSerially(() -> {
            startTime = System.currentTimeMillis() - timeElapsed;
            updateTimer();
        });
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
