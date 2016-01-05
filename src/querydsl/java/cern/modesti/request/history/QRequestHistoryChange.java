package cern.modesti.request.history;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QRequestHistoryChange is a Querydsl query type for RequestHistoryChange
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QRequestHistoryChange extends BeanPath<RequestHistoryChange> {

    private static final long serialVersionUID = 1150003143L;

    public static final QRequestHistoryChange requestHistoryChange = new QRequestHistoryChange("requestHistoryChange");

    public final DateTimePath<org.joda.time.DateTime> changeDate = createDateTime("changeDate", org.joda.time.DateTime.class);

    public final ListPath<RequestHistoryChangeItem, SimplePath<RequestHistoryChangeItem>> items = this.<RequestHistoryChangeItem, SimplePath<RequestHistoryChangeItem>>createList("items", RequestHistoryChangeItem.class, SimplePath.class, PathInits.DIRECT2);

    public QRequestHistoryChange(String variable) {
        super(RequestHistoryChange.class, forVariable(variable));
    }

    public QRequestHistoryChange(Path<? extends RequestHistoryChange> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRequestHistoryChange(PathMetadata<?> metadata) {
        super(RequestHistoryChange.class, metadata);
    }

}

