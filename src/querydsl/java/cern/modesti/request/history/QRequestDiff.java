package cern.modesti.request.history;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QRequestDiff is a Querydsl query type for RequestDiff
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QRequestDiff extends EntityPathBase<RequestDiff> {

    private static final long serialVersionUID = -2117681598L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRequestDiff requestDiff = new QRequestDiff("requestDiff");

    public final de.danielbechler.diff.node.QDiffNode diff;

    public final StringPath id = createString("id");

    public final DateTimePath<org.joda.time.DateTime> modificationDate = createDateTime("modificationDate", org.joda.time.DateTime.class);

    public final cern.modesti.user.QUser user;

    public QRequestDiff(String variable) {
        this(RequestDiff.class, forVariable(variable), INITS);
    }

    public QRequestDiff(Path<? extends RequestDiff> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QRequestDiff(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QRequestDiff(PathMetadata<?> metadata, PathInits inits) {
        this(RequestDiff.class, metadata, inits);
    }

    public QRequestDiff(Class<? extends RequestDiff> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.diff = inits.isInitialized("diff") ? new de.danielbechler.diff.node.QDiffNode(forProperty("diff"), inits.get("diff")) : null;
        this.user = inits.isInitialized("user") ? new cern.modesti.user.QUser(forProperty("user")) : null;
    }

}

