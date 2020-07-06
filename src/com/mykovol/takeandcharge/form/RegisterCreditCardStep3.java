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

import com.codename1.components.FloatingActionButton;
import com.codename1.components.SpanLabel;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.util.Resources;
import com.codename1.ui.util.UITimer;
import com.mykovol.takeandcharge.service.RegisterStyle;
import com.mykovol.takeandcharge.service.UserService;

import static com.codename1.ui.CN.getCurrentForm;

/**
 * Implements the SMS verification code logic
 *
 * @author Shai Almog
 */
public class RegisterCreditCardStep3 extends Form {
    public RegisterCreditCardStep3() {
        super(new BorderLayout());
        Form previous = getCurrentForm();
//        CommonCode.removeTransitionsTemporarily(previous);
        Command cmd = new Command("") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                previous.showBack();
            }
        };
        getToolbar().setBackCommand(cmd, Toolbar.BackCommandPolicy.AS_ARROW, 4.5f);
        getToolbar().setTitle("Step 3 from 3");

        Container box = new Container(BoxLayout.y());
        box.setScrollableY(true);

        Image LogoImage = Resources.getGlobalResources().getImage("register.png");
        box.add(BoxLayout.encloseXCenter(new Label(LogoImage)));

        box.add(new SpanLabel("Card number", RegisterStyle.TEXT_FIELD));
        TextField pass = new TextField("", "1234 1234 1234 1234", 40, TextField.PHONENUMBER);
        box.add(pass);

        add(CENTER, box);


        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_ARROW_FORWARD);
        fab.bindFabToContainer(this);

        fab.addActionListener(e -> {

//            new EditAccountForm().show();
        });
    }

}
