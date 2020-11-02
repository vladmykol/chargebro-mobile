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

import com.codename1.ui.Command;
import com.codename1.ui.Component;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.animations.Transition;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.plaf.UIManager;

import static com.codename1.ui.CN.getCurrentForm;

/**
 * Common code for construction and initialization of various classes e.g. the side menu logic etc.
 *
 * @author Vlad Mykol
 */
public class FormCommand {
    private static final float menuImageSize = Float.parseFloat(UIManager.getInstance().getThemeConstant("menuImageSize", "4.5"));

    public static Command getCloseCommand(Form destForm) {
        FontImage mat = FontImage.createMaterial(FontImage.MATERIAL_CLOSE, "", menuImageSize);
        return Command.create("", mat, e -> {

            Component currEditing = getCurrentForm().findCurrentlyEditingComponent();
            if (currEditing != null) {
                currEditing.stopEditing(() -> destForm.show());
            } else {
                destForm.show();
            }
        });
    }

    public static void setBackAction(Form destForm, Form current) {
        setAction(destForm, current, FontImage.MATERIAL_ARROW_BACK, CommonTransitions.SLIDE_HORIZONTAL);
    }


    public static void setCloseAction(Form destForm, Form current) {
        setAction(destForm, current, FontImage.MATERIAL_CLOSE, CommonTransitions.SLIDE_VERTICAL);
    }

    private static void setAction(Form destForm, Form current, char materialIcon, int animation) {
        Style s = UIManager.getInstance().getComponentStyle("TitleCommand");
        FontImage mat = FontImage.createMaterial(materialIcon, s, menuImageSize);
        final Command command = Command.create("", mat, e -> {

            Component currEditing = getCurrentForm().findCurrentlyEditingComponent();
            if (currEditing != null) {
                currEditing.stopEditing(() -> setOutAnimation(destForm, current, animation));
            } else {
                setOutAnimation(destForm, current, animation);
            }
        });
        current.getToolbar().addCommandToLeftBar(command);
    }

    private static void setOutAnimation(Form destForm, Form current, int animation) {
        final Transition curOutAnimation = current.getTransitionOutAnimator();
        final Transition destInAnimation = destForm.getTransitionInAnimator();
        current.setTransitionOutAnimator(CommonTransitions.createUncover(animation, false, 200));
        destForm.setTransitionInAnimator(CommonTransitions.createEmpty());
        destForm.show();
        current.setTransitionOutAnimator(curOutAnimation);
        destForm.setTransitionInAnimator(destInAnimation);
    }


}
