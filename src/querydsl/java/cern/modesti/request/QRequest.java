package cern.modesti.request;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRequest is a Querydsl query type for Request
 */
@Generated("com.querydsl.codegen.EntitySerializer")
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

    public final MapPath<Long, java.util.List<cern.modesti.request.point.Error>, SimplePath<java.util.List<cern.modesti.request.point.Error>>> errors = this.<Long, java.util.List<cern.modesti.request.point.Error>, SimplePath<java.util.List<cern.modesti.request.point.Error>>>createMap("errors", Long.class, java.util.List.class, SimplePath.class);

    public final StringPath id = createString("id");

    public final StringPath parentRequestId = createString("parentRequestId");

    public final ListPath<cern.modesti.request.point.Point, cern.modesti.request.point.QPoint> points = this.<cern.modesti.request.point.Point, cern.modesti.request.point.QPoint>createList("points", cern.modesti.request.point.Point.class, cern.modesti.request.point.QPoint.class, PathInits.DIRECT2);

    public final MapPath<String, Object, SimplePath<Object>> properties = this.<String, Object, SimplePath<Object>>createMap("properties", String.class, Object.class, SimplePath.class);

    public final StringPath requestId = createString("requestId");

    public final StringPath status = createString("status");

    public final EnumPath<RequestType> type = createEnum("type", RequestType.class);

    public final BooleanPath valid = createBoolean("valid");

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QRequest(String variable) {
        super(Request.class, forVariable(variable));
    }

    public QRequest(Path<? extends Request> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRequest(PathMetadata metadata) {
        super(Request.class, metadata);
    }

}

