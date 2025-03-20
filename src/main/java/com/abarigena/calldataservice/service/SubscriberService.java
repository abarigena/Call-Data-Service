package com.abarigena.calldataservice.service;

import com.abarigena.calldataservice.store.entity.Subscriber;
import com.abarigena.calldataservice.store.repository.SubscriberRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для управления абонентами.
 * Отвечает за инициализацию и управление списком абонентов.
 */
@Service
public class SubscriberService {
    private static final Logger logger = LoggerFactory.getLogger(SubscriberService.class);

    private final SubscriberRepository subscriberRepository;

    @Autowired
    public SubscriberService(SubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    /**
     * Инициализация списка абонентов после создания экземпляра сервиса.
     * Проверяет наличие абонентов в базе данных и, если их нет,
     * создает 10 тестовых абонентов с номерами формата "7999XXXXXXX".
     */
    @PostConstruct
    public void initializeSubscribers() {
        logger.info("Проверка наличия абонентов в базе данных");
        if (subscriberRepository.count() == 0) {
            logger.info("Начата инициализация списка абонентов");
            List<Subscriber> subscribers = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                Subscriber subscriber = new Subscriber();
                subscriber.setMsisdn("7999" + String.format("%07d", i));
                subscribers.add(subscriber);
                logger.debug("Создан абонент с номером: {}", subscriber.getMsisdn());
            }
            subscriberRepository.saveAll(subscribers);
            logger.info("Завершена инициализация списка абонентов. Создано {} абонентов", subscribers.size());
        } else {
            logger.info("Абоненты уже существуют в базе данных. Инициализация не требуется");
        }
    }
}
