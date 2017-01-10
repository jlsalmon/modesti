package cern.modesti.point;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QError is a Querydsl query type for Error
 */
@Generated("com.querydsl.codegen.EmbeddableSerializer")
public class QError extends BeanPath<Error> {

    private static final long serialVersionUID = -1316949647L;

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

    public QError(PathMetadata metadata) {
        super(Error.class, metadata);
    }

}

