package com.edabip.billing.repository;
import com.edabip.billing.entity.PlanEntity; import com.edabip.billing.model.*; import org.springframework.data.domain.*; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public class PlanRepository {
 private final PlanJpaRepository jpa; public PlanRepository(PlanJpaRepository jpa){this.jpa=jpa;}
 public PageResult<Plan> findAll(int page,int size,String sortColumn,String direction,String label){String field=switch(sortColumn){case "user_limit"->"userLimit";case "storage_limit_gb"->"storageLimitGb";case "reports_per_month"->"reportsPerMonth";default->sortColumn;};Page<PlanEntity> p=jpa.findAll(PageRequest.of(page,size,Sort.by(Sort.Direction.fromString(direction),field).and(Sort.by("id"))));return new PageResult<>(p.getContent().stream().map(PlanRepository::dto).toList(),page,size,p.getTotalElements(),p.getTotalPages(),label);}
 public long countAll(){return jpa.count();} public Optional<Plan> findById(long id){return jpa.findById(id).map(PlanRepository::dto);}
 public Plan create(CreatePlanRequest r){return dto(jpa.save(new PlanEntity(r.name(),r.price(),r.userLimit(),r.storageLimitGb(),r.reportsPerMonth(),r.activeDashboardLimit(),r.dataProcessingLimitGb(),r.support())));}
 public void setActive(long id,boolean active){jpa.findById(id).ifPresent(p->p.setActive(active));}
 public static Plan dto(PlanEntity p){return new Plan(p.getId(),p.getName(),p.getPrice(),p.getUserLimit(),p.getStorageLimitGb(),p.getReportsPerMonth(),p.getActiveDashboardLimit(),p.getDataProcessingLimitGb(),p.getSupport(),p.isActive());}
}
