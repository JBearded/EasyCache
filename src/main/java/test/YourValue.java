package test;

/**
 * @author 谢俊权
 * @create 2016/7/25 16:39
 */
public class YourValue {

    private int id;
    private String name;

    public YourValue(){}
    public YourValue(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
