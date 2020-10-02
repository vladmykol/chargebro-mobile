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

package com.mykovol.takeandcharge.dataobj;

import com.codename1.properties.IntProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;

/**
 * Property object representing a user
 *
 * @author Shai Almog
 */
public class BeforeRentInfo implements PropertyBusinessObject {
    public final Property<String, BeforeRentInfo> stationId = new Property<>("stationId");
    public final IntProperty<BeforeRentInfo> holdAmount = new IntProperty<>("holdAmount");
    public final IntProperty<BeforeRentInfo> bonusAmount = new IntProperty<>("bonusAmount");
    public final IntProperty<BeforeRentInfo> powerLevel = new IntProperty<>("powerLevel");

    private final PropertyIndex idx = new PropertyIndex(this, "BeforeRentInfo", stationId, holdAmount, bonusAmount, powerLevel);

    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }
}
