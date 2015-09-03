package cern.modesti.point;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QPoint is a Querydsl query type for Point
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QPoint extends EntityPathBase<Point> {

    private static final long serialVersionUID = -1306888967L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPoint point = new QPoint("point");

    public final cern.modesti.point.state.QAddressing addressing;

    public final BooleanPath alarm = createBoolean("alarm");

    public final cern.modesti.point.state.QApproval approval;

    public final cern.modesti.point.state.QCabling cabling;

    public final BooleanPath configured = createBoolean("configured");

    public final BooleanPath dirty = createBoolean("dirty");

    public final ListPath<cern.modesti.point.state.Error, cern.modesti.point.state.QError> errors = this.<cern.modesti.point.state.Error, cern.modesti.point.state.QError>createList("errors", cern.modesti.point.state.Error.class, cern.modesti.point.state.QError.class, PathInits.DIRECT2);

    public final StringPath id = createString("id");

    public final NumberPath<Long> lineNo = createNumber("lineNo", Long.class);

    public final MapPath<String, Object, SimplePath<Object>> properties = this.<String, Object, SimplePath<Object>>createMap("properties", String.class, Object.class, SimplePath.class);

    public final BooleanPath selected = createBoolean("selected");

    public final cern.modesti.point.state.QTesting testing;

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
        this.addressing = inits.isInitialized("addressing") ? new cern.modesti.point.state.QAddressing(forProperty("addressing")) : null;
        this.approval = inits.isInitialized("approval") ? new cern.modesti.point.state.QApproval(forProperty("approval")) : null;
        this.cabling = inits.isInitialized("cabling") ? new cern.modesti.point.state.QCabling(forProperty("cabling")) : null;
        this.testing = inits.isInitialized("testing") ? new cern.modesti.point.state.QTesting(forProperty("testing")) : null;
    }

}

