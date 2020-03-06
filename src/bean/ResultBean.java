
package bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultBean<T> implements Serializable {
    private int status; // 状态码：0正常，-1失败

    private String msg; // 返回消息：成功还是失败

    private T data; // 返回的对象。有时候我们要返回一些数据就放在这个里，如果不需要返回数据则是null
    public ResultBean(){
        this.status = 0;

        this.msg = "";
    }
    public ResultBean(T data) {
        this.status = 0;

        this.msg = "";

        this.data = data;
    }

    private ResultBean(int status, String msg, T data) {

        super();

        this.status = status;

        this.msg = msg;

        this.data = data;
    }

    /**
     * 对返回值的封装
     *
     * @param status
     * @param msg
     * @param object
     * @return
     */
    public  ResultBean result(int status, String msg, T object) {
        return new ResultBean(status, msg, object);
    }
    public   ResultBean resultOnly(T object) {
        return new ResultBean(object);
    }

    public void setProperties(int status, String msg, T object){
        this.status = status;

        this.msg = msg;

        this.data = object;
    }
}