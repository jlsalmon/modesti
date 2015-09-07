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

    public final cern.modesti.request.point.state.QAddressing addressing;

    public final cern.modesti.request.point.state.QApproval approval;

    public final cern.modesti.request.point.state.QCabling cabling;

    public final SetPath<String, StringPath> categories = this.<String, StringPath>createSet("categories", String.class, StringPath.class, PathInits.DIRECT2);

    public final ListPath<String, StringPath> childRequestIds = this.<String, StringPath>createList("childRequestIds", String.class, StringPath.class, PathInits.DIRECT2);

    public final ListPath<Comment, QComment> comments = this.<Comment, QComment>createList("comments", Comment.class, QComment.class, PathInits.DIRECT2);

    public final cern.modesti.workflow.result.QConfigurationResult configurationResult;

    public final cern.modesti.security.ldap.QUser creator;

    public final StringPath description = createString("description");

    public final StringPath domain = createString("domain");

    public final StringPath id = createString("id");

    public final StringPath parentRequestId = createString("parentRequestId");

    public final ListPath<cern.modesti.request.point.Point, cern.modesti.request.point.QPoint> points = this.<cern.modesti.request.point.Point, cern.modesti.request.point.QPoint>createList("points", cern.modesti.request.point.Point.class, cern.modesti.request.point.QPoint.class, PathInits.DIRECT2);

    public final StringPath requestId = createString("requestId");

    public final NumberPath<Float> score = createNumber("score", Float.class);

    public final EnumPath<RequestStatus> status = createEnum("status", RequestStatus.class);

    public final StringPath subsystem = createString("subsystem");

    public final cern.modesti.request.point.state.QTesting testing;

    public final EnumPath<RequestType> type = createEnum("type", RequestType.class);

    public final BooleanPath valid = createBoolean("valid");

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
        this.addressing = inits.isInitialized("addressing") ? new cern.modesti.request.point.state.QAddressing(forProperty("addressing")) : null;
        this.approval = inits.isInitialized("approval") ? new cern.modesti.request.point.state.QApproval(forProperty("approval")) : null;
        this.cabling = inits.isInitialized("cabling") ? new cern.modesti.request.point.state.QCabling(forProperty("cabling")) : null;
        this.configurationResult = inits.isInitialized("configurationResult") ? new cern.modesti.workflow.result.QConfigurationResult(forProperty("configurationResult")) : null;
        this.creator = inits.isInitialized("creator") ? new cern.modesti.security.ldap.QUser(forProperty("creator")) : null;
        this.testing = inits.isInitialized("testing") ? new cern.modesti.request.point.state.QTesting(forProperty("testing")) : null;
    }

}

