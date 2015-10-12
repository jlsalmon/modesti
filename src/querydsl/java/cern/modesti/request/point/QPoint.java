package cern.modesti.request.point;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QPoint is a Querydsl query type for Point
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QPoint extends BeanPath<Point> {

    private static final long serialVersionUID = -1746245350L;

    public static final QPoint point = new QPoint("point");

    public final BooleanPath dirty = createBoolean("dirty");

    public final ListPath<Error, SimplePath<Error>> errors = this.<Error, SimplePath<Error>>createList("errors", Error.class, SimplePath.class, PathInits.DIRECT2);

    public final NumberPath<Long> lineNo = createNumber("lineNo", Long.class);

    public final MapPath<String, Object, SimplePath<Object>> properties = this.<String, Object, SimplePath<Object>>createMap("properties", String.class, Object.class, SimplePath.class);

    public final BooleanPath selected = createBoolean("selected");

    public QPoint(String variable) {
        super(Point.class, forVariable(variable));
    }

    public QPoint(Path<? extends Point> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPoint(PathMetadata<?> metadata) {
        super(Point.class, metadata);
    }

}

