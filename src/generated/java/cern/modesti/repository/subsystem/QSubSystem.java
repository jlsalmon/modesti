package cern.modesti.repository.subsystem;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QSubSystem is a Querydsl query type for SubSystem
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QSubSystem extends BeanPath<SubSystem> {

    private static final long serialVersionUID = -1545920443L;

    public static final QSubSystem subSystem = new QSubSystem("subSystem");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath subsystem = createString("subsystem");

    public final StringPath subsystemCode = createString("subsystemCode");

    public final StringPath system = createString("system");

    public final StringPath systemCode = createString("systemCode");

    public final StringPath value = createString("value");

    public QSubSystem(String variable) {
        super(SubSystem.class, forVariable(variable));
    }

    public QSubSystem(Path<? extends SubSystem> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSubSystem(PathMetadata<?> metadata) {
        super(SubSystem.class, metadata);
    }

}

