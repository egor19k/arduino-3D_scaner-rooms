package com.arduino.connection.bluetooth;

import com.arduino.connection.ArduinoConnection;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Подключение через Bluetooth (требует BlueCove библиотеку)
 */
public class BluetoothConnection implements ArduinoConnection {

    private String deviceAddress;
    private StreamConnection streamConnection;
    private BufferedReader inputReader;
    private PrintWriter outputWriter;
    private boolean connected;
    private String connectionUrl;

    /**
     * Конструктор
     * @param deviceAddress MAC адрес Bluetooth устройства
     */
    public BluetoothConnection(String deviceAddress) {
        this.deviceAddress = deviceAddress;
        this.connectionUrl = "btspp://" + deviceAddress + ":1";
        this.connected = false;
    }

    /**
     * Поиск Bluetooth устройств
     * @return список найденных устройств
     */
    public static List<RemoteDevice> discoverDevices() {
        List<RemoteDevice> devices = new ArrayList<>();
        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            DiscoveryAgent discoveryAgent = localDevice.getDiscoveryAgent();

            final Object inquiryCompletedEvent = new Object();
            DiscoveryListener listener = new DiscoveryListener() {
                @Override
                public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                    devices.add(btDevice);
                }

                @Override
                public void inquiryCompleted(int discType) {
                    synchronized (inquiryCompletedEvent) {
                        inquiryCompletedEvent.notifyAll();
                    }
                }

                @Override
                public void serviceSearchCompleted(int transID, int respCode) {}

                @Override
                public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {}
            };

            synchronized (inquiryCompletedEvent) {
                boolean started = discoveryAgent.startInquiry(DiscoveryAgent.GIAC, listener);
                if (started) {
                    inquiryCompletedEvent.wait();
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка поиска Bluetooth устройств: " + e.getMessage());
        }
        return devices;
    }

    @Override
    public boolean connect() {
        try {
            streamConnection = (StreamConnection) Connector.open(connectionUrl);
            inputReader = new BufferedReader(new InputStreamReader(streamConnection.openInputStream()));
            outputWriter = new PrintWriter(streamConnection.openOutputStream(), true);

            connected = true;
            System.out.println("Успешно подключено к Bluetooth устройству " + deviceAddress);
            return true;

        } catch (Exception e) {
            System.err.println("Ошибка подключения к Bluetooth устройству: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    @Override
    public void disconnect() {
        connected = false;
        try {
            if (inputReader != null) {
                inputReader.close();
            }
            if (outputWriter != null) {
                outputWriter.close();
            }
            if (streamConnection != null) {
                streamConnection.close();
            }
            System.out.println("Отключено от Bluetooth устройства " + deviceAddress);
        } catch (Exception e) {
            System.err.println("Ошибка при отключении: " + e.getMessage());
        }
    }

    @Override
    public boolean isConnected() {
        return connected && streamConnection != null;
    }

    @Override
    public int sendData(String data) {
        if (!isConnected()) {
            System.err.println("Не подключено к устройству");
            return -1;
        }

        try {
            outputWriter.print(data);
            outputWriter.flush();
            return data.length();
        } catch (Exception e) {
            System.err.println("Ошибка отправки данных: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public int sendData(byte[] data) {
        if (!isConnected()) {
            System.err.println("Не подключено к устройству");
            return -1;
        }

        try {
            streamConnection.openOutputStream().write(data);
            streamConnection.openOutputStream().flush();
            return data.length;
        } catch (Exception e) {
            System.err.println("Ошибка отправки данных: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public String receiveData() {
        if (!isConnected()) {
            return "";
        }

        try {
            if (inputReader.ready()) {
                return inputReader.readLine();
            }
        } catch (Exception e) {
            System.err.println("Ошибка получения данных: " + e.getMessage());
        }
        return "";
    }

    @Override
    public int receiveData(byte[] buffer) {
        if (!isConnected()) {
            return -1;
        }

        try {
            return streamConnection.openInputStream().read(buffer);
        } catch (Exception e) {
            System.err.println("Ошибка получения данных: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public String receiveData(int timeoutMs) {
        if (!isConnected()) {
            return "";
        }

        long startTime = System.currentTimeMillis();
        StringBuilder data = new StringBuilder();

        try {
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                if (inputReader.ready()) {
                    int charRead = inputReader.read();
                    if (charRead == -1) {
                        break;
                    }
                    data.append((char) charRead);
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка получения данных: " + e.getMessage());
        }

        return data.toString();
    }

    @Override
    public void clearInputBuffer() {
        if (!isConnected()) {
            return;
        }

        try {
            while (inputReader.ready()) {
                inputReader.read();
            }
        } catch (Exception e) {
            System.err.println("Ошибка очистки буфера: " + e.getMessage());
        }
    }

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.BLUETOOTH;
    }

    // Геттеры и сеттеры
    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
        this.connectionUrl = "btspp://" + deviceAddress + ":1";
    }
}