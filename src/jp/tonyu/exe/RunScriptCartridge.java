package jp.tonyu.exe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import com.google.appengine.api.datastore.DatastoreService;

import jp.tonyu.edit.EQ;
import jp.tonyu.edit.FS;
import jp.tonyu.fs.GLSFile;
import jp.tonyu.servlet.ServletCartridge;
import jp.tonyu.udb.AppAuth;
import jp.tonyu.util.Html;
import jp.tonyu.util.Resource;

public class RunScriptCartridge implements ServletCartridge {
    private static final String VIEW_SOURCE = "/view-source";
    private static final String SINGLE_INFO = "/info";
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
        boolean vs=false,desc=false;
        url=url.substring(1);
        if (url.endsWith(VIEW_SOURCE)) {
            vs=true;
            url=url.substring(0,url.length()-VIEW_SOURCE.length());
        }
        if (url.endsWith(SINGLE_INFO)) {
            desc=true;
            url=url.substring(0,url.length()-SINGLE_INFO.length());
        }

        GLSFile c = fs.get("/home/").rel(url+"/").rel(CONCAT);
        if (c.exists()) {
            String []up=url.split("/");
            EQ pinfo = ProjectInfo.get(up[0], up[1], false);
            String title =pinfo.attr(ProjectInfo.KEY_PRJ_TITLE)+"";
            if (vs && pinfo.attr(ProjectInfo.KEY_ALLOW_FORK).equals(true) ) {
                return viewSource(req,resp, c,title);
            }
            if (desc) {
                return singleInfo(req,resp, pinfo);
            }
            AppAuth appAuth=AppAuth.create(dss, up[0], up[1]);
            System.out.println(c+" "+c.exists());
            String html = Resource.text(RunScriptCartridge.class, ".html");
            resp.setContentType("text/html; charset=utf8");
            resp.getWriter().println(
                    Html.p(html, title, appAuth.embed(c.text()))
            );
            return true;
        } else {
            resp.setStatus(404);
            resp.getWriter().print("Not found: "+url);
            return true;

        }
    }


    private boolean singleInfo(HttpServletRequest req,
            HttpServletResponse resp, EQ pinfo) throws JSONException, IOException {
        String title=(String)pinfo.attr(ProjectInfo.KEY_PRJ_TITLE);
        Map m=new HashMap();
        pinfo.putTo(m);
        String html = Resource.text(SingleInfoCartridge.class, ".html");
        resp.setContentType("text/html; charset=utf8");
        resp.getWriter().println(
                Html.p(html, title, JSON.encode(m))
                );
        return true;
    }

    private boolean viewSource(HttpServletRequest req,
            HttpServletResponse resp, GLSFile concatFile, String title) throws IOException {
        String html = Resource.text(ViewSourceCartridge.class, ".html");
        resp.setContentType("text/html; charset=utf8");
        resp.getWriter().println(
                Html.p(html, title, concatFile.text())
                );
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
