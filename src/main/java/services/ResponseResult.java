package services;

import org.json.JSONObject;

/**
 * Created by hadoop on 17-11-27.
 */
public class ResponseResult {
    private final long code;

    private final String error;
    private final String message;
    private final AppResult app;
    public ResponseResult(long id, String content, AppResult app) {
        this.code = id;
        this.message = content;
        this.app = app;
        if(id == 0x1400){
            this.error = "请求参数错误";
        }else if(id == 0x2000){
            this.error = "解析内部出错，请查看日志";
        }else{
            this.error = "";
        }
    }
    public String getError() {
        return error;
    }
    public long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public AppResult getApp() {
        return app;
    }

}
