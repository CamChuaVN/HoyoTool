package camchua.genshinimpacttool.dailycheckin.model;

public class CheckIn {

    private String name;
    private int amount;
    private String icon;

    private boolean success;
    private String msg;
    private int code;

    private CheckIn(String name, int amount, String icon, boolean success, String msg, int code) {
        this.name = name;
        this.amount = amount;
        this.icon = icon;
        this.success = success;
        this.msg = msg;
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public int getAmount() {
        return this.amount;
    }

    public String getIconUrl() {
        return this.icon;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public String getMessage() {
        return this.msg;
    }

    public int getCode() {
        return this.code;
    }

}
