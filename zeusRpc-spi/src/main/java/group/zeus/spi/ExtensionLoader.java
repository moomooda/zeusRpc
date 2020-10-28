package group.zeus.spi;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * @Author: maodazhan
 * @Date: 2020/10/7 13:20
 */
public class ExtensionLoader<T> {

    private static final String SPI_DIRECTORY = "META-INF/SPI/";

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

    // Key: Extension interface class Value: ExtensionLoader class
    private static final ConcurrentHashMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    // Key: Extension name Value: Extension implementation instance
    private static final ConcurrentHashMap<String, Holder<Object>> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    //-------------------------------

    private final ConcurrentMap<String, IllegalStateException> exceptions = new ConcurrentHashMap<>();

    // Map<String,Class<?>> Key： Extension name Value : Extension implementation class
    private final Holder<Map<String, Class<?>>> cached_classes = new Holder<>();

    private String cachedDefaultName;

    private Holder<Object> cachedDefaultExtension = new Holder<>();

    private Class<T> type;

    private ExtensionLoader(Class<T> type) {
        this.type = type;
    }

    /**
     * ExtensionLoader 静态方法
     * get ExtensionLoader instance according to Extension interface class type
     * Extension interface class type demands:
     * 1. 非空
     * 2. 接口
     * 3. with 注解
     *
     * @param type
     * @param <T>
     * @return
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension class type Cannot be null");
        } else if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension class type is not interface");
        } else if (!type.isAnnotationPresent(SPI.class)) {
            throw new IllegalArgumentException("Extension class type(" + type + ") must With @" + SPI.class.getName() + "" +
                    "Annotation");
        }
        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            // TODO 并发可能会创建多个对象
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<>(type));
            extensionLoader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    /**
     * ExtensionLoader  实例方法
     *
     * @param name expected Extension implementation instance's name
     * @return Extension implementation instance
     */
    public T getExtension(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Extension name Cannot be null");
        }
        // TODO 小写
        name = name.toLowerCase();
        if ("default".equals(name)) {
            return getDefaultExtension();
        } else if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Extension name illegal");
        }
        Holder holder = EXTENSION_INSTANCES.get(name);
        if (holder == null) {
            // TODO 可能会创建多个对象
            EXTENSION_INSTANCES.putIfAbsent(name, new Holder<>());
            holder = EXTENSION_INSTANCES.get(name);
        }
        Object extension = holder.getValue();
        // DCL SingleTon
        if (extension == null) {
            synchronized (holder) {
                if (extension == null) {
                    extension = createExtension(name);
                    holder.setValue(extension);
                }
            }
        }

        return (T) extension;
    }

    public T getDefaultExtension() {
        T instance = (T) cachedDefaultExtension.getValue();
        if (instance == null) {
            synchronized (cachedDefaultExtension) {
                if (instance == null) {
                    instance = createExtension(getCachedDefaultName());
                    cachedDefaultExtension.setValue(instance);
                }
            }
        }
        return instance;
    }

    /**
     * create Extension implementation instance
     *
     * @param name expected Extension implementation instance's name
     * @return Extension implementation instance
     */
    public T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw findException(name);
        }
        try {
            T instance = (T) clazz.newInstance();
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                    type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }

    /**
     * @return Extension implementation class
     */
    public Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cached_classes.getValue();
        // DCL SingleTon
        if (classes == null) {
            synchronized (cached_classes) {
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cached_classes.setValue(classes);
                }
            }
        }
        return classes;
    }

    public Map<String, Class<?>> loadExtensionClasses() {
        Map<String, Class<?>> extensionClasses = new HashMap<>();
        loadFile(extensionClasses, SPI_DIRECTORY);
        return extensionClasses;
    }

    // 线程安全
    public String getCachedDefaultName() {
        if (cachedDefaultName == null) {
            final SPI defaultAnnotation = type.getAnnotation(SPI.class);
            if (StringUtils.isBlank(defaultAnnotation.value())) {
                throw new IllegalStateException("Extension class type(" + type + ") must With correct value of @SPI");
            }
            cachedDefaultName = defaultAnnotation.value().toLowerCase().trim();
            String[] names = NAME_SEPARATOR.split(cachedDefaultName);
            if (names.length > 1) {
                throw new IllegalStateException("more than 1 default extension name on extension " + type.getName());
            }
        }
        return cachedDefaultName;
    }

    // Load file,then append cache
    public void loadFile(Map<String, Class<?>> extensionClasses, String dir) {
        String fileName = dir + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                        try {
                            String line = null;
                            while ((line = bufferedReader.readLine()) != null) {
                                line = line.trim();
                                if (line.length() > 0) {
                                    try {
                                        String name = null;
                                        int i = line.indexOf('=');
                                        if (i > 0) {
                                            name = line.substring(0, i).trim();
                                            line = line.substring(i + 1).trim();
                                        }
                                        if (line.length() > 0) {
                                            Class<?> clazz = Class.forName(line, true, classLoader);
                                            if (!type.isAssignableFrom(clazz)) {
                                                throw new IllegalStateException("Error when load extension class(interface: " +
                                                        type + ", class line: " + clazz.getName() + "), class "
                                                        + clazz.getName() + "is not subtype of interface.");
                                            }
                                            extensionClasses.put(name, clazz);
                                        }
                                    } catch (Throwable t) {
                                        IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + url + ", cause: " + t.getMessage(), t);
                                        exceptions.put(line, e);
                                    }
                                }
                            }
                        } finally {
                            bufferedReader.close();
                        }
                    } catch (Throwable t) {
                        //
                    }
                }
            }
        } catch ( Throwable t) {
            //
        }
    }

    // get ExtensionLoader ClassLoader
    private static ClassLoader findClassLoader() {
        return ExtensionLoader.class.getClassLoader();
    }

    // get Exception hints
    public IllegalStateException findException(String name) {
        if (exceptions.containsKey(name.toLowerCase())) {
            return exceptions.get(name.toLowerCase());
        }
        StringBuilder buf = new StringBuilder("No such extension " + type.getName() + " by name " + name);

        int i = 1;
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (i == 1) {
                buf.append(", possible causes: ");
            }

            buf.append("\r\n(");
            buf.append(i++);
            buf.append(") ");
            buf.append(entry.getKey());
            buf.append(":\r\n");
            buf.append(entry.getValue().toString());
        }
        return new IllegalStateException(buf.toString());
    }
}
