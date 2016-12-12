package cern.modesti.schema;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QRowCommentStateDescriptor is a Querydsl query type for RowCommentStateDescriptor
 */
@Generated("com.querydsl.codegen.EmbeddableSerializer")
public class QRowCommentStateDescriptor extends BeanPath<RowCommentStateDescriptor> {

    private static final long serialVersionUID = -253787193L;

    public static final QRowCommentStateDescriptor rowCommentStateDescriptor = new QRowCommentStateDescriptor("rowCommentStateDescriptor");

    public final StringPath property = createString("property");

    public final StringPath status = createString("status");

    public QRowCommentStateDescriptor(String variable) {
        super(RowCommentStateDescriptor.class, forVariable(variable));
    }

    public QRowCommentStateDescriptor(Path<? extends RowCommentStateDescriptor> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRowCommentStateDescriptor(PathMetadata metadata) {
        super(RowCommentStateDescriptor.class, metadata);
    }

}

