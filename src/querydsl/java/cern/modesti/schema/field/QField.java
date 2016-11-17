package cern.modesti.schema.field;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QField is a Querydsl query type for Field
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QField extends BeanPath<Field> {

    private static final long serialVersionUID = -957984046L;

    public static final QField field = new QField("field");

    public final SimplePath<Object> defaultValue = createSimple("defaultValue", Object.class);

    public final SimplePath<Object> editable = createSimple("editable", Object.class);

    public final SimplePath<Object> fixed = createSimple("fixed", Object.class);

    public final StringPath help = createString("help");

    public final StringPath id = createString("id");

    public final NumberPath<Integer> maxLength = createNumber("maxLength", Integer.class);

    public final NumberPath<Integer> minLength = createNumber("minLength", Integer.class);

    public final StringPath model = createString("model");

    public final StringPath name = createString("name");

    public final SimplePath<Object> required = createSimple("required", Object.class);

    public final SimplePath<Object> template = createSimple("template", Object.class);

    public final StringPath type = createString("type");

    public final SimplePath<Object> unique = createSimple("unique", Object.class);

    public QField(String variable) {
        super(Field.class, forVariable(variable));
    }

    public QField(Path<? extends Field> path) {
        super(path.getType(), path.getMetadata());
    }

    public QField(PathMetadata<?> metadata) {
        super(Field.class, metadata);
    }

}

