package fdfs.demo.controller;


import fdfs.demo.client.FastDFSClient;
import fdfs.demo.client.ResponseData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * FastDFS测试
 */
@Controller
@RequestMapping("/fdfs")
public class FastDFSClientTest {

    /**
     * 上传文件页面
     * @return
     */
    @RequestMapping("index")
    public String index() {
        return "index";
    }

    /**
     * 文件上传测试
     */
    @RequestMapping("upload")
    @ResponseBody
    public Object testUpload(MultipartFile file) {
        ResponseData responseData = new ResponseData();
        try {
            if (file.isEmpty()) {
//                return ApiReturnUtil.error("空上传");
            } else {
                //上传文件，获得fileId
                String fileId = FastDFSClient.upload(file);
                System.out.println("upload local file " + file.getOriginalFilename() + " ok, fileid=" + fileId);
                //TODO 这里可以追加一些业务代码，例如上传成功后保存到upload_file表，统一进行上传文件管理之类
                responseData.setSuccess(true);
                responseData.setMessage("文件上传成功" + fileId);
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
     * 文件下载测试
     */
    @RequestMapping("dowload")
    @ResponseBody
    public Object testDownload() {
        ResponseData fileResponseData=new ResponseData();
        try{
            int r = FastDFSClient.downloadFile("group1/M00/00/00/wKiIgF-H7YqASV7KAAAQgElpT6I672.txt-m", new File("src/main/resources/download/DownloadFile1.jpg"));
            System.out.println(r == 0 ? "下载成功" : "下载失败");
            fileResponseData.setSuccess(r == 0 ? true : false);
            fileResponseData.setMessage(r == 0 ? "下载成功" : "下载失败");

        }catch(Exception e){
            fileResponseData.setSuccess(false);
            fileResponseData.setMessage(e.getMessage());
        }
        return fileResponseData;

    }

    /**
     * 获取文件元数据测试
     */
//    @Test
//    public void testGetFileMetadata() {
//        Map<String,String> metaList = FastDFSClient.getFileMetadata("group1/M00/00/00/wKgAyVgFk9aAB8hwAA-8Q6_7tHw351.jpg");
//        for (Iterator<Map.Entry<String,String>>  iterator = metaList.entrySet().iterator(); iterator.hasNext();) {
//            Map.Entry<String,String> entry = iterator.next();
//            String name = entry.getKey();
//            String value = entry.getValue();
//            System.out.println(name + " = " + value );
//        }
//    }

    /**
     * 文件删除测试
     */
    @RequestMapping("delete")
    @ResponseBody
    public Object testDelete() {
        ResponseData fileResponseData=new ResponseData();
        int r = FastDFSClient.deleteFile("group1/M00/00/00/wKiIgF-SdfuABI4tAADLewy_5ac07..jpg");
        System.out.println(r == 0 ? "删除成功" : "删除失败");
        fileResponseData.setSuccess(r == 0 ? true : false);
        fileResponseData.setMessage(r == 0 ? "下载成功" : "下载失败");
        return fileResponseData;
    }

    /**
     * 文件上传测试
     */
    @RequestMapping("upload1")
    public void testUpload1() {
        File file = new File("C:\\Users\\yangfang\\Pictures\\angularjs_share.jpg");
        Map<String,String> metaList = new HashMap<String, String>();
        metaList.put("width","1024");
        metaList.put("height","768");
        metaList.put("author","杨信");
        metaList.put("date","20161018");
        String fid = FastDFSClient.uploadFile(file,file.getName(),metaList);
        System.out.println("upload local file " + file.getPath() + " ok, fileid=" + fid);
        //上传成功返回的文件ID： group1/M00/00/00/wKgAyVgFk9aAB8hwAA-8Q6_7tHw351.jpg
    }
}