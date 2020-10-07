/*
 * Copyright (c) 2012, Codename One and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Codename One designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Codename One through http://www.codenameone.com/ if you
 * need additional information or have any questions.
 */

package com.mykovol.takeandcharge.tools;

import com.codename1.components.SpanLabel;
import com.codename1.gif.GifImage;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.Resources;
import com.codename1.ui.util.UITimer;
import com.mykovol.takeandcharge.form.MainForm;

import java.io.IOException;
import java.io.InputStream;

import static com.codename1.ui.CN.callSerially;

/**
 * Common code for construction and initialization of various classes e.g. the side menu logic etc.
 *
 * @author Vlad Mykol
 */
public class RentFullScreenLoader extends Form {
    private static RentFullScreenLoader instance;
    private static Label imageLabel;
    final SpanLabel loadText;
    private UITimer timer;
    private Form backRoundForm;

    private RentFullScreenLoader(Image i) {
        super(BoxLayout.yCenter());
        setToolbar(new Toolbar(true));
        setTransitionOutAnimator(CommonTransitions.createEmpty());
        setTransitionInAnimator(CommonTransitions.createEmpty());
        imageLabel = new Label(i);
        loadText = new SpanLabel(" ", "LoadImageText");
        loadText.setEnabled(true);
        add(BorderLayout.centerAbsolute(imageLabel));
        add(BorderLayout.centerAbsolute(loadText));
    }

    public static RentFullScreenLoader get() {
        if (instance == null) {
            try {
                InputStream gifFile = Display.getInstance().getResourceAsStream(Resources.class, "/load2.gif");
                instance = new RentFullScreenLoader(GifImage.decode(gifFile, 98306));
                instance.setUIID("LoadImage");
                instance.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return instance;
    }

    public void start() {
        if (timer != null) {
            timer.cancel();
        }
        loadText.setText("checking available powebanks");
        timer = UITimer.timer(4000, false, getComponentForm(), () -> {
            callSerially(() -> {
                loadText.setText("holding a deposit");
                loadText.getParent().revalidate();
            });
            timer = UITimer.timer(4000, false, getComponentForm(), () -> {
                callSerially(() -> {
                    loadText.setText("unlocking a powerbank");
                    loadText.getParent().revalidate();
                });
                timer = UITimer.timer(7000, false, getComponentForm(), () -> {
                    callSerially(() -> {
                        loadText.setText("resolving connection issue");
                        loadText.getParent().revalidate();
                    });
                    timer = UITimer.timer(10000, false, getComponentForm(), () -> {
                        callSerially(() -> {
                            loadText.setText("it takes longer than usual but we do our best ro resolve this request");
                            loadText.getParent().revalidate();

                        });
                        timer = UITimer.timer(40000, false, getComponentForm(), () -> {
                            callSerially(() -> {
                                MainForm.get().showIfNotVisible();
                                MainForm.get().refreshRentContent();
                            });
                        });
                    });
                });
            });
        });
        show();
    }

    public void setBackgroundForm(Form backRoundForm) {
        if (this.backRoundForm == null) {
            this.backRoundForm = backRoundForm;
            if (getBlurBackgroundRadius() > 0 && Display.getInstance().isGaussianBlurSupported()) {
                Image img = Image.createImage(backRoundForm.getWidth(), backRoundForm.getHeight());
                Graphics g = img.getGraphics();
                backRoundForm.paintComponent(g, true);
                img = Display.getInstance().gaussianBlurImage(img, getBlurBackgroundRadius());
                getUnselectedStyle().setBgImage(img);
                getUnselectedStyle().setBackgroundType(Style.BACKGROUND_IMAGE_SCALED_FILL);
            }
        }
    }

    public float getBlurBackgroundRadius() {
        return 17;
    }

}
