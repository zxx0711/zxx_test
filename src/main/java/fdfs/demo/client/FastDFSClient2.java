package fdfs.demo.client;

import org.apache.commons.lang.StringUtils;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ProtoCommon;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FastDFS java api 文件上传下载主类
 *
 * @author zxx
 * @Date 2020/10/20 15:26
 */
public class FastDFSClient2 {
    /**
     * 路径分隔符
     */
    public static final String SEPARATOR = "/";
    /**
     * Point
     */
    public static final String POINT = ".";
    /**
     * 文件名称Key
     */
    private static final String FILENAME = "fileName";
    /**
     * 文件最大的大小
     */
    private int maxFileSize = 100 * 1000 * 1000;

    /**
     * org.slf4j.Logger
     */
    private static Logger logger = LoggerFactory.getLogger(FastDFSClient2.class);


    /**
     * MultipartFile 上传文件
     *
     * @param file MultipartFile
     * @return 返回上传成功后的文件路径
     */
    public String uploadFileWithMultipart(MultipartFile file) throws FastDFSException {
        return upload(file, null);
    }

    public String upload(MultipartFile file, Map<String, String> descriptions) throws FastDFSException {
        if (file == null || file.isEmpty()) {
            throw new FastDFSException(ErrorCode.FILE_ISNULL.CODE,ErrorCode.FILE_ISNULL.MESSAGE);
        }
        String path = null;
        try {
            path = upload(file.getInputStream(), file.getOriginalFilename(), descriptions);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FastDFSException(ErrorCode.FILE_ISNULL.CODE, ErrorCode.FILE_ISNULL.MESSAGE);
        }
        return path;
    }

    /**
     * 上传通用方法
     *
     * @param is 文件输入流
     * @param filename 文件名
     * @param descriptions 文件描述信息
     * @return 组名+文件路径，如：group1/M00/00/00/wKgz6lnduTeAMdrcAAEoRmXZPp870.jpeg
     * @throws FastDFSException
     */
    public String upload(InputStream is, String filename, Map<String, String> descriptions) throws FastDFSException {
        if(is == null){
            throw new FastDFSException(ErrorCode.FILE_ISNULL.CODE, ErrorCode.FILE_ISNULL.MESSAGE);
        }

        try {
            if(is.available() > maxFileSize){
                throw new FastDFSException(ErrorCode.FILE_OUT_SIZE.CODE, ErrorCode.FILE_OUT_SIZE.MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        filename = toLocal(filename);
        // 返回路径
        String path = null;
        // 文件描述
        NameValuePair[] nvps = null;
        List<NameValuePair> nvpsList = new ArrayList<>();
        // 文件名后缀
        String suffix = getFilenameSuffix(filename);

        // 文件名
        if (StringUtils.isNotBlank(filename)) {
            nvpsList.add(new NameValuePair(FILENAME, filename));
        }
        // 描述信息
        if (descriptions != null && descriptions.size() > 0) {
            descriptions.forEach((key, value) -> {
                nvpsList.add(new NameValuePair(key, value));
            });
        }
        if (nvpsList.size() > 0) {
            nvps = new NameValuePair[nvpsList.size()];
            nvpsList.toArray(nvps);
        }

        TrackerServer trackerServer = TrackerServerPool.borrowObject();
        StorageClient1 storageClient = new StorageClient1(trackerServer, null);
        try {
            // 读取流
            byte[] fileBuff = new byte[is.available()];
            is.read(fileBuff, 0, fileBuff.length);

            // 上传
            path = storageClient.upload_file1(fileBuff, suffix, nvps);

            if(StringUtils.isBlank(path)) {
                throw new FastDFSException(ErrorCode.FILE_UPLOAD_FAILED.CODE, ErrorCode.FILE_UPLOAD_FAILED.MESSAGE);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("upload file success, return path is {}", path);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new FastDFSException(ErrorCode.FILE_UPLOAD_FAILED.CODE, ErrorCode.FILE_UPLOAD_FAILED.MESSAGE);
        } catch (MyException e) {
            e.printStackTrace();
            throw new FastDFSException(ErrorCode.FILE_UPLOAD_FAILED.CODE, ErrorCode.FILE_UPLOAD_FAILED.MESSAGE);
        } finally {
            // 关闭流
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // 返还对象
        TrackerServerPool.returnObject(trackerServer);

        return path;
    }

    /**
     * 获取文件名称的后缀
     *
     * @param filename 文件名 或 文件路径
     * @return 文件后缀
     */
    public static String getFilenameSuffix(String filename) {
        String suffix = null;
        String originalFilename = filename;
        if (StringUtils.isNotBlank(filename)) {
            if (filename.contains(SEPARATOR)) {
                filename = filename.substring(filename.lastIndexOf(SEPARATOR) + 1);
            }
            if (filename.contains(POINT)) {
                suffix = filename.substring(filename.lastIndexOf(POINT) + 1);
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("filename error without suffix : {}", originalFilename);
                }
            }
        }
        return suffix;
    }
    /**
     * 转换路径中的 '\' 为 '/' <br>
     * 并把文件后缀转为小写
     *
     * @param path 路径
     * @return
     */
    public static String toLocal(String path) {
        if (StringUtils.isNotBlank(path)) {
            path = path.replaceAll("\\\\", SEPARATOR);

            if (path.contains(POINT)) {
                String pre = path.substring(0, path.lastIndexOf(POINT) + 1);
                String suffix = path.substring(path.lastIndexOf(POINT) + 1).toLowerCase();
                path = pre + suffix;
            }
        }
        return path;
    }

    /**
     * 获取访问服务器的token，拼接到地址后面
     *
     * @param filepath 文件路径 group1/M00/00/00/wKgzgFnkTPyAIAUGAAEoRmXZPp876.jpeg
     * @param httpSecretKey 秘钥
     * @return 返回token，如： token=078d370098b03e9020b82c829c205e1f&ts=1508141521
     */
    public static String getToken(String filepath, String httpSecretKey){
        // unix seconds
        int ts = (int) Instant.now().getEpochSecond();
        // token
        String token = "null";
        try {
            token = ProtoCommon.getToken(getFilename(filepath), ts, httpSecretKey);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("token=").append(token);
        sb.append("&ts=").append(ts);

        return sb.toString();
    }
    /**
     * 获取FastDFS文件的名称，如：M00/00/00/wKgzgFnkTPyAIAUGAAEoRmXZPp876.jpeg
     *
     * @param fileId 包含组名和文件名，如：group1/M00/00/00/wKgzgFnkTPyAIAUGAAEoRmXZPp876.jpeg
     * @return FastDFS 返回的文件名：M00/00/00/wKgzgFnkTPyAIAUGAAEoRmXZPp876.jpeg
     */
    public static String getFilename(String fileId){
        String[] results = new String[2];
        StorageClient1.split_file_id(fileId, results);

        return results[1];
    }

}
