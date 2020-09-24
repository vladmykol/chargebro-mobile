package com.mykovol.takeandcharge.form.component;

import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Label;
import com.codename1.ui.layouts.BoxLayout;

import java.util.HashMap;
import java.util.Map;

import static com.mykovol.takeandcharge.service.StyleConst.*;

public class RentContent extends Container {
    private final Label rentTitle = new Label("Your rental", RENT_BORDER_HEADER);
    private final Container rentDetails = new Container(BoxLayout.y());

    public RentContent() {
        super(BoxLayout.y());
        addAll(rentTitle, rentDetails);
    }

    public void hideTitle() {
        rentTitle.setHidden(true, true);
    }

    public void showTitle() {
        rentTitle.setHidden(false, true);
    }

    public String getTitleText() {
        return rentTitle.getText();
    }

    public RentBoard addRow(String serialNumber, long elapsedTime) {
        RentBoard rentBoard = new RentBoard(serialNumber, elapsedTime);
        rentDetails.addComponent(0, rentBoard);
        return rentBoard;
    }

    public int getRentRows() {
        return rentDetails.getComponentCount();
    }

    public void removeAllRows() {
        rentDetails.removeAll();
    }

    public Map<String, RentBoard> getShowedRents() {
        Map<String, RentBoard> rentInfoMap = new HashMap<>();
        for (int i = 0; i < rentDetails.getComponentCount(); i++) {
            Component rentRow = rentDetails.getComponentAt(i);
            rentInfoMap.put(rentRow.getName(), (RentBoard) rentRow);
        }
        return rentInfoMap;
    }

    public RentBoard findRentBoardByName(String serialNumber) {
        for (int i = 0; i < rentDetails.getComponentCount(); i++) {
            if (serialNumber.equals(rentDetails.getComponentAt(i).getName()))
                return (RentBoard) rentDetails.getComponentAt(i);
        }
        return null;
    }

    public void animateRentContent(int duration) {
        rentDetails.animateLayoutAndWait(duration);
    }
}
