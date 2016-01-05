package cern.modesti.request.history;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QRequestHistoryEntry is a Querydsl query type for RequestHistoryEntry
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QRequestHistoryEntry extends EntityPathBase<RequestHistoryEntry> {

    private static final long serialVersionUID = 1840256379L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRequestHistoryEntry requestHistoryEntry = new QRequestHistoryEntry("requestHistoryEntry");

    public final BooleanPath deleted = createBoolean("deleted");

    public final ListPath<RequestHistoryChange, QRequestHistoryChange> differences = this.<RequestHistoryChange, QRequestHistoryChange>createList("differences", RequestHistoryChange.class, QRequestHistoryChange.class, PathInits.DIRECT2);

    public final StringPath id = createString("id");

    public final cern.modesti.request.QRequest originalRequest;

    public final StringPath requestId = createString("requestId");

    public QRequestHistoryEntry(String variable) {
        this(RequestHistoryEntry.class, forVariable(variable), INITS);
    }

    public QRequestHistoryEntry(Path<? extends RequestHistoryEntry> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QRequestHistoryEntry(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QRequestHistoryEntry(PathMetadata<?> metadata, PathInits inits) {
        this(RequestHistoryEntry.class, metadata, inits);
    }

    public QRequestHistoryEntry(Class<? extends RequestHistoryEntry> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.originalRequest = inits.isInitialized("originalRequest") ? new cern.modesti.request.QRequest(forProperty("originalRequest"), inits.get("originalRequest")) : null;
    }

}

