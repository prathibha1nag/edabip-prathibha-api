package com.edabip.billing.repository;
import com.edabip.billing.entity.PlanEntity; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.Optional;
public interface PlanJpaRepository extends JpaRepository<PlanEntity,Long>{
 Optional<PlanEntity> findByName(String name);
 @Query("select p from PlanEntity p left join fetch p.subscriptions where p.id=:id") Optional<PlanEntity> findWithSubscriptions(@Param("id") Long id);
}
