package com.nexus.desktop.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic DAO interface for database operations
 * @param <T> Entity type
 * @param <ID> ID type
 */
public interface GenericDAO<T, ID> {
    
    /**
     * Save entity to database
     */
    void save(T entity) throws SQLException;
    
    /**
     * Update existing entity
     */
    void update(T entity) throws SQLException;
    
    /**
     * Delete entity by ID
     */
    void delete(ID id) throws SQLException;
    
    /**
     * Find entity by ID
     */
    T findById(ID id) throws SQLException;
    
    /**
     * Find all entities
     */
    List<T> findAll() throws SQLException;
    
    /**
     * Check if entity exists by ID
     */
    boolean exists(ID id) throws SQLException;
}