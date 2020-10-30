package fdfs.demo.client;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>Description: FastDFS文件上传下载工具类 </p>
 * <p>Copyright: Copyright (c) 2016</p>
 *
 * @author yangxin
 * @version 1.0
 * @date 2016/10/19
 */
public class FastDFSClient {
    /**
     * TrackerServer 配置文件路径
     */
    private static final String FASTDFS_CONFIG_PATH = "fdfs_client.conf";

//    private static final String CONFIG_FILENAME = "target/fdfs_client.conf";

    private static StorageClient1 storageClient1 = null;

    //

    /**
     * 初始化FastDFS Client
     * 通过trackerServer取得某一个可用的Storage Server的地址并用其实例化一个StorageClient1对象。这样就完成了FastDFS的客户端调用上传、下载、删除等所有操作的前期建立连接的工作。
     */
    static {
        try {
            ClientGlobal.init(FASTDFS_CONFIG_PATH);
            TrackerClient trackerClient = new TrackerClient(ClientGlobal.g_tracker_group);
            TrackerServer trackerServer = trackerClient.getConnection();
            if (trackerServer == null) {
                throw new IllegalStateException("getConnection return null");
            }

            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            if (storageServer == null) {
                throw new IllegalStateException("getStoreStorage return null");
            }

            storageClient1 = new StorageClient1(trackerServer, storageServer);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传文件
     *
     * @param file     文件对象
     * @param fileName 文件名
     * @return
     */
    public static String uploadFile(File file, String fileName) {
        return uploadFile(file, fileName, null);
    }

    /**
     * 上传文件
     *
     * @param file     文件对象
     * @param fileName 文件名
     * @param metaList 文件元数据
     * @return
     */
    public static String uploadFile(File file, String fileName, Map<String, String> metaList) {
        try {
            byte[] buff = IOUtils.toByteArray(new FileInputStream(file));
            NameValuePair[] nameValuePairs = null;
//            if (metaList != null) {
//                nameValuePairs = new NameValuePair[metaList.size()];
//                int index = 0;
//                for (Iterator<Map.Entry<String, String>> iterator = metaList.entrySet().iterator(); iterator.hasNext(); ) {
//                    Map.Entry<String, String> entry = iterator.next();
//                    String name = entry.getKey();
//                    String value = entry.getValue();
//                    nameValuePairs[index++] = new NameValuePair(name, value);
//                }
//            }
            //获得文件后缀
            String extName = FilenameUtils.getExtension(fileName);
            return storageClient1.upload_file1(buff, extName, nameValuePairs);
//            return storageClient1.upload_file1(buff,FileUtils.getExtension(fileName),nameValuePairs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 上传文件
     *
     * @param file     文件对象
     * @return
     */
    public static String upload(MultipartFile file) {
        try {
            byte[] fileBuff = file.getBytes();
            String tempFileName = file.getOriginalFilename();
            String fileExtName = tempFileName.substring(tempFileName.lastIndexOf("."));
            //设置元信息
            NameValuePair[] metaList = new NameValuePair[3];
            metaList[0] = new NameValuePair("fileName", tempFileName);
            metaList[1] = new NameValuePair("fileExtName", fileExtName);
            metaList[2] = new NameValuePair("fileLength", String.valueOf(file.getSize()));
            //获得文件后缀:txt/doc
            String extName = FilenameUtils.getExtension(tempFileName);
            return storageClient1.upload_file1(fileBuff, extName, metaList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取文件元数据
     *
     * @param fileId 文件ID
     * @return
     */
    public static Map<String, String> getFileMetadata(String fileId) {
        try {
            NameValuePair[] metaList = storageClient1.get_metadata1(fileId);
            if (metaList != null) {
                HashMap<String, String> map = new HashMap<String, String>();
                for (NameValuePair metaItem : metaList) {
                    map.put(metaItem.getName(), metaItem.getValue());
                }
                return map;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @return 删除失败返回-1，否则返回0
     */
    public static int deleteFile(String fileId) {
        try {
            return storageClient1.delete_file1(fileId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 下载文件
     *
     * @param fileId  文件ID（上传文件成功后返回的ID）
     * @param outFile 文件下载保存位置
     * @return
     */
//    public static int downloadFile(String fileId, File outFile,StorageClient1 storageClient1) {
    public static int downloadFile(String fileId, File outFile) {
//        FileOutputStream fos = null;
        OutputStream fos = null;
        try {
            byte[] content = storageClient1.download_file1(fileId);
            InputStream input = new ByteArrayInputStream(content);
//            fos = new FileOutputStream(outFile);
            fos = new FileOutputStream(outFile);
            IOUtils.copy(input, fos);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

}