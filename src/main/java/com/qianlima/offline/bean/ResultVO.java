package com.qianlima.offline.bean;

public class ResultVO<T> {
    /**
     * code值 参考ResultCodeEnum
     */
    private String code;
    /**
     * 响应信息
     */
    private String msg;
    /**
     * 响应结果
     */
    private T data;

    public ResultVO(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResultVO(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
