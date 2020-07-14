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
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BorderLayout;
import com.mykovol.takeandcharge.tools.CommonCode;

/**
 * The Login form
 *
 * @author Shai Almog
 */
public class PaymentPopUp extends Form {
    public PaymentPopUp(Form previousForm, String title, String url) {
        super(new BorderLayout());
        setTransitionInAnimator(CommonTransitions.createCover(CommonTransitions.SLIDE_VERTICAL, false, 300));
//        MorphTransition morph = MorphTransition.create(400).
//                morph("LogoImageName");
//        setTransitionInAnimator(morph);
        setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, false, 300));
        FontImage mat = FontImage.createMaterial(FontImage.MATERIAL_CLOSE, "", 4.5f);
        getToolbar().addCommandToRightBar("", mat, e -> previousForm.show());
        getToolbar().setTitle(title);
//        getToolbar().setBackCommand("", Toolbar.BackCommandPolicy.AS_ARROW, e -> {
//            previous.show();
//        });123

        CommonCode.removeTransitionsTemporarily(previousForm);
        Display.getInstance().setProperty("BrowserComponent.useWKWebView", "true");
        BrowserComponent browser = new BrowserComponent();
        browser.setURL(url);

        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_ARROW_FORWARD);
        fab.bindFabToContainer(this);
        fab.addActionListener(evt -> previousForm.showBack());

        add(BorderLayout.CENTER, browser);
    }

}
