package group.zeus.spi;


import com.sun.istack.internal.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated interface is a SPI Extension Interface
 * @Author: maodazhan
 * @Date: 2020/10/7 13:45
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SPI {

    /**
     * @return the suggested SPI extension name, cannot be null
     */
    @NotNull
    String value();
}
