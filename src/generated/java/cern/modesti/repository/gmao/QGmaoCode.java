package cern.modesti.repository.gmao;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QGmaoCode is a Querydsl query type for GmaoCode
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QGmaoCode extends EntityPathBase<GmaoCode> {

    private static final long serialVersionUID = 1761797936L;

    public static final QGmaoCode gmaoCode = new QGmaoCode("gmaoCode");

    public final StringPath value = createString("value");

    public QGmaoCode(String variable) {
        super(GmaoCode.class, forVariable(variable));
    }

    public QGmaoCode(Path<? extends GmaoCode> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGmaoCode(PathMetadata<?> metadata) {
        super(GmaoCode.class, metadata);
    }

}

