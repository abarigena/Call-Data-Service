package com.abarigena.calldataservice.store.repository;

import com.abarigena.calldataservice.store.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с абонентами.
 */
@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
}
