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

import com.codename1.components.ScaleImageLabel;
import com.codename1.gif.GifImage;
import com.codename1.ui.Display;
import com.codename1.ui.Image;
import com.codename1.ui.util.Resources;

import java.io.IOException;
import java.io.InputStream;

/**
 * Common code for construction and initialization of various classes e.g. the side menu logic etc.
 *
 * @author Shai Almog
 */
public class MainGifLoader extends ScaleImageLabel {
    private static MainGifLoader instance;

    private MainGifLoader(Image i) {
        super(i);
    }

    public static MainGifLoader get() {
        if (instance == null) {
            try {
                InputStream gifFile = Display.getInstance().getResourceAsStream(Resources.class, "/load3.gif");
                instance = new MainGifLoader(GifImage.decode(gifFile, 95172));
                instance.setUIID("LoadImage");
                instance.setVisible(false);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return instance;
    }

    public void start() {
        setVisible(true);
    }

    public void stop() {
        setVisible(false);
    }

}
