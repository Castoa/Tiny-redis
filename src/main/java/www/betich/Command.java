package www.betich;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

//Command是一个接口，器里面只有一个方法，其每个命令的实现有其自己的实现子类
public interface Command {
    //留给每个命令自己去解决
    void setArgs(List<Object> args);
    //处理命令
    void run(OutputStream os) throws Exception;
}