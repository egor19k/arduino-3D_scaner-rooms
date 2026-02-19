package com.arduino.connection.serial;

import com.arduino.connection.ArduinoConnection;
import com.fazecast.jSerialComm.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Подключение через последовательный порт (COM) с использованием jSerialComm
 */
public class SerialConnection implements ArduinoConnection {

    private String portName;
    private int baudRate;
    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean connected;

    /**
     * Конструктор
     * @param portName имя порта (COM1, COM3, /dev/ttyUSB0 и т.д.)
     * @param baudRate скорость передачи (9600, 115200 и т.д.)
     */
    public SerialConnection(String portName, int baudRate) {
        this.portName = portName;
        this.baudRate = baudRate;
        this.connected = false;
    }

    /**
     * Получить список доступных портов
     * @return список доступных портов
     */
    public static List<String> getAvailablePorts() {
        List<String> ports = new ArrayList<>();
        SerialPort[] commPorts = SerialPort.getCommPorts();
        for (SerialPort port : commPorts) {
            ports.add(port.getSystemPortName());
        }
        return ports;
    }

    /**
     * Получить подробную информацию о портах
     * @return список портов с информацией
     */
    public static List<SerialPort> getDetailedPorts() {
        List<SerialPort> ports = new ArrayList<>();
        SerialPort[] commPorts = SerialPort.getCommPorts();
        for (SerialPort port : commPorts) {
            ports.add(port);
        }
        return ports;
    }

    @Override
    public boolean connect() {
        try {
            // Найти порт
            SerialPort[] ports = SerialPort.getCommPorts();
            SerialPort selectedPort = null;

            for (SerialPort port : ports) {
                if (port.getSystemPortName().equals(portName)) {
                    selectedPort = port;
                    break;
                }
            }

            if (selectedPort == null) {
                System.err.println("Порт " + portName + " не найден");
                return false;
            }

            serialPort = selectedPort;

            // Настройка параметров порта
            serialPort.setBaudRate(baudRate);
            serialPort.setNumDataBits(8);
            serialPort.setNumStopBits(1);
            serialPort.setParity(SerialPort.NO_PARITY);

            // Открыть порт
            if (serialPort.openPort()) {
                // Установить таймауты
                serialPort.setComPortTimeouts(
                        SerialPort.TIMEOUT_READ_BLOCKING |
                                SerialPort.TIMEOUT_WRITE_BLOCKING,
                        1000, // read timeout
                        1000  // write timeout
                );

                inputStream = serialPort.getInputStream();
                outputStream = serialPort.getOutputStream();

                connected = true;
                System.out.println("Успешно подключено к " + portName +
                        " на скорости " + baudRate +
                        " (" + serialPort.getDescriptivePortName() + ")");
                return true;
            } else {
                System.err.println("Не удалось открыть порт " + portName);
                return false;
            }

        } catch (Exception e) {
            System.err.println("Ошибка подключения к " + portName + ": " + e.getMessage());
            disconnect();
            return false;
        }
    }

    @Override
    public void disconnect() {
        connected = false;
        try {
            if (serialPort != null && serialPort.isOpen()) {
                serialPort.closePort();
                System.out.println("Отключено от " + portName);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при отключении: " + e.getMessage());
        }
    }

    @Override
    public boolean isConnected() {
        return connected && serialPort != null && serialPort.isOpen();
    }

    @Override
    public int sendData(String data) {
        if (!isConnected()) {
            System.err.println("Не подключено к устройству");
            return -1;
        }

        try {
            byte[] bytes = data.getBytes();
            outputStream.write(bytes);
            outputStream.flush();
            return bytes.length; // Возвращаем длину данных, которые должны были быть отправлены
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
            outputStream.write(data);
            outputStream.flush();
            return data.length; // Возвращаем длину данных
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
            StringBuilder data = new StringBuilder();
            byte[] buffer = new byte[1024];

            while (serialPort.bytesAvailable() > 0) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                data.append(new String(buffer, 0, bytesRead));
            }
            return data.toString();
        } catch (Exception e) {
            System.err.println("Ошибка получения данных: " + e.getMessage());
            return "";
        }
    }

    @Override
    public int receiveData(byte[] buffer) {
        if (!isConnected()) {
            return -1;
        }

        try {
            int bytesAvailable = serialPort.bytesAvailable();
            if (bytesAvailable > 0) {
                int bytesToRead = Math.min(bytesAvailable, buffer.length);
                return inputStream.read(buffer, 0, bytesToRead);
            }
            return 0;
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
        byte[] buffer = new byte[1024];

        try {
            // Установить временный таймаут
            int originalReadTimeout = serialPort.getReadTimeout();
            serialPort.setComPortTimeouts(
                    SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                    timeoutMs,
                    0
            );

            while (System.currentTimeMillis() - startTime < timeoutMs) {
                if (serialPort.bytesAvailable() > 0) {
                    int bytesRead = inputStream.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    data.append(new String(buffer, 0, bytesRead));
                }

                // Если получен символ новой строки, прекращаем чтение
                if (data.length() > 0 && data.charAt(data.length() - 1) == '\n') {
                    break;
                }

                // Небольшая пауза для снижения нагрузки на CPU
                if (serialPort.bytesAvailable() == 0) {
                    Thread.sleep(10);
                }
            }

            // Восстановить оригинальный таймаут
            serialPort.setComPortTimeouts(
                    SerialPort.TIMEOUT_READ_BLOCKING,
                    originalReadTimeout,
                    0
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Чтение прервано");
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
            byte[] buffer = new byte[1024];
            while (serialPort.bytesAvailable() > 0) {
                inputStream.read(buffer);
            }
        } catch (Exception e) {
            System.err.println("Ошибка очистки буфера: " + e.getMessage());
        }
    }

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.SERIAL;
    }

    // Новые методы для jSerialComm
    public void addDataListener(SerialPortDataListener listener) {
        if (serialPort != null) {
            serialPort.addDataListener(listener);
        }
    }

    public void removeDataListener() {
        if (serialPort != null) {
            serialPort.removeDataListener();
        }
    }

    /**
     * Метод для чтения строки (до символа новой строки)
     * @return строка данных
     */
    public String readLine() {
        return readLine(1000);
    }

    /**
     * Метод для чтения строки с таймаутом
     * @param timeoutMs таймаут в миллисекундах
     * @return строка данных
     */
    public String readLine(int timeoutMs) {
        if (!isConnected()) {
            return "";
        }

        long startTime = System.currentTimeMillis();
        StringBuilder line = new StringBuilder();

        try {
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                if (serialPort.bytesAvailable() > 0) {
                    int byteRead = inputStream.read();
                    if (byteRead == -1) {
                        break;
                    }

                    char c = (char) byteRead;
                    line.append(c);

                    if (c == '\n') {
                        break;
                    }
                } else {
                    Thread.sleep(10);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка чтения строки: " + e.getMessage());
        }

        return line.toString().trim();
    }

    /**
     * Отправить данные и получить ответ
     * @param data данные для отправки
     * @param timeoutMs таймаут ожидания ответа
     * @return ответ от устройства
     */
    public String sendAndReceive(String data, int timeoutMs) {
        if (sendData(data) > 0) {
            return receiveData(timeoutMs);
        }
        return "";
    }

    // Геттеры
    public String getPortName() {
        return portName;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }
}