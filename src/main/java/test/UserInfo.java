package test;

/**
 * @author 谢俊权
 * @create 2016/7/28 10:00
 */
public class UserInfo {

    private int id;
    private String name;
    private String pword;

    public UserInfo(int id, String pword) {
        this.id = id;
        this.pword = pword;
    }

    public UserInfo(int id, String name, String pword) {
        this.id = id;
        this.name = name;
        this.pword = pword;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPword() {
        return pword;
    }
}
