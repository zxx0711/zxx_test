package fdfs.demo.client;

/**
 * FastDFS 文件上传下载可能出现
 * @author zxx
 * @Date 2020/10/20 15:38
 */
public class FastDFSException extends Exception{
    /**
     * 错误码
     */
    private String code;
    /**
     * 错误信息
     */
    private String message;

    public FastDFSException(){

    }

    public FastDFSException(String message, String code) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
