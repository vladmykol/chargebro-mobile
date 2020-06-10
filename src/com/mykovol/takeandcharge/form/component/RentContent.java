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
    private final Container rentInfo = new Container(BoxLayout.y());

    public RentContent() {
        super(BoxLayout.y());
        addAll(rentTitle,rentInfo);
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
        rentInfo.addComponent(0, rentBoard);
        return rentBoard;
    }

    public boolean noRentRows() {
        return rentInfo.getComponentCount() == 0;
    }

    public Map<String, RentBoard> getVisibleRentBoards() {
        Map<String, RentBoard> rentInfoMap = new HashMap<>();
        for (int i = 0; i < rentInfo.getComponentCount(); i++) {
            Component rentRow = rentInfo.getComponentAt(i);
            rentInfoMap.put(rentRow.getName(), (RentBoard) rentRow);
        }
        return rentInfoMap;
    }

    public RentBoard findRentBoardByName(String serialNumber) {
        for (int i = 0; i < rentInfo.getComponentCount(); i++) {
            if (serialNumber.equals(rentInfo.getComponentAt(i).getName()))
                return (RentBoard) rentInfo.getComponentAt(i);
        }
        return null;
    }

    public void animateRentContent(int duration) {
        rentInfo.animateLayoutAndWait(duration);
    }
}
