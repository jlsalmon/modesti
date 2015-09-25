package cern.modesti.schema.category;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QCategory is a Querydsl query type for Category
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QCategory extends BeanPath<Category> {

    private static final long serialVersionUID = 155233018L;

    public static final QCategory category = new QCategory("category");

    public final ListPath<Constraint, SimplePath<Constraint>> constraints = this.<Constraint, SimplePath<Constraint>>createList("constraints", Constraint.class, SimplePath.class, PathInits.DIRECT2);

    public final SimplePath<Object> editable = createSimple("editable", Object.class);

    public final ListPath<String, StringPath> excludes = this.<String, StringPath>createList("excludes", String.class, StringPath.class, PathInits.DIRECT2);

    public final ListPath<cern.modesti.schema.field.Field, SimplePath<cern.modesti.schema.field.Field>> fields = this.<cern.modesti.schema.field.Field, SimplePath<cern.modesti.schema.field.Field>>createList("fields", cern.modesti.schema.field.Field.class, SimplePath.class, PathInits.DIRECT2);

    public final StringPath id = createString("id");

    public final StringPath name_en = createString("name_en");

    public final StringPath name_fr = createString("name_fr");

    public QCategory(String variable) {
        super(Category.class, forVariable(variable));
    }

    public QCategory(Path<? extends Category> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCategory(PathMetadata<?> metadata) {
        super(Category.class, metadata);
    }

}

