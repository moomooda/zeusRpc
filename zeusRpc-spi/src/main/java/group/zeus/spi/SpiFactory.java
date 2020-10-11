package group.zeus.spi;

/**
 * @Author: maodazhan
 * @Date: 2020/10/7 13:07
 */
public class SpiFactory<T> {

    /**
     *  get Extension instance  via class type  and  name of Extension
     * @param type Extension class
     * @param name Extension name
     * @param <T>
     * @return
     */
    public static <T> T getExtension(Class<T> type, String name){ return ExtensionLoader.getExtensionLoader(type).getExtension(name);}
}
