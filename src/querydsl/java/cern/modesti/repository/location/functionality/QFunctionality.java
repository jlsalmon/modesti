package cern.modesti.repository.location.functionality;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QFunctionality is a Querydsl query type for Functionality
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QFunctionality extends EntityPathBase<Functionality> {

    private static final long serialVersionUID = -1651432934L;

    public static final QFunctionality functionality = new QFunctionality("functionality");

    public final StringPath generalFunctionality = createString("generalFunctionality");

    public final StringPath value = createString("value");

    public QFunctionality(String variable) {
        super(Functionality.class, forVariable(variable));
    }

    public QFunctionality(Path<? extends Functionality> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFunctionality(PathMetadata<?> metadata) {
        super(Functionality.class, metadata);
    }

}

