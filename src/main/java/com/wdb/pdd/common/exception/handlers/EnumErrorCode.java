package com.wdb.pdd.common.exception.handlers;

/**
 * <pre>
 * 全局异常码
 * </pre>
 * <small> 2018年4月5日 |  </small>
 */
public enum EnumErrorCode {


    ok(0, "请求成功")
    , unknowFail(500, "未知错误")
    , pageNotFound(404, "页面不存在")
    , notAuthorization(405, "未授权")
    , paramsFail(50000,"参数错误")
    , duplicateKeyExist(40000, "记录已存在")
    , genReadConfigError(40100, "代码生成器获取配置文件失败")
    , genWriteConfigError(40101, "代码生成器修改配置文件失败")
    , genRenderTemplateError(40102, "代码生成器渲染模板失败")
    , FileUploadGetBytesError(40200, "文件上传错误")
    , FileUploadError(40201, "文件上传错误")
    , userUpdatePwd4adminNotAllowed(40402, "超级管理员的账号不允许直接重置！")
    , apiAuthorizationSignFailed(44000, "token生成失败")
    , apiAuthorizationInvalid(44001, "token不合法")
    , apiAuthorizationLoggedout(44002, "token已注销")
    , apiAuthorizationExpired(44003, "token已过期")
    //以下为真正目前用到的错误码
    , apiAuthorizationFailed(44004, "token认证失败")
    , reguserNull(44005, "用户不存在")//不用再去请求获取新token
    , tokenNull(44006, "token不存在")
    , backing(43000, "后台运行中,不要重复点击")
    ;

    private int code;
    private String msg;

    EnumErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getCodeStr() {
        return code + "";
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public EnumErrorCode setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public static String getMsgByCode(int code) {
        EnumErrorCode[] values = EnumErrorCode.values();
        for (EnumErrorCode ec : values) {
            if (ec.code == code) {
                return ec.msg;
            }
        }
        return "";
    }

    /**
     *
     * <pre>
     * </pre>
     *
     * <small>   | 2017-09-05</small>
     *
     * @param args
     */
    public static void main(String[] args) {
        String msg = EnumErrorCode.getMsgByCode(EnumErrorCode.unknowFail.code);
        System.out.println(msg);
    }



}
