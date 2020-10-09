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

package com.mykovol.takeandcharge.form;

import com.codename1.components.SpanLabel;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.animations.FlipTransition;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;

/**
 * @author Vlad Mykol
 */
public class NotImplementedScreen extends Form {

    public NotImplementedScreen(String title, Form previousForm) {
        super(new BorderLayout());

        setFormBottomPaddingEditingMode(true);
        setToolbar(new Toolbar(true));
        setTitle(title);
        setTransitionInAnimator(CommonTransitions.createEmpty());
        setTransitionOutAnimator(new FlipTransition(-1, 300));

        getToolbar().setBackCommand(constructBackCommand(previousForm), Toolbar.BackCommandPolicy.AS_ARROW, 4.5f);

        getContentPane().getAllStyles().setMarginUnit(Style.UNIT_TYPE_DIPS);
        getContentPane().getAllStyles().setMargin(0, 5, 3.5f, 3.5f);

        Label headerImage = new Label("", "RentConfirmationImage");
        headerImage.setMaterialIcon(FontImage.MATERIAL_EMOJI_OBJECTS);
        SpanLabel headerTextLabel = new SpanLabel("Coming soon...", "RentConfirmationHeader");
        headerTextLabel.setEnabled(false);

        SpanLabel headerSubText = new SpanLabel("This feature is not implemented yet. Check out new app version and get more features", "RentConfirmationHint");
        headerSubText.setEnabled(false);

        final Container centerHolder = BoxLayout.encloseYCenter(headerImage,
                headerTextLabel,
                headerSubText);
        centerHolder.setScrollableY(false);
        centerHolder.setTensileDragEnabled(false);

        add(BorderLayout.CENTER, centerHolder);

        Button okButton = new Button("Ok", "LoginButton");
        okButton.addActionListener(evt -> {
            previousForm.showBack();
        });

        add(BorderLayout.SOUTH, okButton);
    }

    private Command constructBackCommand(Form previousForm) {
        return new Command("") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                previousForm.showBack();
            }
        };
    }
}
