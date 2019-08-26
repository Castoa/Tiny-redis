package www.betich.commands;

import www.betich.Command;
import www.betich.Database;
import www.betich.Protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class HGETCommand implements Command {
    private List<Object> args;
    @Override
    public void setArgs(List<Object> args) {
        this.args = args;
    }
    @Override
    public void run(OutputStream os) throws IOException {
        if (args.size() != 2) {
            Protocol.writeError(os, "命令至少需要两个参数");
        }
        String key = new String((byte[])args.get(0));
        String field = new String((byte[])args.get(1));

        Map<String, String> hash = Database.getHashes(key);
        String value = hash.get(field);
        if (value != null) {
            Protocol.writeBulking(os, value);
        } else {
            Protocol.writeNull(os);
        }
    }
}
