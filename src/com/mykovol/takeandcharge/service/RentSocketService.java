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

package com.mykovol.takeandcharge.service;

import com.codename1.io.Log;
import com.codename1.io.websocket.WebSocket;
import com.codename1.util.EasyThread;
import com.mykovol.takeandcharge.form.MainForm;

import java.io.*;

import static com.codename1.ui.CN.callSerially;
import static com.mykovol.takeandcharge.service.GlobalConst.SERVER_SOCKET_URL;

/**
 * Connects to the server and updates every time we move using the websocket API
 *
 * @author Shai Almog
 */
public class RentSocketService extends WebSocket {
    private static final short MESSAGE_TYPE_AUTH = 1;
    private static final short MESSAGE_TYPE_UPDATE = 2;
    private static final short MESSAGE_CODE_OK = 200;
    private static final short MESSAGE_CODE_ACCEPTED = 202;
    private static final short MESSAGE_CODE_UNAUTHORIZED = 401;
    private static RentSocketService instance;
    private EasyThread et;
    private WebSocket webSocket;

    public RentSocketService() {
        super(GlobalConst.getServerUrl() + SERVER_SOCKET_URL);
        autoReconnect(10000);
        et = EasyThread.start("Websocket");
        connect();
    }

    public static RentSocketService get() {
        if (instance == null) {
            instance = new RentSocketService();
        }
        return instance;
    }

    @Override
    public void reconnect() {
        super.reconnect();
        autoReconnect(10000);
    }

    @Override
    protected void onOpen() {
        sendAuthInfo();
    }

    private void sendAuthInfo() {
        if (!et.isThisIt()) {
            et.run(this::sendAuthInfo);
            return;
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {
            dos.writeShort(MESSAGE_TYPE_AUTH);
            writeToken(dos);

            dos.flush();
            send(bos.toByteArray());
        } catch (IOException e) {
            Log.e(e);
        }
    }

    private void writeToken(DataOutputStream dos) throws IOException {
        String token = UserService.getToken();
        dos.writeShort(token.length());
        for (int iter = 0; iter < token.length(); iter++) {
            dos.writeByte((byte) token.charAt(iter));
        }
    }


    @Override
    protected void onClose(int statusCode, String reason) {
        if (statusCode == 101) {
            reconnect();
        }
    }

    @Override
    protected void onMessage(String string) {
    }

    @Override
    protected void onMessage(byte[] bytes) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            short messageType = dis.readShort();
            short messageCode = dis.readShort();
            String message = dis.readUTF();
            switch (messageType) {
                case MESSAGE_TYPE_AUTH:
                    authAction(messageCode, message);
                    break;
                case MESSAGE_TYPE_UPDATE:
                    returnPowerBankAction(message);
                    break;
                default:
                    System.out.println("not defined message type from webSocket server " + messageCode + " " + message);
            }

        } catch (IOException err) {
            // won't happen as this is in RAM
            Log.e(err);
        }
    }

    private void returnPowerBankAction(String serialNumber) {
        callSerially(() -> MainForm.get().removeRentRow(serialNumber));
    }

    private void authAction(short messageCode, String responseMessage) {
        if (messageCode != MESSAGE_CODE_OK) {
            System.out.println("websocket authentication issue - " + responseMessage);
            UserService.logout();
        } else {
            System.out.println("authenticated in websocket server " + responseMessage);
            callSerially(() ->  MainForm.get().refreshRentContent());
        }
    }

    @Override
    protected void onError(Exception e) {
        Log.e(e);
    }

}
