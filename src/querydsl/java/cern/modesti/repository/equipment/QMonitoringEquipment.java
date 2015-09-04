package cern.modesti.repository.equipment;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QMonitoringEquipment is a Querydsl query type for MonitoringEquipment
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QMonitoringEquipment extends EntityPathBase<MonitoringEquipment> {

    private static final long serialVersionUID = 660298235L;

    public static final QMonitoringEquipment monitoringEquipment = new QMonitoringEquipment("monitoringEquipment");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final StringPath value = createString("value");

    public QMonitoringEquipment(String variable) {
        super(MonitoringEquipment.class, forVariable(variable));
    }

    public QMonitoringEquipment(Path<? extends MonitoringEquipment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMonitoringEquipment(PathMetadata<?> metadata) {
        super(MonitoringEquipment.class, metadata);
    }

}

