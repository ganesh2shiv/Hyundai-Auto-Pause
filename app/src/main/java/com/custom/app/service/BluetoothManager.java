package com.custom.app.service;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

public class BluetoothManager {

    private static BluetoothManager instance;
    private SendReceiveThread sendReceiveThread;

    public static synchronized BluetoothManager getInstance() {
        if (instance == null) {
            instance = new BluetoothManager();
        }
        return instance;
    }

    protected void start(BluetoothSocket socket, BluetoothService.onReceiveListener receiveListener) {
        if (sendReceiveThread == null) {
            sendReceiveThread = new SendReceiveThread(socket);
            setOnReceiveListener(receiveListener);
            sendReceiveThread.start();
        }
    }

    protected void stop() {
        if (sendReceiveThread != null) {
            sendReceiveThread.cancel();
            sendReceiveThread = null;
        }

        if (instance != null) {
            instance = null;
        }
    }

    /**
     * Set receive listener,you can read data with this listener.
     * @param receiveListener BluetoothListener.onReceivedListener
     */
    public void setOnReceiveListener(BluetoothService.onReceiveListener receiveListener) {
        if (sendReceiveThread != null) {
            sendReceiveThread.attachReceiveListener(receiveListener);
        }
    }

    public boolean send(String data) {
        if (sendReceiveThread != null) {
            return sendReceiveThread.write(data);
        }
        return false;
    }

    public boolean send(byte[] b) {
        if (sendReceiveThread != null) {
            return sendReceiveThread.write(b);
        }
        return false;
    }

    public boolean send(byte[] b, int offset, int length) {
        if (sendReceiveThread != null) {
            return sendReceiveThread.write(b, offset, length);
        }
        return false;
    }

    private static class SendReceiveThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private BluetoothService.onReceiveListener receiveListener = null;

        public SendReceiveThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Timber.e(e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];

            // Keep looping to listen received data
            while (true) {
                try {
                    int bytes = mmInStream.read(buffer);
//                  Timber.d("[RX] %s", new String(buffer, 0, bytes));
                    setReceivedListenerResult(buffer);
                } catch (IOException e) {
                    Timber.e(e);
                    break;
                }
            }
        }

        // write method string
        public boolean write(String input) {
            byte[] msgBuffer = input.getBytes();

            try {
                mmOutStream.write(msgBuffer);
                return true;
            } catch (IOException e) {
                Timber.e(e);
                return false;
            }
        }

        public boolean write(byte[] b) {
            try {
                mmOutStream.write(b);
                return true;
            } catch (IOException e) {
                Timber.e(e);
                return false;
            }
        }

        public boolean write(byte[] b, int offset, int length) {
            try {
                mmOutStream.write(b, offset, length);
                return true;
            } catch (IOException e) {
                Timber.e(e);
                return false;
            }
        }

        // cancel send receive process
        private void cancel() {
            dettachReceiveListener();

            if (mmInStream != null) {
                try {
                    mmInStream.close();
                } catch (IOException e) {
                    Timber.e(e);
                }
            }

            if (mmOutStream != null) {
                try {
                    mmOutStream.close();
                } catch (IOException e) {
                    Timber.e(e);
                }
            }
        }

        private void attachReceiveListener(BluetoothService.onReceiveListener receiveListener) {
            if (receiveListener != null) {
                this.receiveListener = receiveListener;
            }
        }

        private void dettachReceiveListener() {
            if (this.receiveListener != null) {
                this.receiveListener = null;
            }
        }

        private void setReceivedListenerResult(byte[] receivedData) {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (SendReceiveThread.this.receiveListener != null) {
                    SendReceiveThread.this.receiveListener.onReceived(receivedData);
                }
            });
        }
    }
}