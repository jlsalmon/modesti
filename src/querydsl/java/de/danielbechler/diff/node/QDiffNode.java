package de.danielbechler.diff.node;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QDiffNode is a Querydsl query type for DiffNode
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QDiffNode extends BeanPath<DiffNode> {

    private static final long serialVersionUID = -601624429L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDiffNode diffNode = new QDiffNode("diffNode");

    public final SimplePath<de.danielbechler.diff.access.Accessor> accessor = createSimple("accessor", de.danielbechler.diff.access.Accessor.class);

    public final BooleanPath added = createBoolean("added");

    public final SetPath<String, StringPath> categories = this.<String, StringPath>createSet("categories", String.class, StringPath.class, PathInits.DIRECT2);

    public final BooleanPath changed = createBoolean("changed");

    public final SimplePath<de.danielbechler.diff.identity.IdentityStrategy> childIdentityStrategy = createSimple("childIdentityStrategy", de.danielbechler.diff.identity.IdentityStrategy.class);

    public final MapPath<de.danielbechler.diff.selector.ElementSelector, DiffNode, QDiffNode> children = this.<de.danielbechler.diff.selector.ElementSelector, DiffNode, QDiffNode>createMap("children", de.danielbechler.diff.selector.ElementSelector.class, DiffNode.class, QDiffNode.class);

    public final QDiffNode circleStartNode;

    public final ComparablePath<de.danielbechler.diff.path.NodePath> circleStartPath = createComparable("circleStartPath", de.danielbechler.diff.path.NodePath.class);

    public final BooleanPath circular = createBoolean("circular");

    public final SimplePath<de.danielbechler.diff.selector.ElementSelector> elementSelector = createSimple("elementSelector", de.danielbechler.diff.selector.ElementSelector.class);

    public final BooleanPath excluded = createBoolean("excluded");

    public final SetPath<java.lang.annotation.Annotation, SimplePath<java.lang.annotation.Annotation>> fieldAnnotations = this.<java.lang.annotation.Annotation, SimplePath<java.lang.annotation.Annotation>>createSet("fieldAnnotations", java.lang.annotation.Annotation.class, SimplePath.class, PathInits.DIRECT2);

    public final BooleanPath ignored = createBoolean("ignored");

    public final QDiffNode parentNode;

    public final ComparablePath<de.danielbechler.diff.path.NodePath> path = createComparable("path", de.danielbechler.diff.path.NodePath.class);

    public final SetPath<java.lang.annotation.Annotation, SimplePath<java.lang.annotation.Annotation>> propertyAnnotations = this.<java.lang.annotation.Annotation, SimplePath<java.lang.annotation.Annotation>>createSet("propertyAnnotations", java.lang.annotation.Annotation.class, SimplePath.class, PathInits.DIRECT2);

    public final BooleanPath propertyAware = createBoolean("propertyAware");

    public final StringPath propertyName = createString("propertyName");

    public final BooleanPath removed = createBoolean("removed");

    public final BooleanPath rootNode = createBoolean("rootNode");

    public final EnumPath<DiffNode.State> state = createEnum("state", DiffNode.State.class);

    public final BooleanPath untouched = createBoolean("untouched");

    public final SimplePath<Class<?>> valueType = createSimple("valueType", Class.class);

    public final SimplePath<de.danielbechler.diff.instantiation.TypeInfo> valueTypeInfo = createSimple("valueTypeInfo", de.danielbechler.diff.instantiation.TypeInfo.class);

    public QDiffNode(String variable) {
        this(DiffNode.class, forVariable(variable), INITS);
    }

    public QDiffNode(Path<? extends DiffNode> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QDiffNode(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QDiffNode(PathMetadata<?> metadata, PathInits inits) {
        this(DiffNode.class, metadata, inits);
    }

    public QDiffNode(Class<? extends DiffNode> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.circleStartNode = inits.isInitialized("circleStartNode") ? new QDiffNode(forProperty("circleStartNode"), inits.get("circleStartNode")) : null;
        this.parentNode = inits.isInitialized("parentNode") ? new QDiffNode(forProperty("parentNode"), inits.get("parentNode")) : null;
    }

}

