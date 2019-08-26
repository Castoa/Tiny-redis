package www.betich;

import java.util.*;

public class Database {
    //每个命令共享有一份数据，所以建了一个数据库
    //为什么用单例模式？因为只有一个对象，因为所有的命令用的是一个对象
    private static Database instance=new Database();
    public static Database getInstance(){
        return instance;
    }
    //hash类型
    private static Map<String,Map<String,String>> hashes=new HashMap<>();
    //list
    private static Map<String,List<String>>  lists=new HashMap<>();
    public static Map<String,String> getHashes(String key){
        Map<String,String> hash=hashes.get(key);
        if (hash==null){
            hash=new HashMap<>();
            hashes.put(key,hash);
        }
        return hash;
    }
    public static List<String> getList(String key){
        List<String> list=lists.get(key);
        //保证list一定有内容
        if (list==null){
            list=new ArrayList<>();
            lists.put(key,list);
        }
        return list;
    }
}

