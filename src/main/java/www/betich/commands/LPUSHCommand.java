package www.betich.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import www.betich.Command;
import www.betich.Database;
import www.betich.Protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class LPUSHCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(LPUSHCommand.class);
    private List<Object> args;
    public void setArgs(List<Object> args) {
        this.args = args;
    }
    public void run(OutputStream os) throws IOException {
        if (args.size() != 2) {
            Protocol.writeError(os, "命令至少需要两个参数");
            return;
        }
        String key = new String((byte[])args.get(0));
        String value = new String((byte[])args.get(1));
        logger.debug("运行的是 lpush 命令: {} {}", key, value);
        // 这种方式不是一个很好的线程同步的方式
        List<String> list = Database.getList(key);
        list.add(0, value);
        logger.debug("插入后数据共有 {} 个", list.size());
        Protocol.writeInteger(os, list.size());
    }
}

