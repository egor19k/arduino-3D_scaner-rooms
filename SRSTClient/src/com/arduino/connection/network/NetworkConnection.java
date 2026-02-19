package com.arduino.connection.network;

import com.arduino.connection.ArduinoConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

/**
 * Подключение через сеть (Ethernet/WiFi)
 */
public class NetworkConnection implements ArduinoConnection {

    private String host;
    private int port;
    private Socket socket;
    private BufferedReader inputReader;
    private PrintWriter outputWriter;
    private boolean connected;
    private int connectTimeout = 5000; // 5 секунд
    private int readTimeout = 10000; // 10 секунд

    /**
     * Конструктор
     * @param host IP адрес или хостнейм
     * @param port порт
     */
    public NetworkConnection(String host, int port) {
        this.host = host;
        this.port = port;
        this.connected = false;
    }

    @Override
    public boolean connect() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), connectTimeout);
            socket.setSoTimeout(readTimeout);

            inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputWriter = new PrintWriter(socket.getOutputStream(), true);

            connected = true;
            System.out.println("Успешно подключено к " + host + ":" + port);
            return true;

        } catch (Exception e) {
            System.err.println("Ошибка подключения к " + host + ":" + port + ": " + e.getMessage());
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
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Отключено от " + host + ":" + port);
        } catch (Exception e) {
            System.err.println("Ошибка при отключении: " + e.getMessage());
        }
    }

    @Override
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed() && socket.isConnected();
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
            socket.getOutputStream().write(data);
            socket.getOutputStream().flush();
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
        } catch (SocketTimeoutException e) {
            System.err.println("Таймаут получения данных");
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
            return socket.getInputStream().read(buffer);
        } catch (SocketTimeoutException e) {
            return 0; // Таймаут не считается ошибкой
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

        try {
            int originalTimeout = socket.getSoTimeout();
            socket.setSoTimeout(timeoutMs);

            StringBuilder data = new StringBuilder();
            char[] charBuffer = new char[1024];
            int bytesRead;

            while (inputReader.ready() && (bytesRead = inputReader.read(charBuffer)) != -1) {
                data.append(charBuffer, 0, bytesRead);
            }

            socket.setSoTimeout(originalTimeout);
            return data.toString();

        } catch (Exception e) {
            System.err.println("Ошибка получения данных: " + e.getMessage());
            return "";
        }
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
        return ConnectionType.NETWORK;
    }

    // Геттеры и сеттеры
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}