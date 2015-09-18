package cern.modesti.schema;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QSchema is a Querydsl query type for Schema
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QSchema extends EntityPathBase<Schema> {

    private static final long serialVersionUID = 1180083797L;

    public static final QSchema schema = new QSchema("schema");

    public final ListPath<cern.modesti.schema.category.Category, cern.modesti.schema.category.QCategory> categories = this.<cern.modesti.schema.category.Category, cern.modesti.schema.category.QCategory>createList("categories", cern.modesti.schema.category.Category.class, cern.modesti.schema.category.QCategory.class, PathInits.DIRECT2);

    public final ListPath<cern.modesti.schema.category.Datasource, cern.modesti.schema.category.QDatasource> datasourceOverrides = this.<cern.modesti.schema.category.Datasource, cern.modesti.schema.category.QDatasource>createList("datasourceOverrides", cern.modesti.schema.category.Datasource.class, cern.modesti.schema.category.QDatasource.class, PathInits.DIRECT2);

    public final ListPath<cern.modesti.schema.category.Datasource, cern.modesti.schema.category.QDatasource> datasources = this.<cern.modesti.schema.category.Datasource, cern.modesti.schema.category.QDatasource>createList("datasources", cern.modesti.schema.category.Datasource.class, cern.modesti.schema.category.QDatasource.class, PathInits.DIRECT2);

    public final StringPath domain = createString("domain");

    public final StringPath id = createString("id");

    public final StringPath name = createString("name");

    public final ListPath<cern.modesti.schema.category.Category, cern.modesti.schema.category.QCategory> overrides = this.<cern.modesti.schema.category.Category, cern.modesti.schema.category.QCategory>createList("overrides", cern.modesti.schema.category.Category.class, cern.modesti.schema.category.QCategory.class, PathInits.DIRECT2);

    public final StringPath parent = createString("parent");

    public QSchema(String variable) {
        super(Schema.class, forVariable(variable));
    }

    public QSchema(Path<? extends Schema> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSchema(PathMetadata<?> metadata) {
        super(Schema.class, metadata);
    }

}

