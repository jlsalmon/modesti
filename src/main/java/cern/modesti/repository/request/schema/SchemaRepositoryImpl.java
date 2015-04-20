package cern.modesti.repository.request.schema;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;

//public class SchemaRepositoryImpl extends SimpleMongoRepository<Schema, String> {
//
//    public SchemaRepositoryImpl(MongoEntityInformation<Schema, String> metadata, MongoOperations mongoOperations) {
//    super(metadata, mongoOperations);
//    // TODO Auto-generated constructor stub
//  }
//
//    public <S extends Schema> S save(S entity) {
//      System.out.println("custom save");
//      return entity;
//    }
//    
//
//    
//    // actually want to override findOne();
//}
