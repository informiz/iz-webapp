package org.informiz.model;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

// TODO: Consider deprecating local-id and using entity-id as primary-key. I.e. add annotations:
/*
    @Id
    @GeneratedValue(generator = "entity-id-generator")
    @GenericGenerator(name = "entity-id-generator",
            strategy = "org.informiz.model.EntityIdGenerator")
*/

public class EntityIdGenerator implements IdentifierGenerator {
    @Override
    public Object generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        try {
            return Utils.createEntityId((ChainCodeEntity)o);
        } catch (ClassCastException e) {
            String name = (o == null) ? "Null" : o.getClass().getSimpleName();
            throw new IllegalArgumentException("Can't generate an id for " + name);
        }
    }
}
