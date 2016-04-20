package cern.modesti.user;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QUser extends BeanPath<User> {

    private static final long serialVersionUID = -600982699L;

    public static final QUser user = new QUser("user");

    public final BooleanPath accountNonExpired = createBoolean("accountNonExpired");

    public final BooleanPath accountNonLocked = createBoolean("accountNonLocked");

    public final ListPath<Role, SimplePath<Role>> authorities = this.<Role, SimplePath<Role>>createList("authorities", Role.class, SimplePath.class, PathInits.DIRECT2);

    public final BooleanPath credentialsNonExpired = createBoolean("credentialsNonExpired");

    public final StringPath email = createString("email");

    public final NumberPath<Integer> employeeId = createNumber("employeeId", Integer.class);

    public final BooleanPath enabled = createBoolean("enabled");

    public final StringPath firstName = createString("firstName");

    public final StringPath lastName = createString("lastName");

    public final StringPath password = createString("password");

    public final StringPath username = createString("username");

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata<?> metadata) {
        super(User.class, metadata);
    }

}

