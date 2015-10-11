package cern.modesti.repository.base;

import java.io.Serializable;

import org.springframework.data.repository.Repository;

/**
 * @author Justin Lewis Salmon
 */
public interface ReadOnlyRepository<T, ID extends Serializable> extends Repository<T, ID> {

  T findOne(ID id);

  Iterable<T> findAll();
}