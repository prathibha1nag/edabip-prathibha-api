package com.edabip.billing.entity;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.LocalDate; import java.time.Instant;
@Entity @Table(name="subscriptions")
public class SubscriptionEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="customer_id",nullable=false) private Long customerId;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="plan_id",nullable=false) private PlanEntity plan;
 @Column(name="billing_cycle",nullable=false,length=20) private String billingCycle;
 @Column(name="current_period_start",nullable=false) private LocalDate currentPeriodStart;
 @Column(name="current_period_end",nullable=false) private LocalDate currentPeriodEnd;
 @Column(name="amount_due",nullable=false,precision=10,scale=2) private BigDecimal amountDue;
 @Column(name="last_payment_at") private Instant lastPaymentAt;
 protected SubscriptionEntity() {}
 public SubscriptionEntity(Long customerId,PlanEntity plan,String billingCycle,LocalDate start,LocalDate end,BigDecimal amount){this.customerId=customerId;this.plan=plan;this.billingCycle=billingCycle;this.currentPeriodStart=start;this.currentPeriodEnd=end;this.amountDue=amount;}
 public Long getId(){return id;} public Long getCustomerId(){return customerId;} public PlanEntity getPlan(){return plan;} public String getBillingCycle(){return billingCycle;} public LocalDate getCurrentPeriodStart(){return currentPeriodStart;} public LocalDate getCurrentPeriodEnd(){return currentPeriodEnd;} public BigDecimal getAmountDue(){return amountDue;} public Instant getLastPaymentAt(){return lastPaymentAt;} public void setPlan(PlanEntity p){plan=p;} public void setAmountDue(BigDecimal a){amountDue=a;}
}
