package my.com.maybank.jpa.datasource;

import java.lang.reflect.Method;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * Datasource proxy bean post processor that can be enabled by setting property 
 * {@code spring.jpa.show-sql=true}
 * @since 1.0.0
 * @see <a href="https://www.baeldung.com/jpa-hibernate-batch-insert-update#2-tracing-sql-queries">2.2.  Tracing SQL Queries</a>
 * @see <a href="https://github.com/ttddyy/datasource-proxy-examples/blob/master/springboot-autoconfig-example/src/main/java/net/ttddyy/dsproxy/example/DatasourceProxyBeanPostProcessor.java">springboot-autoconfig-example/src/main/java/net/ttddyy/dsproxy/example/DatasourceProxyBeanPostProcessor.java</a>
 * @author ChristopherCKW
 */
@Slf4j
@Accessors(
    fluent = false,
    chain = true
)
@Getter
@Setter
@ConditionalOnProperty(
    name="spring.jpa.show-sql",
    havingValue = "true"
)
@Component
public class DatasourceTracerProxyBeanPostProcessor
       implements BeanPostProcessor
{
    @Override
    public Object postProcessBeforeInitialization(final Object bean, 
                                                  final String beanName)
    {
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(final Object bean, 
                                                 final String beanName)
    {
        if(bean instanceof DataSource source && !(bean instanceof ProxyDataSource))
        {
            // Instead of directly returning a less specific datasource bean
            // (e.g.: HikariDataSource -> DataSource), return a proxy object.
            // See following links for why:
            //   https://stackoverflow.com/questions/44237787/how-to-use-user-defined-database-proxy-in-datajpatest
            //   https://gitter.im/spring-projects/spring-boot?at=5983602d2723db8d5e70a904
            //   https://arnoldgalovics.com/configuring-a-datasource-proxy-in-spring-boot/
            final ProxyFactory factory = new ProxyFactory(
                bean
            );
            factory.setProxyTargetClass(
                true
            );
            factory.addAdvice(
                new ProxyDataSourceInterceptor(
                    source
                )
            );
            log.warn(
                "Datasource tracer proxy ENABLED\n\tNOTE!!! NOT RECOMMENDED for production, set property to \'spring.jpa.show-sql=false\'"
            );
            return factory.getProxy();
        }
        return bean;
    }

    private static class ProxyDataSourceInterceptor 
            implements MethodInterceptor 
    {
        private final DataSource dataSource;

        public ProxyDataSourceInterceptor(final DataSource dataSource) {
            this.dataSource = ProxyDataSourceBuilder.create(
                dataSource
            ).name(
                "Datasource-Tracer-Logger"
            ).multiline().logQueryBySlf4j(
                SLF4JLogLevel.INFO
            ).build();
        }

        @Override
        public Object invoke(final MethodInvocation invocation) throws Throwable {
            final Method proxyMethod = ReflectionUtils.findMethod(
                this.dataSource.getClass(),
                invocation.getMethod().getName()
            );
            if(proxyMethod != null)
            {
                return proxyMethod.invoke(
                    this.dataSource, 
                    invocation.getArguments()
                );
            }
            return invocation.proceed();
        }
    }
}
