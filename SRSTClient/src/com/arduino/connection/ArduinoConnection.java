package com.arduino.connection;

/**
 * Базовый интерфейс для подключения к Arduino
 */
public interface ArduinoConnection {

    /**
     * Подключиться к устройству
     * @return true если подключение успешно
     */
    boolean connect();

    /**
     * Отключиться от устройства
     */
    void disconnect();

    /**
     * Проверить подключение
     * @return true если подключено
     */
    boolean isConnected();

    /**
     * Отправить строковые данные
     * @param data данные для отправки
     * @return количество отправленных байт или -1 при ошибке
     */
    int sendData(String data);

    /**
     * Отправить байтовые данные
     * @param data байтовые данные
     * @return количество отправленных байт или -1 при ошибке
     */
    int sendData(byte[] data);

    /**
     * Получить данные
     * @return полученная строка
     */
    String receiveData();

    /**
     * Получить данные в буфер
     * @param buffer буфер для данных
     * @return количество полученных байт или -1 при ошибке
     */
    int receiveData(byte[] buffer);

    /**
     * Получить данные с таймаутом
     * @param timeoutMs таймаут в миллисекундах
     * @return полученная строка
     */
    String receiveData(int timeoutMs);

    /**
     * Очистить буфер ввода
     */
    void clearInputBuffer();

    /**
     * Получить тип подключения
     * @return тип подключения
     */
    ConnectionType getConnectionType();

    /**
     * Типы подключений
     */
    enum ConnectionType {
        SERIAL,
        NETWORK,
        BLUETOOTH,
        USB
    }
}