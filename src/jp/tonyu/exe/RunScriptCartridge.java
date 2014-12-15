package jp.tonyu.exe;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;

import jp.tonyu.edit.EQ;
import jp.tonyu.edit.FS;
import jp.tonyu.fs.GLSFile;
import jp.tonyu.servlet.ServletCartridge;
import jp.tonyu.udb.AppAuth;
import jp.tonyu.util.Html;
import jp.tonyu.util.Resource;

public class RunScriptCartridge implements ServletCartridge {
    final FS fs;
    static final String CONCAT="concat.js";
    DatastoreService dss;
    public RunScriptCartridge(DatastoreService dss, FS fs) {
        super();
        this.dss =dss;
        this.fs = fs;
    }

    @Override
    public boolean get(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String url=req.getPathInfo();
        url=url.substring(1);
        GLSFile c = fs.get("/home/").rel(url+"/").rel(CONCAT);
        String []up=url.split("/");
        EQ pinfo = ProjectInfo.get(up[0], up[1], false);
        String title =pinfo.attr(ProjectInfo.KEY_PRJ_TITLE)+"";
        AppAuth appAuth=AppAuth.create(dss, up[0], up[1]);
        System.out.println(c+" "+c.exists());
        if (c.exists()) {
            String html = Resource.text(RunScriptCartridge.class, ".html");
            resp.setContentType("text/html; charset=utf8");
            resp.getWriter().println(
                    Html.p(html, title, appAuth.embed(c.text()))
//                    html.replace("$CONCAT", appAuth.embed(c.text()) )
            );
            return true;
        }
        resp.setStatus(404);
        resp.getWriter().print("Not found: "+url);
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
