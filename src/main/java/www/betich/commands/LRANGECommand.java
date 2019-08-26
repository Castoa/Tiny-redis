package www.betich.commands;

import www.betich.Command;
import www.betich.Database;
import www.betich.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.List;

public class LRANGECommand implements Command {
    public final static Logger logger = LoggerFactory.getLogger( LRANGECommand.class );
    public static List<Object> args;
    @Override
    public void setArgs(List<Object> args) {
        this.args=args;
    }
    @Override
    public void run(OutputStream os) throws Exception {
        if (args.size() != 3) {
            Protocol.writeError(os, "命令至少需要三个参数");
            return;
        }
        String key = new String((byte[]) args.get(0));
        int start = Integer.parseInt(new String((byte[]) args.get(1)));
        int end = Integer.parseInt(new String((byte[]) args.get(2)));
        logger.debug("lrangecommand中获取的key:start:end{},{},{}", key, start, end);
        List<String> list = Database.getList(key);
        if (end<0){
            end=list.size()+end;
        }
        List<String> result=list.subList(start,end+1);
        Protocol.writeArray(os,result);
    }
}

