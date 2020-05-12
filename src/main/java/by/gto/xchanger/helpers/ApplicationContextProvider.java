package by.gto.xchanger.helpers;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Utility bean for retrieving spring's application context must be defined in
 * spring bean config file as: <bean id="applicationContextProvider" class=
 * "by.gto.office.bto.helpers.ApplicationContextProvider" />
 *
 * @author timo
 */

public class ApplicationContextProvider implements ApplicationContextAware {
    public static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        applicationContext = ctx;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

}
