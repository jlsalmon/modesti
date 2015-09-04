package cern.modesti.repository.location.zone;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QSafetyZone is a Querydsl query type for SafetyZone
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QSafetyZone extends EntityPathBase<SafetyZone> {

    private static final long serialVersionUID = -1335485550L;

    public static final QSafetyZone safetyZone = new QSafetyZone("safetyZone");

    public final StringPath value = createString("value");

    public QSafetyZone(String variable) {
        super(SafetyZone.class, forVariable(variable));
    }

    public QSafetyZone(Path<? extends SafetyZone> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSafetyZone(PathMetadata<?> metadata) {
        super(SafetyZone.class, metadata);
    }

}

