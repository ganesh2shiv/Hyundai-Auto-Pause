package com.custom.app.service;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.UUID;

import timber.log.Timber;

@SuppressLint("MissingPermission")
public class BluetoothService {

    private final Context context;
    private ConnectThread connectThread = null;
    private onConnectionListener connectionListener = null;

    private long connectTimeout = 10 * 1000;

    private boolean isRegister = false;
    private boolean isConnected = false;
    private boolean isEnabledConnectTimeout = false;

    public static final int CONNECTING = 101;
    public static final int CONNECTED = 102;
    public static final int DISCONNECTED = 103;
    public static final int CONNECT_FAILED = 201;
    public static final int SOCKET_NOT_FOUND = 202;

    public BluetoothService(Context context) {
        this.context = context;
    }

    public void enableConnectTimeout() {
        isEnabledConnectTimeout = true;
    }

    public void disableConnectTimeout() {
        isEnabledConnectTimeout = false;
    }

    public boolean isEnabledConnectTimeout() {
        return isEnabledConnectTimeout;
    }

    /**
     * For bluetooth connection set connect timeout.
     * When connect timeout is over bluetooth connection gets disconnected and {@link #CONNECT_FAILED} gets transmitted.
     * <p>Default connect timeout is {@link #connectTimeout}.
     * @param timeoutMillis the connect timeout delay(in milliseconds)
     */
    public void setConnectTimeout(long timeoutMillis) {
        this.connectTimeout = timeoutMillis;
    }

    public long getConnectTimeout() {
        return this.connectTimeout;
    }

    /**
     *  connect method is used to connect bluetooth device using its address.
     *	<p>Note : Don't interrupt with connect method till it gives response to {@link #CONNECTED} or {@link #CONNECT_FAILED}
     *	or {@link #SOCKET_NOT_FOUND} .
     *	If you want to interrupt connect method you have the option to switch on connect timeout feature.
     *  @param deviceAddress - Bluetooth device mac address
     *  @param connectionListener - Connection listener, you can check all the bluetooth connection state with this listener
     *  @param receiveListener - Receive listener, you can read data with this listener
     *  @return true if connect method run successfully
     */
    public boolean connect(String deviceAddress, String uuid, onConnectionListener connectionListener,
                           onReceiveListener receiveListener) {
        boolean isSuccess = false;
        if (connectThread == null) {
            this.connectionListener = connectionListener;
            connectThread = new ConnectThread(deviceAddress, UUID.fromString(uuid), connectionListener, receiveListener);
            connectThread.start();
            isSuccess = true;
        }
        return isSuccess;
    }

    public void disconnect() {
        unregisterBroadcastReceiver();

        removeConnectionListener();

        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
    }

    public boolean isConnected() {
        if (connectThread != null) {
            return isConnected;
        }
        return false;
    }

    /**
     * Sends data in String format message to connected device.
     * @param data String to be send
     * @return true if data send successfully
     */
    public boolean send(String data) {
        return BluetoothManager.getInstance().send(data);
    }

    /**
     * Sends byte array to connected device.
     * @param b byte array to be send
     * @return true if data send successfully
     */
    public boolean send(byte[] b) {
        return BluetoothManager.getInstance().send(b);
    }

    /**
     * Sends byte array , int offset and int length to connected device.
     * @param b byte array to be send
     * @param off int offset to be send
     * @param len int length to be send
     * @return true if data send successfully
     */
    public boolean send(byte[] b, int off, int len) {
        return BluetoothManager.getInstance().send(b, off, len);
    }

    /**
     * Set receive listener,you can read data with this listener.
     * @param receiveListener BluetoothListener.onReceivedListener
     */
    public void setOnReceiveListener(onReceiveListener receiveListener) {
        BluetoothManager.getInstance().setOnReceiveListener(receiveListener);
    }

