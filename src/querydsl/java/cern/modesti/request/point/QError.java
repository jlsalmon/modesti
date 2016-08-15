package cern.modesti.request.point;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QError is a Querydsl query type for Error
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QError extends BeanPath<Error> {

    private static final long serialVersionUID = -1756306030L;

    public static final QError error = new QError("error");

    public final StringPath category = createString("category");

    public final ListPath<String, StringPath> errors = this.<String, StringPath>createList("errors", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath property = createString("property");

    public QError(String variable) {
        super(Error.class, forVariable(variable));
    }

    public QError(Path<? extends Error> path) {
        super(path.getType(), path.getMetadata());
    }

    public QError(PathMetadata<?> metadata) {
        super(Error.class, metadata);
    }

}

