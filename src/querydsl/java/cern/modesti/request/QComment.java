package cern.modesti.request;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QComment is a Querydsl query type for Comment
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QComment extends BeanPath<Comment> {

    private static final long serialVersionUID = -811157561L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QComment comment = new QComment("comment");

    public final StringPath text = createString("text");

    public final DateTimePath<java.util.Date> timestamp = createDateTime("timestamp", java.util.Date.class);

    public final cern.modesti.user.QUser user;

    public QComment(String variable) {
        this(Comment.class, forVariable(variable), INITS);
    }

    public QComment(Path<? extends Comment> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QComment(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QComment(PathMetadata<?> metadata, PathInits inits) {
        this(Comment.class, metadata, inits);
    }

    public QComment(Class<? extends Comment> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new cern.modesti.user.QUser(forProperty("user")) : null;
    }

}

