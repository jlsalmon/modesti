package cern.modesti.repository.refpoint;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QRefPoint is a Querydsl query type for RefPoint
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QRefPoint extends EntityPathBase<RefPoint> {

    private static final long serialVersionUID = -1925321053L;

    public static final QRefPoint refPoint = new QRefPoint("refPoint");

    public final StringPath address = createString("address");

    public final StringPath addressHierarchy = createString("addressHierarchy");

    public final StringPath alarmFlag = createString("alarmFlag");

    public final NumberPath<Long> alarmId = createNumber("alarmId", Long.class);

    public final StringPath alarmValue = createString("alarmValue");

    public final StringPath analogFlag = createString("analogFlag");

    public final NumberPath<Long> autocallNumber = createNumber("autocallNumber", Long.class);

    public final StringPath buildingFloor = createString("buildingFloor");

    public final StringPath buildingName = createString("buildingName");

    public final NumberPath<Long> buildingNumber = createNumber("buildingNumber", Long.class);

    public final StringPath buildingRoom = createString("buildingRoom");

    public final NumberPath<Long> changeRequestId = createNumber("changeRequestId", Long.class);

    public final StringPath commandFlag = createString("commandFlag");

    public final StringPath commandValue = createString("commandValue");

    public final StringPath controlFlag = createString("controlFlag");

    public final NumberPath<Long> createRequestId = createNumber("createRequestId", Long.class);

    public final NumberPath<Long> faultCode = createNumber("faultCode", Long.class);

    public final StringPath faultFamily = createString("faultFamily");

    public final StringPath faultMember = createString("faultMember");

    public final StringPath functionalityCode = createString("functionalityCode");

    public final StringPath globalFunctionalityCode = createString("globalFunctionalityCode");

    public final StringPath gmaoCode = createString("gmaoCode");

    public final NumberPath<Long> highLimit = createNumber("highLimit", Long.class);

    public final NumberPath<Long> lowLimit = createNumber("lowLimit", Long.class);

    public final StringPath monitoringEquipmentName = createString("monitoringEquipmentName");

    public final StringPath otherEquipmentCode = createString("otherEquipmentCode");

    public final StringPath pointAttribute = createString("pointAttribute");

    public final StringPath pointComplementaryInfo = createString("pointComplementaryInfo");

    public final StringPath pointDatatype = createString("pointDatatype");

    public final StringPath pointDescription = createString("pointDescription");

    public final NumberPath<Long> pointId = createNumber("pointId", Long.class);

    public final NumberPath<Long> pointState = createNumber("pointState", Long.class);

    public final StringPath pointStateText = createString("pointStateText");

    public final NumberPath<Long> priorityCode = createNumber("priorityCode", Long.class);

    public final StringPath responsibleFirstName = createString("responsibleFirstName");

    public final StringPath responsibleLastName = createString("responsibleLastName");

    public final NumberPath<Long> responsiblePersonId = createNumber("responsiblePersonId", Long.class);

    public final StringPath subsystemName = createString("subsystemName");

    public final StringPath systemName = createString("systemName");

    public final StringPath timberFlag = createString("timberFlag");

    public final StringPath timTagName = createString("timTagName");

    public final NumberPath<Long> valueDeadband = createNumber("valueDeadband", Long.class);

    public QRefPoint(String variable) {
        super(RefPoint.class, forVariable(variable));
    }

    public QRefPoint(Path<? extends RefPoint> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRefPoint(PathMetadata<?> metadata) {
        super(RefPoint.class, metadata);
    }

}

