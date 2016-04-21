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

    public static final QRequest request = new QRequest("request");

    public final StringPath assignee = createString("assignee");

    public final ListPath<String, StringPath> childRequestIds = this.<String, StringPath>createList("childRequestIds", String.class, StringPath.class, PathInits.DIRECT2);

    public final ListPath<Comment, QComment> comments = this.<Comment, QComment>createList("comments", Comment.class, QComment.class, PathInits.DIRECT2);

    public final DateTimePath<org.joda.time.DateTime> createdAt = createDateTime("createdAt", org.joda.time.DateTime.class);

    public final StringPath creator = createString("creator");

    public final StringPath description = createString("description");

    public final StringPath domain = createString("domain");

    public final StringPath id = createString("id");

    public final StringPath parentRequestId = createString("parentRequestId");

    public final ListPath<cern.modesti.request.point.Point, cern.modesti.request.point.QPoint> points = this.<cern.modesti.request.point.Point, cern.modesti.request.point.QPoint>createList("points", cern.modesti.request.point.Point.class, cern.modesti.request.point.QPoint.class, PathInits.DIRECT2);

    public final MapPath<String, Object, SimplePath<Object>> properties = this.<String, Object, SimplePath<Object>>createMap("properties", String.class, Object.class, SimplePath.class);

    public final StringPath requestId = createString("requestId");

    public final StringPath status = createString("status");

    public final EnumPath<RequestType> type = createEnum("type", RequestType.class);

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QRequest(String variable) {
        super(Request.class, forVariable(variable));
    }

    public QRequest(Path<? extends Request> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRequest(PathMetadata<?> metadata) {
        super(Request.class, metadata);
    }

}

