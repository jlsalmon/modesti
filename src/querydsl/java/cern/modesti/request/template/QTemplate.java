package cern.modesti.request.template;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QTemplate is a Querydsl query type for Template
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QTemplate extends EntityPathBase<Template> {

    private static final long serialVersionUID = 1409918742L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTemplate template = new QTemplate("template");

    public final cern.modesti.request.QRequest _super;

    // inherited
    public final cern.modesti.user.QUser assignee;

    //inherited
    public final ListPath<String, StringPath> childRequestIds;

    //inherited
    public final ListPath<cern.modesti.request.Comment, cern.modesti.request.QComment> comments;

    //inherited
    public final DateTimePath<org.joda.time.DateTime> createdAt;

    // inherited
    public final cern.modesti.user.QUser creator;

    //inherited
    public final StringPath description;

    //inherited
    public final StringPath domain;

    //inherited
    public final StringPath id;

    //inherited
    public final StringPath parentRequestId;

    //inherited
    public final ListPath<cern.modesti.request.point.Point, cern.modesti.request.point.QPoint> points;

    //inherited
    public final MapPath<String, Object, SimplePath<Object>> properties;

    //inherited
    public final StringPath requestId;

    //inherited
    public final StringPath status;

    //inherited
    public final StringPath subsystem;

    //inherited
    public final EnumPath<cern.modesti.request.RequestType> type;

    //inherited
    public final NumberPath<Long> version;

    public QTemplate(String variable) {
        this(Template.class, forVariable(variable), INITS);
    }

    public QTemplate(Path<? extends Template> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QTemplate(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QTemplate(PathMetadata<?> metadata, PathInits inits) {
        this(Template.class, metadata, inits);
    }

    public QTemplate(Class<? extends Template> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new cern.modesti.request.QRequest(type, metadata, inits);
        this.assignee = _super.assignee;
        this.childRequestIds = _super.childRequestIds;
        this.comments = _super.comments;
        this.createdAt = _super.createdAt;
        this.creator = _super.creator;
        this.description = _super.description;
        this.domain = _super.domain;
        this.id = _super.id;
        this.parentRequestId = _super.parentRequestId;
        this.points = _super.points;
        this.properties = _super.properties;
        this.requestId = _super.requestId;
        this.status = _super.status;
        this.subsystem = _super.subsystem;
        this.type = _super.type;
        this.version = _super.version;
    }

}

