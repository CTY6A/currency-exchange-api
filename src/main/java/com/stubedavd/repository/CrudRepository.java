package com.stubedavd.repository;

import com.stubedavd.exception.AlreadyExistException;
import com.stubedavd.exception.DatabaseException;

import java.util.List;

public interface CrudRepository<T> {

    List<T> findAll() throws DatabaseException;

    T save(T entity) throws DatabaseException, AlreadyExistException;
}
