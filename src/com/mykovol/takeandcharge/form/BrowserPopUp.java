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

import com.codename1.components.FloatingActionButton;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Toolbar;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BorderLayout;

public class BrowserPopUp extends Form {
    public BrowserPopUp(Form previousForm, String title, String url) {
        new BrowserPopUp(previousForm, title, url, true);
    }

    public BrowserPopUp(Form previousForm, String title, String url, boolean isInAnimation) {
        super(new BorderLayout());
        if (title != null) {
            setToolbar(new Toolbar(false));
            FontImage mat = FontImage.createMaterial(FontImage.MATERIAL_CLOSE, "", 4.5f);
            getToolbar().addCommandToRightBar("", mat, e -> previousForm.show());
            getToolbar().setTitle(title);
        }

        if (isInAnimation) {
            setTransitionInAnimator(CommonTransitions.createCover(CommonTransitions.SLIDE_VERTICAL, false, 300));
        }
        setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, false, 300));


        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_DONE);
        fab.bindFabToContainer(this);
        fab.addActionListener(evt -> {
//            ToastBar.showMessage("Congrats! Now you can take a powerbank", FontImage.MATERIAL_INFO);
            previousForm.show();
        });

//        CommonCode.removeTransitionsTemporarily(previousForm);

        BrowserComponent browser = new BrowserComponent();
        browser.setURL(url);
        browser.addBrowserNavigationCallback(url1 -> {
            if (!url1.equals(url)) {
                previousForm.show();
                return false;
            } else
                return true;
        });


        add(BorderLayout.CENTER, browser);

    }

}
