package pt.tecnico.distledger.server.domain.account;

public class Account {

    private Integer money;
    private String Id;

    public Account (String Id, Integer money) {
        setMoney(money);
        setId(Id);
    }

    public void setMoney (Integer money) {
        this.money = money;
    }

    public Integer getMoney () {
        return this.money;
    }

    public void setId (String Id) {
        this.Id = Id;
    }

    public String getId () {
        return this.Id;
    }
}
