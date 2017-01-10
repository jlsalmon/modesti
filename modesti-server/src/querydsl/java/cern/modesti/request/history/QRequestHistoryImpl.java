package cern.modesti.request.history;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRequestHistoryImpl is a Querydsl query type for RequestHistoryImpl
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QRequestHistoryImpl extends EntityPathBase<RequestHistoryImpl> {

    private static final long serialVersionUID = 1029312503L;

    public static final QRequestHistoryImpl requestHistoryImpl = new QRequestHistoryImpl("requestHistoryImpl");

    public final BooleanPath deleted = createBoolean("deleted");

    public final ListPath<ChangeEvent, QChangeEvent> events = this.<ChangeEvent, QChangeEvent>createList("events", ChangeEvent.class, QChangeEvent.class, PathInits.DIRECT2);

    public final StringPath id = createString("id");

    public final StringPath idProperty = createString("idProperty");

    public final StringPath requestId = createString("requestId");

    public QRequestHistoryImpl(String variable) {
        super(RequestHistoryImpl.class, forVariable(variable));
    }

    public QRequestHistoryImpl(Path<? extends RequestHistoryImpl> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRequestHistoryImpl(PathMetadata metadata) {
        super(RequestHistoryImpl.class, metadata);
    }

}

