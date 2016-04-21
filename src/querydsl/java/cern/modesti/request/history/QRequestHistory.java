package cern.modesti.request.history;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QRequestHistory is a Querydsl query type for RequestHistory
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QRequestHistory extends EntityPathBase<RequestHistory> {

    private static final long serialVersionUID = 189705527L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRequestHistory requestHistory = new QRequestHistory("requestHistory");

    public final BooleanPath deleted = createBoolean("deleted");

    public final ListPath<ChangeEvent, QChangeEvent> events = this.<ChangeEvent, QChangeEvent>createList("events", ChangeEvent.class, QChangeEvent.class, PathInits.DIRECT2);

    public final StringPath id = createString("id");

    public final cern.modesti.request.QRequest originalRequest;

    public final StringPath requestId = createString("requestId");

    public QRequestHistory(String variable) {
        this(RequestHistory.class, forVariable(variable), INITS);
    }

    public QRequestHistory(Path<? extends RequestHistory> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QRequestHistory(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QRequestHistory(PathMetadata<?> metadata, PathInits inits) {
        this(RequestHistory.class, metadata, inits);
    }

    public QRequestHistory(Class<? extends RequestHistory> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.originalRequest = inits.isInitialized("originalRequest") ? new cern.modesti.request.QRequest(forProperty("originalRequest")) : null;
    }

}

