package cern.modesti.request.template;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Justin Lewis Salmon
 */
public interface TemplateRepository extends MongoRepository<Template, String> {
}
