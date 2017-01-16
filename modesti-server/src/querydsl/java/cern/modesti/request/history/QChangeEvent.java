package cern.modesti.request.history;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChangeEvent is a Querydsl query type for ChangeEvent
 */
@Generated("com.querydsl.codegen.EmbeddableSerializer")
public class QChangeEvent extends BeanPath<ChangeEvent> {

    private static final long serialVersionUID = -650745896L;

    public static final QChangeEvent changeEvent = new QChangeEvent("changeEvent");

    public final DateTimePath<org.joda.time.DateTime> changeDate = createDateTime("changeDate", org.joda.time.DateTime.class);

    public final ListPath<Change, SimplePath<Change>> changes = this.<Change, SimplePath<Change>>createList("changes", Change.class, SimplePath.class, PathInits.DIRECT2);

    public QChangeEvent(String variable) {
        super(ChangeEvent.class, forVariable(variable));
    }

    public QChangeEvent(Path<? extends ChangeEvent> path) {
        super(path.getType(), path.getMetadata());
    }

    public QChangeEvent(PathMetadata metadata) {
        super(ChangeEvent.class, metadata);
    }

}
