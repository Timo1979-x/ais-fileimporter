package by.gto.xchanger.dao;

import by.gto.xchanger.model.EntityDescriptor;
import by.gto.xml.entities.DiagCard;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface XChangerDao {

    Map<Integer, Integer> getCodesToIdsMaping();

    int[] getChecksMapping();

    byte[] getMyGuid();

    /**
     * Возвращает номер последнего успешно принятого сообщения от участника обмена.
     *
     * @param peerId guid участника обмена
     * @return номер сообщения, или -2 если участник обмена помечен как неактивный (с такими не осуществляется обмен)
     *     или null, если информации о таком участнике обмена нет в БД (т.е. от него еще не приходило сообщений)
     */
    Integer getReceivedMessageNumber(byte[] peerId);

    int createPeer(byte[] peer, int msgFormatVersion);

    /**
     * @param toDel guid удаляемой записи.
     * @return количество удаленных записей
     */
    int deletePti(byte[] toDel, LocalDate minimumAllowedDate);

    //    /**
    //     *
    //     * @param dc - Вставляемая запись
    //     * @return количество вставленных записей
    //     */
    //    int insertPTI(DiagCard dc, XchangeOptions options);
    //
    //    /**
    //     *
    //     * @param dc - Обновляемая запись
    //     * @return количество обновленных записей
    //     */
    //    int updatePTI(DiagCard dc, XchangeOptions options);

    /**
     * @param dc - вставляемая/обновляемая запись
     * @return количество вставленных/обновленных записей
     */
    int insertOrUpdatePTI(DiagCard dc, boolean loadedByProtocol);

    int updateRegistry(byte[] peer, int successfullyReceivedMessageNumber);

    int updateMessageNumberInPeerTable(byte[] peer, int msgNumber, Long dsCode, int msgFormatVersion);

    /**
     * Найти пиры, у которых в таблице изменений есть неотправленные сообщения.
     * А также те,
     * @param date
     * @return
     */
    List<byte[]> findAdditionalPeers(Date date);

    /**
     * Получает номера сообщений из таблицы пиров.<br>
     * result[0] - номер последнего сообщения , посланного пиру<br>
     * result[1] - номер последнего сообщения , успешно принятого от пира<br>
     * result[2] - версия формата последнего сообщения , успешно принятого от пира<br>
     *
     * @param peerId уникальный id пира
     * @return массив номеров
     */
    List<Integer> getMessageNumbers(byte[] peerId);

    void updateLastSentMessage(byte[] peerId, int lastSentMessage);

    List<Integer> getDeletions(String entityName, byte[] peerId, Date exportDate);

    List<Map<String, Object>> getChanges(EntityDescriptor entityDescriptor, byte[] peerId, Date exportDate);

    /**
     * Проставляет в таблице изменений для каждого ранее не отправленного указанному пиру указанный номер сообщения.
     * @param senderId id пира
     * @param msgNumber номер сообщения, в котором было отправлено изменение
     * @param exportDate дата выгрузки (записи об изменениях с датой больше указанной не будут затронуты)
     */
    void updateChangesRegistry(byte[] senderId, int msgNumber, Date exportDate);

    void registerReferenceForPeer(byte[] peerId, EntityDescriptor entityDescriptor);

    /**
     * Получение версии структуры базы данных.
     *
     * @return версия
     */
    String getDBVersion();

    String test();

    /**
     * получить блокировку на базе данных. Если блокировка именованная, рекомендуется использовать в качестве ее имени полное имя класса
     *
     * @return 1 в случае успеха
     */
    Integer getLock();

    /**
     * Отпускает блокировку.  Если блокировка именованная, рекомендуется использовать в качестве ее имени полное имя класса.
     *
     * @return null в случае успеха, некий код ошибки при неудаче
     */
    Integer releaseLock();

    /**
     * Отключает пиры, которые давно ничего нам не присылали.
     *
     * @param days число дней. Пиры, от которых не было сообщений более days дней, станут неактивными
     * @return количество обновленных записей
     */
    int inactivateOldPeers(long days);

    /**
     * Получает соответствие между нашими идентификаторами моделей и оными из базы ГАИ.
     */
    Map<Integer, Integer> getMapOurModelIdToGaiModelId();

    Optional<Date> getMinimumAllowedDate();
}