    // register broadcast receiver for bluetooth disconnected
    private void registerBroadcastReceiver() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (context != null && !isRegister) {
                IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                context.registerReceiver(myReceiver, intentFilter);
                isRegister = true;
            }
        });
    }

    private void unregisterBroadcastReceiver() {
        if (context != null && isRegister) {
            context.unregisterReceiver(myReceiver);
            isRegister = false;
        }
    }

    // Broadcast Receiver class is used for detect bluetooth device disconnected
    private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                if (connectionListener != null) {
                    connectionListener.onConnectionStateChanged(DISCONNECTED);
                }
            }
        }
    };

    private void removeConnectionListener() {
        if (connectionListener != null) {
            connectionListener = null;
        }
    }

    // Send data to connection state changed listener
    private void setConnectionStateChangedListenerResult(onConnectionListener connectionListener, int state) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (connectionListener != null) {
                connectionListener.onConnectionStateChanged(state);
            }
        });
    }

    // Send data to connection failed listener
    private void setConnectionFailedListenerResult(onConnectionListener connectionListener, int errorCode) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (connectionListener != null) {
                connectionListener.onConnectionFailed(errorCode);
            }
        });
    }

    private class ConnectThread extends Thread {

        private BluetoothSocket mSocket;
        private final BluetoothAdapter btAdapter;
        private onReceiveListener receiveListener;
        private onConnectionListener connectionListener;
        private final Handler timeoutHandler = new Handler(Looper.getMainLooper()); // connection timeout handler

        public ConnectThread(String deviceAddress, UUID uuid, onConnectionListener connectionListener,
                             onReceiveListener receiveListener) {
            this.connectionListener = connectionListener; // initialize bluetooth connection listener
            this.receiveListener = receiveListener; // initialize bluetooth received listener
            btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth default Adapter
            mSocket = createBluetoothSocket(deviceAddress, uuid); // create bluetooth socket
        }

        private BluetoothDevice getRemoteDevice(String deviceAddress) {
            try {
                return btAdapter.getRemoteDevice(deviceAddress);
            } catch (Exception e) {
                Timber.e(e);
                return null;
            }
        }

        private BluetoothSocket createBluetoothSocket(String deviceAddress, UUID uuid) {
            BluetoothSocket socket = null;
            try {
                if (btAdapter != null) {
                    BluetoothDevice device = getRemoteDevice(deviceAddress);
                    if (device != null) {
                        socket = device.createRfcommSocketToServiceRecord(uuid);
                    }
                }
            } catch (Exception e) {
                Timber.e(e);
            }
            return socket;
        }

        @Override
        public void run() {
            if (mSocket != null) {
                try {
                    setConnectionStateChangedListenerResult(this.connectionListener, CONNECTING);
                    // Cancel discovery because it otherwise slows down the connection.
                    btAdapter.cancelDiscovery();

                    addConnectionTimeout();

                    // Connect to the remote device through the socket.
                    // This call blocks until it succeeds or throws an exception.
                    mSocket.connect();

                    removeConnectionTimeout();

                    isConnected = true;

                    BluetoothManager.getInstance().start(mSocket, this.receiveListener);
                    registerBroadcastReceiver();
                    setConnectionStateChangedListenerResult(this.connectionListener, CONNECTED);
                } catch (Exception e) {
                    Timber.e(e);
                    closeSocket();
                    setConnectionFailedListenerResult(this.connectionListener, CONNECT_FAILED);
                    isConnected = false;
                    removeConnectionTimeout();
                }
            } else {
                setConnectionFailedListenerResult(this.connectionListener, SOCKET_NOT_FOUND);
            }
        }

        private void closeSocket() {
            try {
                if (mSocket != null) {
                    mSocket.close();
                    mSocket = null;
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        private void removeThreadListener() {
            if (this.connectionListener != null) {
                this.connectionListener = null;
            }

            if (this.receiveListener != null) {
                this.receiveListener = null;
            }
        }

        private void cancel() {
            removeConnectionTimeout();
            removeThreadListener();
            BluetoothManager.getInstance().stop();
            closeSocket();
            isConnected = false;
        }

        private void addConnectionTimeout() {
            if (isEnabledConnectTimeout) {
                timeoutHandler.postDelayed(timeoutRunnable, connectTimeout);
            }
        }

        private void removeConnectionTimeout() {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }

        private final Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (mSocket != null && !mSocket.isConnected()) {
                    try {
                        mSocket.close();
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                }
            }
        };
    }

    // You can check all the bluetooth connection state with this listener.
    public interface onConnectionListener {
        void onConnectionStateChanged(int state);
        void onConnectionFailed(int errorCode);
    }

    // You can read data with this listener.
    public interface onReceiveListener {
        void onReceived(byte[] receivedData);
    }
}