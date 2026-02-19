package com.arduino.connection;

import com.arduino.connection.serial.SerialConnection;
import com.arduino.connection.network.NetworkConnection;
import com.arduino.connection.bluetooth.BluetoothConnection;

import java.util.HashMap;
import java.util.Map;

/**
 * Менеджер для управления различными подключениями
 */
public class ConnectionManager {

    private Map<String, ArduinoConnection> connections;
    private ArduinoConnection currentConnection;

    public ConnectionManager() {
        this.connections = new HashMap<>();
    }

    /**
     * Создать подключение через Serial порт
     */
    public SerialConnection createSerialConnection(String portName, int baudRate) {
        String key = "SERIAL_" + portName;
        SerialConnection connection = new SerialConnection(portName, baudRate);
        connections.put(key, connection);
        return connection;
    }

    /**
     * Создать подключение через сеть
     */
    public NetworkConnection createNetworkConnection(String host, int port) {
        String key = "NETWORK_" + host + ":" + port;
        NetworkConnection connection = new NetworkConnection(host, port);
        connections.put(key, connection);
        return connection;
    }

    /**
     * Создать подключение через Bluetooth
     */
    public BluetoothConnection createBluetoothConnection(String deviceAddress) {
        String key = "BLUETOOTH_" + deviceAddress;
        BluetoothConnection connection = new BluetoothConnection(deviceAddress);
        connections.put(key, connection);
        return connection;
    }

    /**
     * Установить текущее подключение
     */
    public void setCurrentConnection(ArduinoConnection connection) {
        this.currentConnection = connection;
    }

    /**
     * Получить текущее подключение
     */
    public ArduinoConnection getCurrentConnection() {
        return currentConnection;
    }

    /**
     * Получить все подключения
     */
    public Map<String, ArduinoConnection> getAllConnections() {
        return new HashMap<>(connections);
    }

    /**
     * Закрыть все подключения
     */
    public void closeAllConnections() {
        for (ArduinoConnection connection : connections.values()) {
            if (connection.isConnected()) {
                connection.disconnect();
            }
        }
        connections.clear();
        currentConnection = null;
    }

    /**
     * Отправить данные через текущее подключение
     */
    public int sendData(String data) {
        if (currentConnection != null && currentConnection.isConnected()) {
            return currentConnection.sendData(data);
        }
        return -1;
    }

    /**
     * Получить данные через текущее подключение
     */
    public String receiveData() {
        if (currentConnection != null && currentConnection.isConnected()) {
            return currentConnection.receiveData();
        }
        return "";
    }
}