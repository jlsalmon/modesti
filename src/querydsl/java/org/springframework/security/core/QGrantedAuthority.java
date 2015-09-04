package org.springframework.security.core;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QGrantedAuthority is a Querydsl query type for GrantedAuthority
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QGrantedAuthority extends BeanPath<GrantedAuthority> {

    private static final long serialVersionUID = 1264551024L;

    public static final QGrantedAuthority grantedAuthority = new QGrantedAuthority("grantedAuthority");

    public final StringPath authority = createString("authority");

    public QGrantedAuthority(String variable) {
        super(GrantedAuthority.class, forVariable(variable));
    }

    public QGrantedAuthority(Path<? extends GrantedAuthority> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGrantedAuthority(PathMetadata<?> metadata) {
        super(GrantedAuthority.class, metadata);
    }

}

