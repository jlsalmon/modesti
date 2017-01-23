package cern.modesti.point;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPointImpl is a Querydsl query type for PointImpl
 */
@Generated("com.querydsl.codegen.EmbeddableSerializer")
public class QPointImpl extends BeanPath<PointImpl> {

    private static final long serialVersionUID = -2053626183L;

    public static final QPointImpl pointImpl = new QPointImpl("pointImpl");

    public final BooleanPath dirty = createBoolean("dirty");

    public final BooleanPath empty = createBoolean("empty");

    public final ListPath<Error, QError> errors = this.<Error, QError>createList("errors", Error.class, QError.class, PathInits.DIRECT2);

    public final NumberPath<Long> lineNo = createNumber("lineNo", Long.class);

    public final MapPath<String, Object, SimplePath<Object>> properties = this.<String, Object, SimplePath<Object>>createMap("properties", String.class, Object.class, SimplePath.class);

    public final BooleanPath selected = createBoolean("selected");

    public final BooleanPath valid = createBoolean("valid");

    public QPointImpl(String variable) {
        super(PointImpl.class, forVariable(variable));
    }

    public QPointImpl(Path<? extends PointImpl> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPointImpl(PathMetadata metadata) {
        super(PointImpl.class, metadata);
    }

}

