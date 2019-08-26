package www.betich;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import www.betich.commands.LPUSHCommand;
import www.betich.exceptions.RemoteException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Protocol {
    private static final Logger logger = LoggerFactory.getLogger(LPUSHCommand.class);

    public static Object read(InputStream is) throws IOException, RemoteException {
        return process(is);
    }

    //表示此时需要读
    //如果是false，b就不需要再读了，直接判断c就可以
    //如果是true，则说明上次没有读到两次\r,直接到b读下一次
    //simpleString  和error
    public static String readLine(InputStream is) throws IOException {
        boolean needRead=true;
        StringBuilder sb=new StringBuilder();
        int b=-1;
        //"+OK\r\r\n"
        while(true){
            if (needRead==true) {
                b = is.read();
                if (b == -1) {
                    throw new RuntimeException("不应该读到结尾的");
                }
            }else{
                needRead=false;
            }
            if (b=='\r'){
                int c=is.read();
                if (c==-1){
                    throw new RuntimeException("不应该读到结尾的");
                }
                if (c=='\n'){
                    break;
                }
                if (c=='\r'){
                    sb.append((char)c);
                    b=c;
                    needRead=false;
                }else{
                    sb.append((char)b);
                    sb.append((char)c);
                }
            }else{
                sb.append((char)b);
            }
        }
        return sb.toString();
    }
    public static long readInteger(InputStream is) throws IOException {
        StringBuilder sb=new StringBuilder();
        boolean isNagive=false;
        int b=is.read();
        if (b==-1){
            throw new RuntimeException("不应该读到结尾的");
        }
        //说明此处是否是负数
        if (b=='-'){
            isNagive=true;
        }else{
            sb.append((char)b);
        }
        while(true){
            b=is.read();
            if (b==-1){
                throw new RuntimeException("不应该读到结尾的");
            }
            if (b=='\r'){
                int c=is.read();
                if (c==-1){
                    throw new RuntimeException("不应该读到结尾的");
                }
                if (c=='\n'){
                    break;
                }
                throw new RuntimeException("没有读到");
            }else{
                sb.append((char)b);
            }
        }
        long v=Long.parseLong(sb.toString());
        if (isNagive){
            v=-v;
        }
        return v;
    }


    public static Command readCommand(InputStream is) throws Exception {
        //所有的命令都是Array类型
        //读一个命令，传过来的每一个命令都是List
        Object o=read(is);
        if (!(o instanceof List)){
            throw new Exception("命令必须是Array类型");
        }
        //如果不是List类型，就将其强转，并且命令的长度大于1
        List<Object> list=(List<Object>)o;
        if (list.size()<=1){
            throw new Exception("元素命令必须大于1");
        }
        //希望接收的命令是byte
        Object o2=list.remove(0);
        if (!(o2 instanceof byte[])){
            throw new Exception("错误的命令类型");
        }
        //将其强转为byte类型
        byte[] array=(byte[])o2;
        //将byte数组变为String类型
        String commandName=new String(array);
        //因为在Redis里面命令是不分大小写的。所以将其全部转为大写。
        String className=String.format("www.betich.commands.%sCommand",commandName.toUpperCase());
        //Lpush,有了一个命令的名字，则利用反射
        //String commandName==>(一是基于配置，二是约定俗称，即就是规矩)className全名称==》
        //得到LPush的Class类对象cls
        Class<?> cls=Class.forName(className);
        //如果Class类对象不属于这个接口子类或者实现，就是错误的命令。
        if (!Command.class.isAssignableFrom(cls)){
            throw new Exception("错误的命令");
        }
        //通过反射实例化对象
        Command command=(Command)cls.newInstance();
        logger.debug(list.toString());
        //把剩下的参数传进去,因为第一个参数时命令已经处理过了。所以剩下的只是一些参数的处理。
        command.setArgs(list);
        return command;
    }


    private static String processSimpleString(InputStream is) throws IOException {
        return is.toString();
    }

    private static String processError(InputStream is) throws IOException {
        return is.toString();
    }

    private static long processInteger(InputStream is) throws IOException {
        return readInteger(is);
    }

    //外部是byte数组，里面是readLine
    private static byte[] processBulkString(InputStream is) throws IOException {
        //读了它的长度
        int len=(int)readInteger(is);
        if (len==-1){
            //"$-1\r\n"--->null
            return null;
        }
        byte[] r=new byte[len];
//        for (int i = 0; i < len; i++) {
//            int b=is.read();
//            r[i]=(byte)b;
//        }
        is.read(r,0,len);
        //这里还需要处理\r\n和-1的判断
        int c=is.read();
        if (c==-1){
            throw new RuntimeException("不应该读到结尾的");
        }
        if (c=='\r'){
            int d=is.read();
            if (d!='\n'){
                throw new RuntimeException("您好，您输入的字符不符合规范，请重新输入");
            }
        }else{
            throw new RuntimeException("您好，您输入的字符不符合规范，请重新输入");
        }
        return r;
    }

    private static List<Object> processArray(InputStream is) throws IOException {
        int len=(int)readInteger(is);
        if (len==-1){
            //"*-1\r\n"
            return null;
        }
        List<Object> list=new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            //处理每一个byte[]/字符串,读走了就不会再读了，一长串
            try {
                list.add(process(is));
            } catch (RemoteException e) {
                list.add(e);
            }
        }
        return list;
    }

    private static Object process(InputStream is) throws IOException, RemoteException {
        int b=is.read();
        //读到字节流的结尾了
        if (b==-1){
            throw new RuntimeException("不应该读到末尾的");
        }
        switch(b){
            case '+':
                return processSimpleString(is);
            case '-':
                throw new RemoteException(processError(is));
            case ':':
                return processInteger(is);
            case '$':
                return processBulkString(is);
            case '*':
                return processArray(is);
            default:
                throw new RuntimeException("不识别的类型");
        }
    }
    public static void writeError(OutputStream os,String message) throws IOException {
        os.write('-');
        os.write(message.getBytes());
        os.write("\r\n".getBytes());
    }

    public static void writeInteger(OutputStream os,long v) throws IOException {
        os.write(':');
        os.write(String.valueOf(v).getBytes());
        os.write("\r\n".getBytes());
    }

    public static void writeBulking(OutputStream os,String s) throws IOException {
        byte[] buf=s.getBytes();
        os.write('$');
        os.write(String.valueOf(buf.length).getBytes());
        os.write("\r\n".getBytes());
        os.write(buf);
        os.write("\r\n".getBytes());
    }

    public static void writeArray(OutputStream os, List<String> list) throws Exception {
        os.write('*');
        os.write(String.valueOf(list.size()).getBytes());
        os.write("\r\n".getBytes());
        for (Object o:list) {
            if (o instanceof String){
                writeBulking(os,(String)o);
            }else if (o instanceof Integer){
                writeInteger(os,(Integer)o);
            }else if (o instanceof Long){
                writeInteger(os,(long)o);
            }else {
                throw new Exception("错误的类型");
            }
        }
    }

    public static void writeNull(OutputStream os) throws IOException {
        os.write('$');
        os.write('-');
        os.write(1);
        os.write('\r');
        os.write('\n');
    }
}

