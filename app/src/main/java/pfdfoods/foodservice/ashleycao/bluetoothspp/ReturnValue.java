package pfdfoods.foodservice.ashleycao.bluetoothspp;

public class ReturnValue<T> {
    public String typeName;
    public T object;

    public ReturnValue(String typeName, T object){
        this.typeName = typeName;
        this.object = object;
    }

}
