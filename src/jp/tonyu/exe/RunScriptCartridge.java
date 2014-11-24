package jp.tonyu.exe;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.tonyu.edit.FS;
import jp.tonyu.fs.GLSFile;
import jp.tonyu.servlet.ServletCartridge;
import jp.tonyu.util.Resource;

public class RunScriptCartridge implements ServletCartridge {
    final FS fs;
    static final String CONCAT="concat.js";
    public RunScriptCartridge(FS fs) {
        super();
        this.fs = fs;
    }

    @Override
    public boolean get(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String u=req.getPathInfo();
        u=u.substring(1);
        GLSFile c = fs.get("/home/").rel(u+"/").rel(CONCAT);
        System.out.println(c+" "+c.exists());
        if (c.exists()) {
            String html = Resource.text(RunScriptCartridge.class, ".html");
            resp.setContentType("text/html; charset=utf8");
            resp.getWriter().println(html.replace("$CONCAT", c.text()));
            return true;
        }
        resp.setStatus(404);
        resp.getWriter().print("Not found: "+u);
        return true;
    }

    @Override
    public boolean post(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String u=req.getPathInfo();
        if (u.startsWith("/testSync")) {
            String a=req.getParameter("a");
            String b=req.getParameter("b");

            String resps = "Succ on tonyuexe  a="+a+"  b="+b;
            System.out.println("resps="+resps);
            resp.getWriter().print(resps);
            return true;
        }
        return false;
    }

}
