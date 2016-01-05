package cern.modesti.request;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QRequest is a Querydsl query type for Request
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QRequest extends EntityPathBase<Request> {

    private static final long serialVersionUID = -665863177L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRequest request = new QRequest("request");

    public final cern.modesti.user.QUser assignee;

    public final ListPath<String, StringPath> childRequestIds = this.<String, StringPath>createList("childRequestIds", String.class, StringPath.class, PathInits.DIRECT2);

    public final ListPath<Comment, QComment> comments = this.<Comment, QComment>createList("comments", Comment.class, QComment.class, PathInits.DIRECT2);

    public final DateTimePath<org.joda.time.DateTime> createdAt = createDateTime("createdAt", org.joda.time.DateTime.class);

    public final cern.modesti.user.QUser creator;

    public final StringPath description = createString("description");

    public final StringPath domain = createString("domain");

    public final StringPath id = createString("id");

    public final StringPath parentRequestId = createString("parentRequestId");

    public final ListPath<cern.modesti.request.point.Point, cern.modesti.request.point.QPoint> points = this.<cern.modesti.request.point.Point, cern.modesti.request.point.QPoint>createList("points", cern.modesti.request.point.Point.class, cern.modesti.request.point.QPoint.class, PathInits.DIRECT2);

    public final MapPath<String, Object, SimplePath<Object>> properties = this.<String, Object, SimplePath<Object>>createMap("properties", String.class, Object.class, SimplePath.class);

    public final StringPath requestId = createString("requestId");

    public final StringPath status = createString("status");

    public final StringPath subsystem = createString("subsystem");

    public final EnumPath<RequestType> type = createEnum("type", RequestType.class);

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QRequest(String variable) {
        this(Request.class, forVariable(variable), INITS);
    }

    public QRequest(Path<? extends Request> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QRequest(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QRequest(PathMetadata<?> metadata, PathInits inits) {
        this(Request.class, metadata, inits);
    }

    public QRequest(Class<? extends Request> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.assignee = inits.isInitialized("assignee") ? new cern.modesti.user.QUser(forProperty("assignee")) : null;
        this.creator = inits.isInitialized("creator") ? new cern.modesti.user.QUser(forProperty("creator")) : null;
    }

}

