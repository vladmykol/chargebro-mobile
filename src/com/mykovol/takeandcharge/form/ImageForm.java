package com.mykovol.takeandcharge.form;

import com.codename1.components.ImageViewer;
import com.codename1.io.Storage;
import com.codename1.io.Util;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.events.DataChangedListener;
import com.codename1.ui.events.SelectionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.list.ListModel;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.EventDispatcher;

import java.io.IOException;

/**
 *
 *
 * @author Vlad Mykol
 */
public class ImageForm extends Form {
    private final EncodedImage placeholder = EncodedImage.createFromImage(
            FontImage.createMaterial(FontImage.MATERIAL_SYNC, new Style()).
                    scaled(300, 300), false);

    public ImageForm() {
        super(new BorderLayout());

        ImageList imodel = new ImageList();

        ImageViewer iv = new ImageViewer(imodel.getItemAt(0));
        iv.setImageList(imodel);
        add(BorderLayout.CENTER, iv);
    }

    class ImageList implements ListModel<Image> {
        private int selection;
        private String[] imageURLs = {
                "http://awoiaf.westeros.org/images/thumb/9/93/AGameOfThrones.jpg/300px-AGameOfThrones.jpg",
                "http://awoiaf.westeros.org/images/thumb/3/39/AClashOfKings.jpg/300px-AClashOfKings.jpg",
                "http://awoiaf.westeros.org/images/thumb/2/24/AStormOfSwords.jpg/300px-AStormOfSwords.jpg",
                "http://awoiaf.westeros.org/images/thumb/a/a3/AFeastForCrows.jpg/300px-AFeastForCrows.jpg",
                "http://awoiaf.westeros.org/images/7/79/ADanceWithDragons.jpg"
        };
        private Image[] images;
        private EventDispatcher listeners = new EventDispatcher();

        public ImageList() {
            this.images = new EncodedImage[imageURLs.length];
        }

        public Image getItemAt(final int index) {
            if(images[index] == null) {
                images[index] = placeholder;
                Util.downloadUrlToStorageInBackground(imageURLs[index], "list" + index, (e) -> {
                    try {
                        images[index] = EncodedImage.create(Storage.getInstance().createInputStream("list" + index));
                        listeners.fireDataChangeEvent(index, DataChangedListener.CHANGED);
                    } catch(IOException err) {
                        err.printStackTrace();
                    }
                });
            }
            return images[index];
        }

        public int getSize() {
            return imageURLs.length;
        }

        public int getSelectedIndex() {
            return selection;
        }

        public void setSelectedIndex(int index) {
            selection = index;
        }

        public void addDataChangedListener(DataChangedListener l) {
            listeners.addListener(l);
        }

        public void removeDataChangedListener(DataChangedListener l) {
            listeners.removeListener(l);
        }

        public void addSelectionListener(SelectionListener l) {
        }

        public void removeSelectionListener(SelectionListener l) {
        }

        public void addItem(Image item) {
        }

        public void removeItem(int index) {
        }
    };
}
