package fdfs.demo.controller;


import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB测试
 * @author zxx
 * @Date 2020/10/29 9:42
 */
@Controller
@RequestMapping("/mongoDB")
public class MongodbController {
    /**
     * 查询文档
     */
    @RequestMapping("queryAll")
    public void queryAll(){
        //1.创建连接
        MongoClient client =new MongoClient("localhost");
        //打开数据库
        MongoDatabase db=client.getDatabase("rundb");
        //3.获取集合
        MongoCollection<Document> collection=db.getCollection("rundb");
        //4.获得集合的文档
//        FindIterable<Document> documents=collection.find();
//        MongoCursor<Document> mongoCursor=documents.iterator();
//        //循环遍历
//        while (mongoCursor.hasNext()){
//            System.out.print(mongoCursor.next());
//        }

        //查询第一个文档
        Document document=collection.find().first();
        System.out.println(document.toJson());
        client.close();
    }

    /**
     * 更新文档
     */
    @RequestMapping("update")
    public void update(){
        //连接服务
        MongoClient mongoClient=new MongoClient("localhost");
        //打开数据库
        MongoDatabase db=mongoClient.getDatabase("rundb");
        //获得集合
        MongoCollection<Document> collection=db.getCollection("rundb");
        //更新文档，将文档中likes为100，修改为300
        collection.updateMany(Filters.eq("likes",100),new Document("$set",new Document("likes",300)));
        FindIterable<Document> iterable=collection.find();
        MongoCursor<Document> cursor=iterable.iterator();
        while(cursor.hasNext()){
            System.out.println(cursor.next());
        }
        mongoClient.close();
    }

    /**
     * 删除文档
     */
    @RequestMapping("delete")
    public void delete(){
        //连接服务
        MongoClient mongoClient=new MongoClient("localhost");
        //打开数据库
        MongoDatabase db=mongoClient.getDatabase("rundb");
        //获得集合
        MongoCollection<Document> collection=db.getCollection("rundb");
        //删除likes为200的文档
        collection.deleteOne(Filters.eq("titile","java批量插入测试1"));
        //获得文档
        FindIterable<Document> iterable=collection.find();
        MongoCursor<Document> cursor=iterable.iterator();
        while (cursor.hasNext()){
            System.out.println(cursor.next());
        }
        mongoClient.close();
    }

    /**
     * 插入数据
     */
    @RequestMapping("insert")
    public void insert(){
        //连接服务
        MongoClient mongoClient=new MongoClient("localhost");
        //打开数据库
        MongoDatabase db=mongoClient.getDatabase("rundb");
        //获得集合
        MongoCollection<Document> collection=db.getCollection("rundb");
        //要插入的数据
        Document document=new Document("title","java测试1").append("likes",400);
        collection.insertOne(document);
        //查询文档
        FindIterable<Document> iterable=collection.find();
        MongoCursor<Document> cursor=iterable.iterator();
        while (cursor.hasNext()){
            System.out.println(cursor.next());
        }

    }

    /**
     * 批量插入
     */
    @RequestMapping("batInsert")
    public void batInsert(){
        //连接服务
        MongoClient mongoClient=new MongoClient("localhost");
        //启动数据库
        MongoDatabase db =mongoClient.getDatabase("rundb");
        //获得集合
        MongoCollection<Document> collection=db.getCollection("rundb");
        //批量插入
        List<Document> list=new ArrayList<>();
        list.add(new Document("title","java批量插入测试1").append("likes",120));
        list.add(new Document("title","java批量插入测试2").append("likes",130));
        collection.insertMany(list);//insertMany 3.2版本新添加的功能
        //获得文档
        FindIterable<Document> iterable=collection.find();
        MongoCursor<Document> cursor=iterable.iterator();
        while(cursor.hasNext()){
            System.out.println(cursor.next());
        }

    }





}
