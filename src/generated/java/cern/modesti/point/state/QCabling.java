package cern.modesti.point.state;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QCabling is a Querydsl query type for Cabling
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QCabling extends BeanPath<Cabling> {

    private static final long serialVersionUID = -2084671546L;

    public static final QCabling cabling = new QCabling("cabling");

    public final BooleanPath cabled = createBoolean("cabled");

    public final StringPath message = createString("message");

    public QCabling(String variable) {
        super(Cabling.class, forVariable(variable));
    }

    public QCabling(Path<? extends Cabling> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCabling(PathMetadata<?> metadata) {
        super(Cabling.class, metadata);
    }

}

