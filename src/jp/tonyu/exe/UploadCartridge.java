package jp.tonyu.exe;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mozilla.javascript.Scriptable;

import jp.tonyu.auth.Auth;
import jp.tonyu.auth.RequestSigner;
import jp.tonyu.cartridges.UploadClient;
import jp.tonyu.edit.FS;
import jp.tonyu.fs.GLSFile;
import jp.tonyu.js.Scriptables;
import jp.tonyu.servlet.ServerInfo;
import jp.tonyu.servlet.ServletCartridge;
import jp.tonyu.servlet.UI;
import jp.tonyu.util.col.Maps;
import net.arnx.jsonic.JSON;

public class UploadCartridge implements ServletCartridge {
    final FS fs;
    final Auth auth;
    final RequestSigner sgn;
    public UploadCartridge(FS fs,Auth auth, RequestSigner sgn) {
        super();
        this.fs = fs;
        this.auth=auth;
        this.sgn=sgn;
    }
    static final String URL_UPLOAD_PRJ=UploadClient.URL_UPLOAD_PRJ;
    private static final String PARAM_USER = UploadClient.PARAM_USR;
    private static final String PARAM_PRJ = UploadClient.PARAM_PRJ;
    private static final String PARAM_PRG = UploadClient.PARAM_PRG;
    private static final String PARAM_CHK = UploadClient.PARAM_CHK;
    private static final String PARAM_THUMB = UploadClient.PARAM_THUMB;
    @Override
    public boolean get(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String u=req.getPathInfo();
        if (u.startsWith("/"+URL_UPLOAD_PRJ)) {
            PrintWriter w = resp.getWriter();
            w.print(UI.t("html",
                        UI.t("body",
                                UI.t("form").a("method", "POST").a("action", URL_UPLOAD_PRJ).e(
                                        "user",UI.t("input").a("name", PARAM_USER),UI.t("br"),
                                        "project",UI.t("input").a("name", PARAM_PRJ),UI.t("br"),
                                        UI.t("textarea").a("name", PARAM_PRG).a("rows", 20).a("cols", 40),
                                        UI.t("br"),
                                        "checksum",UI.t("input").a("name", PARAM_CHK),UI.t("br"),
                                        UI.t("input").a("type", "submit")
                                )
                        )
                    ));
            return true;
        }

        return false;
    }

    @Override
    public boolean post(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String u=req.getPathInfo();
        if (u.startsWith("/"+URL_UPLOAD_PRJ)) {
            return uploadProject1(req, resp);
        }
        /*if (u.startsWith("/"+UploadClient.URL_PRJ_INFO)) {
            return getProjectInfo2(req, resp);
        }*/
        return false;
    }
    public boolean uploadProject1(HttpServletRequest req,
            HttpServletResponse resp) throws IOException, FileNotFoundException {
        String user=req.getParameter(PARAM_USER);
        String prj=req.getParameter(PARAM_PRJ);
        String prg=req.getParameter(PARAM_PRG);
        String title=req.getParameter(UploadClient.KEY_PRJ_TITLE);
        String desc=req.getParameter(UploadClient.KEY_PRJ_DESC);
        String thumb = req.getParameter(PARAM_THUMB);

        String license=req.getParameter(UploadClient.KEY_LICENSE);
        boolean allowFork="true".equals(req.getParameter(UploadClient.KEY_ALLOW_FORK));
        boolean pubList="true".equals(req.getParameter(UploadClient.KEY_PUBLIST));

        String chk=req.getParameter(PARAM_CHK);
        if (!sgn.chk(user+prj+prg+title+desc+license+allowFork+pubList , chk)) {
            resp.getWriter().println("invalid");
            return true;
        }

        GLSFile concat = fs.get("/home/").rel(user+"/").rel(prj+"/").rel(RunScriptCartridge.CONCAT);
        concat.text(prg);
        Scriptable s=concat.metaInfo();
        ProjectInfo.put(user, prj, title, desc, thumb, pubList, allowFork, license,Scriptables.getAsString(s,GLSFile.KEY_DATASTORE_KEY ,""));
        resp.setContentType("text/plain; charset=utf8");
        String url=(req.getServerName().indexOf("localhost")>=0?
                ServerInfo.tonyuexe_local : ServerInfo.tonyuexe_server
            );
        resp.getWriter().print(JSON.encode( Maps.create("url",url+"/exe/"+user+"/"+prj) ));
        return true;
    }
    /*public boolean uploadProject2(HttpServletRequest req,
            HttpServletResponse resp) throws IOException, FileNotFoundException {
        String user=auth.currentUserId();
        if (user==null) throw new RuntimeException("Not logged in");
        String prj=req.getParameter(PARAM_PRJ);
        String prg=req.getParameter(PARAM_PRG);
        String title=req.getParameter(UploadClient.KEY_PRJ_TITLE);
        String desc=req.getParameter(UploadClient.KEY_PRJ_DESC);

        System.out.println("uploadPrj prg.len"+prg.length());

        String license=req.getParameter(UploadClient.KEY_LICENSE);
        boolean allowFork="true".equals(req.getParameter(UploadClient.KEY_ALLOW_FORK));
        boolean pubList="true".equals(req.getParameter(UploadClient.KEY_PUBLIST));

        ProjectInfo.put(user, prj, title, desc,  pubList, allowFork, license);

        fs.get("/home/").rel(user+"/").rel(prj+"/").rel(RunScriptCartridge.CONCAT)
        .text(prg);
        resp.setContentType("text/plain; charset=utf8");
        String url=(req.getServerName().indexOf("localhost")>=0?
                "http://localhost:8887/":"http://tonyuexe.appspot.com/"
            );
        resp.getWriter().print(JSON.encode( Maps.create("url",url+"exe/"+user+"/"+prj) ));
        return true;
    }*/


    //final static String salt=UploadClient.salt;

}
