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

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPoint point = new QPoint("point");

    public final cern.modesti.request.point.state.QAddressing addressing;

    public final BooleanPath alarm = createBoolean("alarm");

    public final cern.modesti.request.point.state.QApproval approval;

    public final cern.modesti.request.point.state.QCabling cabling;

    public final BooleanPath configured = createBoolean("configured");

    public final BooleanPath dirty = createBoolean("dirty");

    public final ListPath<cern.modesti.request.point.state.Error, SimplePath<cern.modesti.request.point.state.Error>> errors = this.<cern.modesti.request.point.state.Error, SimplePath<cern.modesti.request.point.state.Error>>createList("errors", cern.modesti.request.point.state.Error.class, SimplePath.class, PathInits.DIRECT2);

    public final NumberPath<Long> lineNo = createNumber("lineNo", Long.class);

    public final MapPath<String, Object, SimplePath<Object>> properties = this.<String, Object, SimplePath<Object>>createMap("properties", String.class, Object.class, SimplePath.class);

    public final BooleanPath selected = createBoolean("selected");

    public final cern.modesti.request.point.state.QTesting testing;

    public final BooleanPath valid = createBoolean("valid");

    public QPoint(String variable) {
        this(Point.class, forVariable(variable), INITS);
    }

    public QPoint(Path<? extends Point> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QPoint(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QPoint(PathMetadata<?> metadata, PathInits inits) {
        this(Point.class, metadata, inits);
    }

    public QPoint(Class<? extends Point> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.addressing = inits.isInitialized("addressing") ? new cern.modesti.request.point.state.QAddressing(forProperty("addressing")) : null;
        this.approval = inits.isInitialized("approval") ? new cern.modesti.request.point.state.QApproval(forProperty("approval")) : null;
        this.cabling = inits.isInitialized("cabling") ? new cern.modesti.request.point.state.QCabling(forProperty("cabling")) : null;
        this.testing = inits.isInitialized("testing") ? new cern.modesti.request.point.state.QTesting(forProperty("testing")) : null;
    }

}

