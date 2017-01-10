package cern.modesti.request;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRequestImpl is a Querydsl query type for RequestImpl
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QRequestImpl extends EntityPathBase<RequestImpl> {

    private static final long serialVersionUID = 1907736247L;

    public static final QRequestImpl requestImpl = new QRequestImpl("requestImpl");

    public final StringPath assignee = createString("assignee");

    public final ListPath<String, StringPath> childRequestIds = this.<String, StringPath>createList("childRequestIds", String.class, StringPath.class, PathInits.DIRECT2);

    public final ListPath<Comment, QComment> comments = this.<Comment, QComment>createList("comments", Comment.class, QComment.class, PathInits.DIRECT2);

    public final DateTimePath<org.joda.time.DateTime> createdAt = createDateTime("createdAt", org.joda.time.DateTime.class);

    public final StringPath creator = createString("creator");

    public final StringPath description = createString("description");

    public final StringPath domain = createString("domain");

    public final MapPath<Long, java.util.List<cern.modesti.point.Error>, SimplePath<java.util.List<cern.modesti.point.Error>>> errors = this.<Long, java.util.List<cern.modesti.point.Error>, SimplePath<java.util.List<cern.modesti.point.Error>>>createMap("errors", Long.class, java.util.List.class, SimplePath.class);

    public final StringPath id = createString("id");

    public final StringPath parentRequestId = createString("parentRequestId");

    public final ListPath<cern.modesti.point.Point, cern.modesti.point.QPoint> points = this.<cern.modesti.point.Point, cern.modesti.point.QPoint>createList("points", cern.modesti.point.Point.class, cern.modesti.point.QPoint.class, PathInits.DIRECT2);

    public final MapPath<String, Object, SimplePath<Object>> properties = this.<String, Object, SimplePath<Object>>createMap("properties", String.class, Object.class, SimplePath.class);

    public final StringPath requestId = createString("requestId");

    public final StringPath status = createString("status");

    public final EnumPath<RequestType> type = createEnum("type", RequestType.class);

    public final BooleanPath valid = createBoolean("valid");

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QRequestImpl(String variable) {
        super(RequestImpl.class, forVariable(variable));
    }

    public QRequestImpl(Path<? extends RequestImpl> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRequestImpl(PathMetadata metadata) {
        super(RequestImpl.class, metadata);
    }

}

