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
import com.codename1.ui.Stroke;
import com.codename1.ui.animations.Motion;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.plaf.RoundBorder;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.UITimer;

import static com.codename1.ui.CN.getCurrentForm;

public class InfinityProgressBlocking {
    private static Dialog dialog;

    public static void start() {
        if (dialog==null) {
            dialog = new InfiniteProgress().showInfiniteBlocking();
            dialog.revalidateWithAnimationSafety();
        } else {
            dialog.putClientProperty("isInfiniteProgress", true);
            dialog.showPacked(BorderLayout.CENTER, false);
        }
    }

    public static void stop() {
        if (dialog != null) {
            dialog.dispose();
        }
    }

    public static Dialog get() {
        return dialog;
    }
}
