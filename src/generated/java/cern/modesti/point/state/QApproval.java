package cern.modesti.point.state;

import static com.mysema.query.types.PathMetadataFactory.*;

import cern.modesti.request.point.state.Approval;
import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QApproval is a Querydsl query type for Approval
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QApproval extends BeanPath<Approval> {

    private static final long serialVersionUID = 1443227063L;

    public static final QApproval approval = new QApproval("approval");

    public final BooleanPath approved = createBoolean("approved");

    public final StringPath message = createString("message");

    public QApproval(String variable) {
        super(Approval.class, forVariable(variable));
    }

    public QApproval(Path<? extends Approval> path) {
        super(path.getType(), path.getMetadata());
    }

    public QApproval(PathMetadata<?> metadata) {
        super(Approval.class, metadata);
    }

}

