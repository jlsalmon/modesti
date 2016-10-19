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

    public final BooleanPath abstract$ = createBoolean("abstract");

    public final ListPath<cern.modesti.schema.category.Category, cern.modesti.schema.category.QCategory> categories = this.<cern.modesti.schema.category.Category, cern.modesti.schema.category.QCategory>createList("categories", cern.modesti.schema.category.Category.class, cern.modesti.schema.category.QCategory.class, PathInits.DIRECT2);

    public final ListPath<cern.modesti.schema.category.Datasource, cern.modesti.schema.category.QDatasource> datasourceOverrides = this.<cern.modesti.schema.category.Datasource, cern.modesti.schema.category.QDatasource>createList("datasourceOverrides", cern.modesti.schema.category.Datasource.class, cern.modesti.schema.category.QDatasource.class, PathInits.DIRECT2);

    public final ListPath<cern.modesti.schema.category.Datasource, cern.modesti.schema.category.QDatasource> datasources = this.<cern.modesti.schema.category.Datasource, cern.modesti.schema.category.QDatasource>createList("datasources", cern.modesti.schema.category.Datasource.class, cern.modesti.schema.category.QDatasource.class, PathInits.DIRECT2);

    public final StringPath description = createString("description");

    public final ListPath<cern.modesti.schema.field.Field, cern.modesti.schema.field.QField> fields = this.<cern.modesti.schema.field.Field, cern.modesti.schema.field.QField>createList("fields", cern.modesti.schema.field.Field.class, cern.modesti.schema.field.QField.class, PathInits.DIRECT2);

    public final StringPath id = createString("id");

    public final BooleanPath isAbstract = createBoolean("isAbstract");

    public final ListPath<cern.modesti.schema.category.Category, cern.modesti.schema.category.QCategory> overrides = this.<cern.modesti.schema.category.Category, cern.modesti.schema.category.QCategory>createList("overrides", cern.modesti.schema.category.Category.class, cern.modesti.schema.category.QCategory.class, PathInits.DIRECT2);

    public final StringPath parent = createString("parent");

    public final ListPath<RowCommentStateDescriptor, QRowCommentStateDescriptor> rowCommentStates = this.<RowCommentStateDescriptor, QRowCommentStateDescriptor>createList("rowCommentStates", RowCommentStateDescriptor.class, QRowCommentStateDescriptor.class, PathInits.DIRECT2);

    public final ListPath<String, StringPath> selectableStates = this.<String, StringPath>createList("selectableStates", String.class, StringPath.class, PathInits.DIRECT2);

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

