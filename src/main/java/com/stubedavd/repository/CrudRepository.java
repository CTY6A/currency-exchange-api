package com.stubedavd.repository;

import com.stubedavd.exception.AlreadyExistException;
import com.stubedavd.exception.InfrastructureException;

import java.util.List;

public interface CrudRepository<T> {

    List<T> findAll() throws InfrastructureException;

    T save(T entity) throws InfrastructureException, AlreadyExistException;
}
