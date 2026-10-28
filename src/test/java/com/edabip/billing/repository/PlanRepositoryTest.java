package com.edabip.billing.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.edabip.billing.entity.PlanEntity;

@ExtendWith(MockitoExtension.class)
class PlanRepositoryTest {
    @Mock PlanJpaRepository jpa;
    @InjectMocks PlanRepository repository;

    @Test
    void findAllMapsEntitiesAndUsesRequestedPagingAndStableIdSort() {
        PlanEntity entity = new PlanEntity("Basic", new BigDecimal("9.99"), 5, 10, 100, 3, 20, "Email");
        when(jpa.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity),
                PageRequest.of(0, 10), 1));

        var result = repository.findAll(0, 10, "price", "ASC", "price,asc");

        assertEquals(1, result.items().size());
        assertEquals("Basic", result.items().get(0).name());
        assertEquals(new BigDecimal("9.99"), result.items().get(0).price());
        assertEquals("price,asc", result.sort());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(jpa).findAll(pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(10, pageable.getValue().getPageSize());
        assertEquals(Sort.by(Sort.Direction.ASC, "price").and(Sort.by("id")), pageable.getValue().getSort());
    }
}
