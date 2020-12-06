package group.zeus.demo.api;

import java.io.Serializable;

/**
 * @Author: maodazhan
 * @Date: 2020/11/9 10:32
 */
public class Person implements Serializable {

    private static final long serialVersionUID = -8971819717377750566L;
    private String firstName;
    private String lastName;

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
