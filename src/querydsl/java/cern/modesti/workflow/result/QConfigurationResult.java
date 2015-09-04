package cern.modesti.workflow.result;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QConfigurationResult is a Querydsl query type for ConfigurationResult
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QConfigurationResult extends BeanPath<ConfigurationResult> {

    private static final long serialVersionUID = 1958051128L;

    public static final QConfigurationResult configurationResult = new QConfigurationResult("configurationResult");

    public final ListPath<String, StringPath> errors = this.<String, StringPath>createList("errors", String.class, StringPath.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath success = createBoolean("success");

    public QConfigurationResult(String variable) {
        super(ConfigurationResult.class, forVariable(variable));
    }

    public QConfigurationResult(Path<? extends ConfigurationResult> path) {
        super(path.getType(), path.getMetadata());
    }

    public QConfigurationResult(PathMetadata<?> metadata) {
        super(ConfigurationResult.class, metadata);
    }

}

