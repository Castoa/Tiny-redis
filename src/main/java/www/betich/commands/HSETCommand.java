package www.betich.commands;

import www.betich.Command;
import www.betich.Database;
import www.betich.Protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class HSETCommand implements Command {
    private List<Object> args;
    @Override
    public void setArgs(List<Object> args) {
        this.args = args;
    }
    @Override
    public void run(OutputStream os) throws IOException {
        if (args.size() != 3) {
            Protocol.writeError(os, "命令至少需要三个参数");
            return;
        }
        String key = new String((byte[])args.get(0));
        String field = new String((byte[])args.get(1));
        String value = new String((byte[])args.get(2));
        Map<String, String> hash =Database.getHashes(key);
        boolean isUpdate = hash.containsKey(field);
        hash.put(field, value);
        if (isUpdate) {
            Protocol.writeInteger(os, 0);
        } else {
            Protocol.writeInteger(os, 1);
        }
    }
}
