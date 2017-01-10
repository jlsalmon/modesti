package cern.modesti.schema;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSchemaImpl is a Querydsl query type for SchemaImpl
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QSchemaImpl extends EntityPathBase<SchemaImpl> {

    private static final long serialVersionUID = 1399081493L;

    public static final QSchemaImpl schemaImpl = new QSchemaImpl("schemaImpl");

    public final BooleanPath abstract$ = createBoolean("abstract");

    public final ListPath<cern.modesti.schema.category.Category, cern.modesti.schema.category.QCategory> categories = this.<cern.modesti.schema.category.Category, cern.modesti.schema.category.QCategory>createList("categories", cern.modesti.schema.category.Category.class, cern.modesti.schema.category.QCategory.class, PathInits.DIRECT2);

    public final ListPath<cern.modesti.schema.category.Datasource, cern.modesti.schema.category.QDatasource> datasourceOverrides = this.<cern.modesti.schema.category.Datasource, cern.modesti.schema.category.QDatasource>createList("datasourceOverrides", cern.modesti.schema.category.Datasource.class, cern.modesti.schema.category.QDatasource.class, PathInits.DIRECT2);

    public final ListPath<cern.modesti.schema.category.Datasource, cern.modesti.schema.category.QDatasource> datasources = this.<cern.modesti.schema.category.Datasource, cern.modesti.schema.category.QDatasource>createList("datasources", cern.modesti.schema.category.Datasource.class, cern.modesti.schema.category.QDatasource.class, PathInits.DIRECT2);

    public final StringPath description = createString("description");

    public final ListPath<cern.modesti.schema.field.Field, cern.modesti.schema.field.QField> fields = this.<cern.modesti.schema.field.Field, cern.modesti.schema.field.QField>createList("fields", cern.modesti.schema.field.Field.class, cern.modesti.schema.field.QField.class, PathInits.DIRECT2);

    public final StringPath id = createString("id");

    public final StringPath idProperty = createString("idProperty");

    public final BooleanPath isAbstract = createBoolean("isAbstract");

    public final ListPath<cern.modesti.schema.category.Category, cern.modesti.schema.category.QCategory> overrides = this.<cern.modesti.schema.category.Category, cern.modesti.schema.category.QCategory>createList("overrides", cern.modesti.schema.category.Category.class, cern.modesti.schema.category.QCategory.class, PathInits.DIRECT2);

    public final StringPath parent = createString("parent");

    public final ListPath<RowCommentStateDescriptor, QRowCommentStateDescriptor> rowCommentStates = this.<RowCommentStateDescriptor, QRowCommentStateDescriptor>createList("rowCommentStates", RowCommentStateDescriptor.class, QRowCommentStateDescriptor.class, PathInits.DIRECT2);

    public final ListPath<String, StringPath> selectableStates = this.<String, StringPath>createList("selectableStates", String.class, StringPath.class, PathInits.DIRECT2);

    public QSchemaImpl(String variable) {
        super(SchemaImpl.class, forVariable(variable));
    }

    public QSchemaImpl(Path<? extends SchemaImpl> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSchemaImpl(PathMetadata metadata) {
        super(SchemaImpl.class, metadata);
    }

}

