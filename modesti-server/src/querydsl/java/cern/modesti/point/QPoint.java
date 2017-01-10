package cern.modesti.point;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPoint is a Querydsl query type for Point
 */
@Generated("com.querydsl.codegen.EmbeddableSerializer")
public class QPoint extends BeanPath<Point> {

    private static final long serialVersionUID = -1306888967L;

    public static final QPoint point = new QPoint("point");

    public final BooleanPath dirty = createBoolean("dirty");

    public final BooleanPath empty = createBoolean("empty");

    public final ListPath<Error, QError> errors = this.<Error, QError>createList("errors", Error.class, QError.class, PathInits.DIRECT2);

    public final NumberPath<Long> lineNo = createNumber("lineNo", Long.class);

    public final MapPath<String, Object, SimplePath<Object>> properties = this.<String, Object, SimplePath<Object>>createMap("properties", String.class, Object.class, SimplePath.class);

    public final BooleanPath selected = createBoolean("selected");

    public final BooleanPath valid = createBoolean("valid");

    public QPoint(String variable) {
        super(Point.class, forVariable(variable));
    }

    public QPoint(Path<? extends Point> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPoint(PathMetadata metadata) {
        super(Point.class, metadata);
    }

}

