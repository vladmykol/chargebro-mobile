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

import com.codename1.components.InfiniteProgress;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.Label;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.util.UITimer;

/**
 * Common code for construction and initialization of various classes e.g. the side menu logic etc.
 *
 * @author Shai Almog
 */
public class FullScreenLoader {
    private static Dialog dialog;
    private static final InfiniteProgress infiniteProgress = new InfiniteProgress();
    private static final Label loadingText = new Label("Sending a request...");
    private static UITimer delayedStarter;

    public static void delayedStart(int delayMills) {
        delayedStarter = UITimer.timer(delayMills, false, FullScreenLoader::start);
    }

    public static void start() {
        InfiniteProgress ip = new InfiniteProgress();
        dialog = ip.showInfiniteBlocking();
        dialog.addComponent(BorderLayout.SOUTH, new Label("Sending a request south...","SideCommand"));
        dialog.addComponent(BorderLayout.NORTH, new Label("Sending a request north...","SideCommand"));
        dialog.repaint();
    }

    public static void stop() {
        if (delayedStarter != null) delayedStarter.cancel();
        if (dialog != null) dialog.dispose();
    }
}
