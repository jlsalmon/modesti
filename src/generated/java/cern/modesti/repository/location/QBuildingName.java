package cern.modesti.repository.location;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QBuildingName is a Querydsl query type for BuildingName
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QBuildingName extends EntityPathBase<BuildingName> {

    private static final long serialVersionUID = -1358609811L;

    public static final QBuildingName buildingName = new QBuildingName("buildingName");

    public final StringPath value = createString("value");

    public QBuildingName(String variable) {
        super(BuildingName.class, forVariable(variable));
    }

    public QBuildingName(Path<? extends BuildingName> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBuildingName(PathMetadata<?> metadata) {
        super(BuildingName.class, metadata);
    }

}

