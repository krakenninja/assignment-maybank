package my.com.maybank.jpa.utils;

import java.io.Serializable;
import java.lang.reflect.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.spi.CustomIdGeneratorCreationContext;

/**
 * Custom id generator for {@link Long} type
 * @since 1.0.0
 * @see my.com.maybank.jpa.annotations.LongIdGenerator
 * @see org.hibernate.annotations.IdGeneratorType
 * @see java.lang.System#nanoTime()
 * @author ChristopherCKW
 */
@Getter
@RequiredArgsConstructor
public class LongIdGenerator
       implements IdentifierGenerator
{
    private final my.com.maybank.jpa.annotations.LongIdGenerator config;
    
    private final Member annotatedMember;
    
    private final CustomIdGeneratorCreationContext context;
    
    @Override
    public Serializable generate(final SharedSessionContractImplementor session, 
                                 final Object entity)
           throws HibernateException
    {
        // just return the nanotime for now
        return System.nanoTime();
    }
}
