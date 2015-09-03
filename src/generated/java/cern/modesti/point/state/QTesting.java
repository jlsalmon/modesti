package cern.modesti.point.state;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QTesting is a Querydsl query type for Testing
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QTesting extends BeanPath<Testing> {

    private static final long serialVersionUID = 248443932L;

    public static final QTesting testing = new QTesting("testing");

    public final StringPath message = createString("message");

    public final BooleanPath tested = createBoolean("tested");

    public QTesting(String variable) {
        super(Testing.class, forVariable(variable));
    }

    public QTesting(Path<? extends Testing> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTesting(PathMetadata<?> metadata) {
        super(Testing.class, metadata);
    }

}

