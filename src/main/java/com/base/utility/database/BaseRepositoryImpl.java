package com.base.utility.database;

import com.base.utility.security.CurrentUserUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
public class BaseRepositoryImpl <T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID>
        implements BaseRepositoryCustom<T, ID>{
    private final EntityManager entityManager;
    private final JpaEntityInformation<T, ID> entityInformation;

    // ✅ Inject additional dependencies using @Autowired (NOT constructor)
    @Autowired(required = false)
    private CurrentUserUtil currentUserUtil;

    // ✅ REQUIRED: Exact constructor signature for Spring Data JPA
    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation,
                              EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.entityInformation = (JpaEntityInformation<T, ID>) entityInformation;
    }

    @Override
    public List<T> findAllActive() {
        String query = String.format("SELECT e FROM %s e WHERE e.deleted = false",
                entityInformation.getEntityName());
        return entityManager.createQuery(query, getDomainClass()).getResultList();
    }

    @Override
    public List<T> findAllActive(Sort sort) {
        String orderBy = buildOrderByClause(sort);
        String query = String.format("SELECT e FROM %s e WHERE e.deleted = false %s",
                entityInformation.getEntityName(), orderBy);
        return entityManager.createQuery(query, getDomainClass()).getResultList();
    }

    @Override
    public Page<T> findAllActive(Pageable pageable) {
        String query = String.format("SELECT e FROM %s e WHERE e.deleted = false",
                entityInformation.getEntityName());
        String countQuery = String.format("SELECT COUNT(e) FROM %s e WHERE e.deleted = false",
                entityInformation.getEntityName());

        Query jpqlQuery = entityManager.createQuery(query, getDomainClass());
        jpqlQuery.setFirstResult((int) pageable.getOffset());
        jpqlQuery.setMaxResults(pageable.getPageSize());

        Query jpqlCountQuery = entityManager.createQuery(countQuery);
        long total = (Long) jpqlCountQuery.getSingleResult();

        List<T> content = jpqlQuery.getResultList();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<T> findByIdActive(ID id) {
        String query = String.format("SELECT e FROM %s e WHERE e.id = :id AND e.deleted = false",
                entityInformation.getEntityName());
        List<T> results = entityManager.createQuery(query, getDomainClass())
                .setParameter("id", id)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    @Transactional
    public void softDelete(ID id) {
        String currentUser = getCurrentUser();
        String query = String.format("UPDATE %s e SET e.deleted = true, e.deletedDate = :deletedDate, e.deletedBy = :deletedBy WHERE e.id = :id",
                entityInformation.getEntityName());
        entityManager.createQuery(query)
                .setParameter("id", id)
                .setParameter("deletedDate", LocalDateTime.now())
                .setParameter("deletedBy", currentUser)
                .executeUpdate();
    }

    @Override
    @Transactional
    public void softDelete(T entity) {
        ID id = entityInformation.getId(entity);
        softDelete(id);
    }

    @Override
    @Transactional
    public void restore(ID id) {
        String query = String.format("UPDATE %s e SET e.deleted = false, e.deletedDate = null, e.deletedBy = null WHERE e.id = :id",
                entityInformation.getEntityName());
        entityManager.createQuery(query)
                .setParameter("id", id)
                .executeUpdate();

        log.info("Entity restored: {} with id: {}", entityInformation.getEntityName(), id);
    }

    @Override
    public boolean existsByIdActive(ID id) {
        String query = String.format("SELECT COUNT(e) FROM %s e WHERE e.id = :id AND e.deleted = false",
                entityInformation.getEntityName());
        Long count = (Long) entityManager.createQuery(query)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public List<T> findAllActive(Specification<T> spec) {
        return findAll(spec); // Simplified - can be enhanced with Criteria API
    }

    @Override
    public Page<T> findAllActive(Specification<T> spec, Pageable pageable) {
        return findAll(spec, pageable); // Simplified
    }

    @Override
    public long countActive() {
        String query = String.format("SELECT COUNT(e) FROM %s e WHERE e.deleted = false",
                entityInformation.getEntityName());
        return (Long) entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countActive(Specification<T> spec) {
        return count(spec); // Simplified
    }

    @Override
    @Transactional
    public <S extends T> List<S> saveInBatch(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    @Transactional
    public void refresh(T entity) {
        entityManager.refresh(entity);
    }

    @Override
    @Transactional
    public T refreshAndReturn(T entity) {
        entityManager.refresh(entity);
        return entity;
    }

    @Override
    public List<T> findByCreatedDateBetween(LocalDateTime start, LocalDateTime end) {
        String query = String.format("SELECT e FROM %s e WHERE e.createdDate BETWEEN :start AND :end AND e.deleted = false",
                entityInformation.getEntityName());
        return entityManager.createQuery(query, getDomainClass())
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    @Override
    public List<T> findByLastModifiedDateAfter(LocalDateTime date) {
        String query = String.format("SELECT e FROM %s e WHERE e.lastModifiedDate > :date AND e.deleted = false",
                entityInformation.getEntityName());
        return entityManager.createQuery(query, getDomainClass())
                .setParameter("date", date)
                .getResultList();
    }

    // ✅ Helper methods
    private String buildOrderByClause(Sort sort) {
        if (sort.isUnsorted()) {
            return "";
        }

        StringBuilder orderBy = new StringBuilder(" ORDER BY ");
        sort.forEach(order -> {
            orderBy.append("e.").append(order.getProperty())
                    .append(" ").append(order.getDirection().name())
                    .append(", ");
        });

        return orderBy.substring(0, orderBy.length() - 2);
    }

    private String getCurrentUser() {
        try {
            return currentUserUtil != null ? currentUserUtil.getCurrentUsername() : "system";
        } catch (Exception e) {
            return "system";
        }
    }
}
