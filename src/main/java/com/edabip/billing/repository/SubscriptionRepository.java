package com.edabip.billing.repository;
import com.edabip.billing.entity.*; import com.edabip.billing.model.*; import org.springframework.stereotype.Repository; import java.math.BigDecimal; import java.util.*;
@Repository public class SubscriptionRepository {
 private final SubscriptionJpaRepository jpa;private final PlanJpaRepository plans; public SubscriptionRepository(SubscriptionJpaRepository jpa,PlanJpaRepository plans){this.jpa=jpa;this.plans=plans;}
 public Subscription create(CreateSubscriptionRequest r,BigDecimal amount){PlanEntity p=plans.findById(r.planId()).orElseThrow();return dto(jpa.save(new SubscriptionEntity(r.customerId(),p,r.billingCycle().name().toLowerCase(),r.currentPeriodStart(),r.currentPeriodEnd(),amount)));}
 public Optional<Subscription> findById(long id){return jpa.findDetailedById(id).map(SubscriptionRepository::dto);}
 public void changePlan(long sid,long pid,BigDecimal amount){SubscriptionEntity s=jpa.findById(sid).orElseThrow();s.setPlan(plans.findById(pid).orElseThrow());s.setAmountDue(amount);}
 public Optional<Subscription> findCurrentByCustomerId(Long id){return jpa.findCurrent(id,java.time.LocalDate.now()).stream().findFirst().map(SubscriptionRepository::dto);}
 public static Subscription dto(SubscriptionEntity s){return new Subscription(s.getId(),s.getCustomerId(),s.getPlan().getId(),s.getPlan().getName(),BillingCycle.valueOf(s.getBillingCycle().toUpperCase()),s.getCurrentPeriodStart(),s.getCurrentPeriodEnd(),s.getAmountDue(),s.getLastPaymentAt());}
}
