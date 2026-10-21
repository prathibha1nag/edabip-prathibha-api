package com.edabip.billing.repository;
import com.edabip.billing.entity.SubscriptionEntity; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.time.LocalDate; import java.util.*;
public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity,Long>{
 @Query("select s from SubscriptionEntity s join fetch s.plan where s.id=:id") Optional<SubscriptionEntity> findDetailedById(@Param("id") Long id);
 @Query("select s from SubscriptionEntity s join fetch s.plan where s.customerId=:customer and s.currentPeriodStart<=:today and s.currentPeriodEnd>:today order by s.currentPeriodStart desc,s.id desc") List<SubscriptionEntity> findCurrent(@Param("customer") Long customer,@Param("today") LocalDate today);
}
