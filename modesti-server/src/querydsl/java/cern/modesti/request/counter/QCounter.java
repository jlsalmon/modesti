package cern.modesti.request.counter;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QCounter is a Querydsl query type for Counter
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCounter extends EntityPathBase<Counter> {

    private static final long serialVersionUID = -294141582L;

    public static final QCounter counter = new QCounter("counter");

    public final StringPath id = createString("id");

    public final NumberPath<Long> sequence = createNumber("sequence", Long.class);

    public QCounter(String variable) {
        super(Counter.class, forVariable(variable));
    }

    public QCounter(Path<? extends Counter> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCounter(PathMetadata metadata) {
        super(Counter.class, metadata);
    }

}

