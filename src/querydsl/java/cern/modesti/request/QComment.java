package cern.modesti.request;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QComment is a Querydsl query type for Comment
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QComment extends BeanPath<Comment> {

    private static final long serialVersionUID = -811157561L;

    public static final QComment comment = new QComment("comment");

    public final StringPath text = createString("text");

    public final DateTimePath<java.util.Date> timestamp = createDateTime("timestamp", java.util.Date.class);

    public final StringPath user = createString("user");

    public QComment(String variable) {
        super(Comment.class, forVariable(variable));
    }

    public QComment(Path<? extends Comment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QComment(PathMetadata<?> metadata) {
        super(Comment.class, metadata);
    }

}

