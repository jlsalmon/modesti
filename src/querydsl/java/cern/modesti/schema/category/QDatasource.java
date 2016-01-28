package cern.modesti.schema.category;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QDatasource is a Querydsl query type for Datasource
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QDatasource extends BeanPath<Datasource> {

    private static final long serialVersionUID = -252850367L;

    public static final QDatasource datasource = new QDatasource("datasource");

    public final QCategory _super = new QCategory(this);

    //inherited
    public final ListPath<Constraint, SimplePath<Constraint>> constraints = _super.constraints;

    //inherited
    public final SimplePath<Object> editable = _super.editable;

    //inherited
    public final ListPath<String, StringPath> excludes = _super.excludes;

    //inherited
    public final ListPath<cern.modesti.schema.field.Field, cern.modesti.schema.field.QField> fields = _super.fields;

    //inherited
    public final StringPath id = _super.id;

    //inherited
    public final StringPath name_en = _super.name_en;

    //inherited
    public final StringPath name_fr = _super.name_fr;

    public QDatasource(String variable) {
        super(Datasource.class, forVariable(variable));
    }

    public QDatasource(Path<? extends Datasource> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDatasource(PathMetadata<?> metadata) {
        super(Datasource.class, metadata);
    }

}

