package com.base.utility.database;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BaseRepositoryCustom <T, ID extends Serializable>{
    // Soft delete support methods
    List<T> findAllActive();
    List<T> findAllActive(Sort sort);
    Page<T> findAllActive(Pageable pageable);
    Optional<T> findByIdActive(ID id);
    void softDelete(ID id);
    void softDelete(T entity);
    void restore(ID id);
    boolean existsByIdActive(ID id);

    // Query methods with soft delete support
    List<T> findAllActive(Specification<T> spec);
    Page<T> findAllActive(Specification<T> spec, Pageable pageable);
    long countActive();
    long countActive(Specification<T> spec);

    // Batch operations
    <S extends T> List<S> saveInBatch(Iterable<S> entities);

    // Refresh functionality
    void refresh(T entity);
    T refreshAndReturn(T entity);

    // Date-based queries
    List<T> findByCreatedDateBetween(LocalDateTime start, LocalDateTime end);
    List<T> findByLastModifiedDateAfter(LocalDateTime date);
}
