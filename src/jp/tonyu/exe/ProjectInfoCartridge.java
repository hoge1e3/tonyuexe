package jp.tonyu.exe;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.tonyu.auth.RequestSigner;
import jp.tonyu.cartridges.UploadClient;
import jp.tonyu.edit.EQ;
import jp.tonyu.edit.FS;
import jp.tonyu.fs.GLSFile;
import jp.tonyu.js.JSRun;
import jp.tonyu.servlet.ServerInfo;
import jp.tonyu.servlet.ServletCartridge;
import jp.tonyu.util.Convert;
import net.arnx.jsonic.JSON;

import org.mozilla.javascript.Function;

import com.google.appengine.api.datastore.Entity;

public class ProjectInfoCartridge implements ServletCartridge {
    JSRun jsrun;
    ServletContext sctx;
    public ProjectInfoCartridge(FS fs,RequestSigner sgn) {
        super();
        this.fs=fs;
        this.sgn = sgn;
    }

    final RequestSigner sgn;
    FS fs;
    @Override
    public boolean get(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String u=req.getPathInfo();
        if (u.startsWith("/addDateToPrjInfo")) {
            return ProjectInfo.addDate();
        }
        if (u.startsWith("/listPublished.html")) {
            return listPublishedHTML(req,resp);
        }
        if (u.startsWith("/listPublished")) {
            return listPublished(req,resp);
        }
        if (u.startsWith("/"+UploadClient.URL_PRJ_INFO)) {
            return getProjectInfo(req, resp);
        }
        if (u.startsWith("/fork")) {
            return fork(req,resp);
        }
        return false;
    }
    private boolean listPublishedHTML(HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        jsrun.requireResource("/js/server/UI.js");
        Function f=(Function)jsrun.requireResource("/js/server/showPrjInfo.js");
        String prjInfoJSON=JSON.encode(listPublishedAsVector(0,10));
        String res=(String)jsrun.call(f, new Object[]{prjInfoJSON, ServerInfo.editURL(req), ServerInfo.exeURL(req) });

        return false;
    }
    private boolean fork(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String user=req.getParameter(UploadClient.PARAM_USR);
        String prj=req.getParameter(UploadClient.PARAM_PRJ);
        EQ e=ProjectInfo.get(user, prj, false);
        if (Boolean.TRUE.equals(e.attr(ProjectInfo.KEY_ALLOW_FORK))) {
            Map elem=new HashMap();
            e.putTo(elem,
                    ProjectInfo.KEY_USER,
                    ProjectInfo.KEY_PRJ_NAME,
                    ProjectInfo.KEY_PRJ_TITLE,
                    ProjectInfo.KEY_PRJ_DESC,
                    //ProjectInfo.KEY_THUMB,
                    ProjectInfo.KEY_LASTUPDATE,
               ProjectInfo.KEY_ALLOW_FORK,
               ProjectInfo.KEY_LICENSE);
            GLSFile c = fs.get("/home/").rel(user+"/").rel(prj+"/").rel(RunScriptCartridge.CONCAT);
            System.out.println(c+" "+c.exists());
            String buf=c.text()+
                    "\n<script language='text/tonyu' type='text/tonyu' data-filename='forkedFrom.json'>"+
            JSON.encode(elem)+"</script>";
            resp.setContentType("text/plain; charset=utf8");
            resp.getWriter().print("projectInfoIs('"+
                    buf.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r")+"');"
            );
        } else {
            throw new RuntimeException("Fork not allowed");
        }
        return true;
    }
    public boolean listPublished(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //System.out.println(req.getParameter("offset"));
        int offset=Convert.toIntDef(req.getParameter("offset"), 0);
        int count=Convert.toIntDef(req.getParameter("count"), 10);
        Vector res = listPublishedAsVector(offset,count);
        resp.setContentType("text/json; charset=utf8");
        resp.getWriter().print(JSON.encode(res));
        return true;
    }
    public Vector listPublishedAsVector(int offset, int count) {
        Vector res=new Vector();
        int i=0;
        for (Entity ee:ProjectInfo.listPublished()) {
            //System.out.println("i="+i+" offset="+offset+" size="+res.size());
            if (i++<offset) continue;
            EQ e=EQ.$(ee);
            Map elem=new HashMap();
            e.putTo(elem,
                    ProjectInfo.KEY_USER,
                    ProjectInfo.KEY_PRJ_NAME,
                    ProjectInfo.KEY_PRJ_TITLE,
                    ProjectInfo.KEY_PRJ_DESC,
                    ProjectInfo.KEY_THUMB,
                    ProjectInfo.KEY_LASTUPDATE,
               ProjectInfo.KEY_ALLOW_FORK,
               ProjectInfo.KEY_LICENSE);
            Object lu=e.attr(ProjectInfo.KEY_LASTUPDATE);
            if (lu instanceof Date) {
                Date lud = (Date) lu;
                elem.put(ProjectInfo.KEY_LASTUPDATE, lud.getTime());
            }
            if (elem.get(ProjectInfo.KEY_ALLOW_FORK).equals(true) &&
                    elem.get(ProjectInfo.KEY_LICENSE)==null) {
                elem.put(ProjectInfo.KEY_ALLOW_FORK, false);
            }
            res.add(elem);
            if (res.size()>=count) break;
        }
        return res;
    }
    public boolean getProjectInfo(HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        String user=req.getParameter(UploadClient.PARAM_USR);
        String prj=req.getParameter(UploadClient.PARAM_PRJ);
        String chk=req.getParameter(UploadClient.PARAM_CHK);
        if (sgn.chk(user+prj,chk)) {
            EQ e=ProjectInfo.get(user, prj, false);
            Map<String,String> res=new HashMap<String,String>();
            res.put(ProjectInfo.KEY_ALLOW_FORK, e.attr(ProjectInfo.KEY_ALLOW_FORK)+""  );
            res.put(ProjectInfo.KEY_LICENSE, e.attr(ProjectInfo.KEY_LICENSE)+""  );
            res.put(ProjectInfo.KEY_PUBLIST, e.attr(ProjectInfo.KEY_PUBLIST)+""  );
            res.put(UploadClient.KEY_PRJ_TITLE, e.attr(UploadClient.KEY_PRJ_TITLE)+"");
            res.put(UploadClient.KEY_PRJ_DESC , e.attr(UploadClient.KEY_PRJ_DESC)+"");
            resp.setContentType("text/plain; charset=utf8");
            resp.getWriter().print(JSON.encode(res));
        } else {
            resp.getWriter().print("NO");
        }
        return true;
    }
    /*public boolean getProjectInfo2(HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        String user=auth.currentUserId();
        String prj=req.getParameter(UploadClient.PARAM_PRJ);
        EQ e=ProjectInfo.get(user, prj, false);
        Map<String,String> res=new HashMap<String,String>();
        res.put(ProjectInfo.KEY_ALLOW_FORK, e.attr(ProjectInfo.KEY_ALLOW_FORK)+""  );
        res.put(ProjectInfo.KEY_LICENSE, e.attr(ProjectInfo.KEY_LICENSE)+""  );
        res.put(ProjectInfo.KEY_PUBLIST, e.attr(ProjectInfo.KEY_PUBLIST)+""  );
        res.put(UploadClient.KEY_PRJ_TITLE, e.attr(UploadClient.KEY_PRJ_TITLE)+"");
        res.put(UploadClient.KEY_PRJ_DESC , e.attr(UploadClient.KEY_PRJ_DESC)+"");

        resp.getWriter().print(JSON.encode(res));
        return true;
    }*/

    @Override
    public boolean post(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

}
