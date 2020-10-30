package fdfs.demo.controller;

import fdfs.demo.client.ResponseData;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


/**
 * 上传文件测试
 *
 * @author zxx
 * @Date 2020/10/20 13:53
 */
//@RestController
@Controller
@RequestMapping("/fastDFS")
public class FileTestController {

    /**
     * 上传文件页面
     *
     * @return
     */
    @RequestMapping("index")
    public String index() {
        return "index";
    }

    /**
     * 上传文件
     *
     * @param file
     * @return
     */
    @RequestMapping("upload")
    @ResponseBody
    public Object upload(MultipartFile file) {
        ResponseData responseData = new ResponseData();
        try {
            if (file.isEmpty()) {
//                return ApiReturnUtil.error("空上传");
            } else {
                //blog.csdn.net/moshowgame
                String conf_filename = this.getClass().getClassLoader().getResource("fdfs_client.conf").getPath().replaceAll("%20", " ");
                System.out.println(conf_filename);
                String tempFileName = file.getOriginalFilename();
                //fastDFS方式
                ClientGlobal.init(conf_filename);

                byte[] fileBuff = file.getBytes();
                String fileId = "";
                String fileExtName = tempFileName.substring(tempFileName.lastIndexOf("."));

                //建立连接
                TrackerClient tracker = new TrackerClient();
                TrackerServer trackerServer = tracker.getConnection();
                StorageServer storageServer = tracker.getStoreStorage(trackerServer);
                StorageClient1 client = new StorageClient1(trackerServer, storageServer);

                //设置元信息
                NameValuePair[] metaList = new NameValuePair[3];
                metaList[0] = new NameValuePair("fileName", tempFileName);
                metaList[1] = new NameValuePair("fileExtName", fileExtName);
                metaList[2] = new NameValuePair("fileLength", String.valueOf(file.getSize()));

                //上传文件，获得fileId
                fileId = client.upload_file1(fileBuff, fileExtName, metaList);
                //TODO 这里可以追加一些业务代码，例如上传成功后保存到upload_file表，统一进行上传文件管理之类
                responseData.setSuccess(true);
                responseData.setMessage("文件上传成功" + fileId);
//                return responseData;
//                return ApiReturnUtil.success("文件上传成功",fileId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseData.setSuccess(false);
            responseData.setMessage(e.getMessage());
//            return ApiReturnUtil.error("上传错误："+e.getMessage());
        }
        return responseData;
    }

    /**
     * 下载文件
     * @param groupName
     * @param remoteFileName
     * @return
     * @throws IOException
     * @throws MyException
     */
    public static byte[] download(String groupName, String remoteFileName) throws IOException, MyException {
        //
        TrackerClient trackerClient = new TrackerClient();
        //
        TrackerServer trackerServer = trackerClient.getConnection();
        //
        StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
        //
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);

        byte[] bytes = storageClient.download_file(groupName, remoteFileName);

        return bytes;
    }
    @RequestMapping("test")
    @ResponseBody
    public String test() {
//        return new ResponseData(true);
        return "hello ddqwss";
    }
    //    public ResponseData test(){
//        return new ResponseData(true);
////        return "hello";
//    }
}
