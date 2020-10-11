package group.zeus.spi;

/**
 * 借鉴 dubbo Helper Class for hold a value
 * @Author: maodazhan
 * @Date: 2020/10/7 18:52
 */
public class Holder<T> {

    private volatile T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
