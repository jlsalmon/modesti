package cern.modesti.elastic;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BuildingRepository extends ElasticsearchRepository<Building, String> {

  List<Building> findByNumber(String number);
}
