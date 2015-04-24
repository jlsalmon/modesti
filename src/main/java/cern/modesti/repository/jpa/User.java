package cern.modesti.repository.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureParameter;


@Entity
@NamedStoredProcedureQuery(name = "validate", procedureName = "timpkutil.STF_GET_RECCOUNT", parameters = {
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_tabname", type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_condition", type = String.class) })
public class User {

  @Id
  @GeneratedValue
  private Long id;
}