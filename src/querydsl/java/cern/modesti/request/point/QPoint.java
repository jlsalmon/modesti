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

    public final BooleanPath alarm = createBoolean("alarm");

    public final BooleanPath configured = createBoolean("configured");

    public final BooleanPath dirty = createBoolean("dirty");

    public final ListPath<cern.modesti.request.point.Error, SimplePath<cern.modesti.request.point.Error>> errors = this.<cern.modesti.request.point.Error, SimplePath<cern.modesti.request.point.Error>>createList("errors", cern.modesti.request.point.Error.class, SimplePath.class, PathInits.DIRECT2);

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

    public QPoint(PathMetadata<?> metadata) {
        super(Point.class, metadata);
    }

}

