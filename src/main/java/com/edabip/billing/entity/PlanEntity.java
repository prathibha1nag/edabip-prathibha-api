package com.edabip.billing.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="plans")
public class PlanEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=50) private String name;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal price;
 @Column(name="user_limit",nullable=false) private Integer userLimit;
 @Column(name="storage_limit_gb",nullable=false) private Integer storageLimitGb;
 @Column(name="reports_per_month",nullable=false) private Integer reportsPerMonth;
 @Column(name="active_dashboard_limit",nullable=false) private Integer activeDashboardLimit;
 @Column(name="data_processing_limit_gb",nullable=false) private Integer dataProcessingLimitGb;
 @Column(nullable=false,length=100) private String support;
 @Column(nullable=false) private boolean active=true;
 @OneToMany(mappedBy="plan") private List<SubscriptionEntity> subscriptions=new ArrayList<>();
 protected PlanEntity() {}
 public PlanEntity(String name,BigDecimal price,Integer userLimit,Integer storageLimitGb,Integer reportsPerMonth,Integer activeDashboardLimit,Integer dataProcessingLimitGb,String support){this.name=name;this.price=price;this.userLimit=userLimit;this.storageLimitGb=storageLimitGb;this.reportsPerMonth=reportsPerMonth;this.activeDashboardLimit=activeDashboardLimit;this.dataProcessingLimitGb=dataProcessingLimitGb;this.support=support;}
 public Long getId(){return id;} public String getName(){return name;} public BigDecimal getPrice(){return price;} public Integer getUserLimit(){return userLimit;} public Integer getStorageLimitGb(){return storageLimitGb;} public Integer getReportsPerMonth(){return reportsPerMonth;} public Integer getActiveDashboardLimit(){return activeDashboardLimit;} public Integer getDataProcessingLimitGb(){return dataProcessingLimitGb;} public String getSupport(){return support;} public boolean isActive(){return active;} public void setActive(boolean active){this.active=active;}
}
