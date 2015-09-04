package cern.modesti.point.state;

import static com.mysema.query.types.PathMetadataFactory.*;

import cern.modesti.request.point.state.Addressing;
import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QAddressing is a Querydsl query type for Addressing
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QAddressing extends BeanPath<Addressing> {

    private static final long serialVersionUID = 153975266L;

    public static final QAddressing addressing = new QAddressing("addressing");

    public final BooleanPath addressed = createBoolean("addressed");

    public final StringPath message = createString("message");

    public QAddressing(String variable) {
        super(Addressing.class, forVariable(variable));
    }

    public QAddressing(Path<? extends Addressing> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAddressing(PathMetadata<?> metadata) {
        super(Addressing.class, metadata);
    }

}

