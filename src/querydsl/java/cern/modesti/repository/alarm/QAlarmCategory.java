package cern.modesti.repository.alarm;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QAlarmCategory is a Querydsl query type for AlarmCategory
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QAlarmCategory extends EntityPathBase<AlarmCategory> {

    private static final long serialVersionUID = 1564539911L;

    public static final QAlarmCategory alarmCategory = new QAlarmCategory("alarmCategory");

    public final StringPath value = createString("value");

    public QAlarmCategory(String variable) {
        super(AlarmCategory.class, forVariable(variable));
    }

    public QAlarmCategory(Path<? extends AlarmCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAlarmCategory(PathMetadata<?> metadata) {
        super(AlarmCategory.class, metadata);
    }

}

