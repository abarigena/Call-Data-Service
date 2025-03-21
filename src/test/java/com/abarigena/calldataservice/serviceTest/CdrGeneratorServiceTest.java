package com.abarigena.calldataservice.serviceTest;

import com.abarigena.calldataservice.service.CdrGeneratorService;
import com.abarigena.calldataservice.store.entity.CdrRecord;
import com.abarigena.calldataservice.store.entity.Subscriber;
import com.abarigena.calldataservice.store.repository.CdrRecordRepository;
import com.abarigena.calldataservice.store.repository.SubscriberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CdrGeneratorServiceTest {
    @Mock
    private CdrRecordRepository cdrRecordRepository;

    @Mock
    private SubscriberRepository subscriberRepository;

    @InjectMocks
    private CdrGeneratorService cdrGeneratorService;

    @Captor
    private ArgumentCaptor<List<CdrRecord>> cdrRecordsCaptor;

    private List<Subscriber> subscribers;

    @BeforeEach
    void setUp() {
        subscribers = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Subscriber subscriber = new Subscriber();
            subscriber.setMsisdn("7999" + String.format("%07d", i));
            subscribers.add(subscriber);
        }
    }

    @Test
    void clearCdrRecords_сallDeleteAll() {
        cdrGeneratorService.clearCdrRecords();

        verify(cdrRecordRepository, times(1)).deleteAll();
    }

    @Test
    void generateYearCdrRecords_throwExceptionWhenNoSubscribers() {
        when(subscriberRepository.findAll()).thenReturn(new ArrayList<>());

        Exception exception = assertThrows(IllegalStateException.class,
                () -> cdrGeneratorService.generateYearCdrRecords());
        assertEquals("Пользователи не найдены", exception.getMessage());
    }

    @Test
    void generateYearCdrRecords_generateAndSaveRecords() {
        when(subscriberRepository.findAll()).thenReturn(subscribers);
        when(cdrRecordRepository.saveAll(any())).thenReturn(new ArrayList<>());

        cdrGeneratorService.generateYearCdrRecords();

        verify(cdrRecordRepository, times(1)).deleteAll();

        verify(cdrRecordRepository, atLeastOnce()).saveAll(cdrRecordsCaptor.capture());

        List<List<CdrRecord>> allSavedRecords = cdrRecordsCaptor.getAllValues();
        assertFalse(allSavedRecords.isEmpty());

        for (List<CdrRecord> recordBatch : allSavedRecords) {
            for (CdrRecord record : recordBatch) {
                assertNotNull(record.getStartTime());
                assertNotNull(record.getEndTime());
                assertTrue(record.getEndTime().isAfter(record.getStartTime()));
                assertTrue(record.getCallType().equals("01") || record.getCallType().equals("02"));
                assertNotNull(record.getCallerNumber());
                assertNotNull(record.getReceiverNumber());
                assertNotEquals(record.getCallerNumber(), record.getReceiverNumber());
            }
        }
    }
}
