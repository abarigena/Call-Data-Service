package com.abarigena.calldataservice.serviceTest;

import com.abarigena.calldataservice.service.SubscriberService;
import com.abarigena.calldataservice.store.entity.Subscriber;
import com.abarigena.calldataservice.store.repository.SubscriberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriberServiceTest {

    @Mock
    private SubscriberRepository subscriberRepository;

    @InjectMocks
    private SubscriberService subscriberService;

    @Captor
    private ArgumentCaptor<List<Subscriber>> subscribersCaptor;

    @Test
    void initializeSubscribers_createSubscribersWhenEmpty() {
        when(subscriberRepository.count()).thenReturn(0L);

        subscriberService.initializeSubscribers();

        verify(subscriberRepository).saveAll(subscribersCaptor.capture());
        List<Subscriber> savedSubscribers = subscribersCaptor.getValue();

        assertEquals(10, savedSubscribers.size());
        for (int i = 0; i < 10; i++) {
            String expectedMsisdn = "7999" + String.format("%07d", i + 1);
            assertEquals(expectedMsisdn, savedSubscribers.get(i).getMsisdn());
        }
    }

    @Test
    void initializeSubscribers_notCreateSubscribersWhenNotEmpty() {
        when(subscriberRepository.count()).thenReturn(5L);

        subscriberService.initializeSubscribers();

        verify(subscriberRepository, never()).saveAll(any());
    }
}
