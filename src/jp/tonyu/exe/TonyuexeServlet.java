package jp.tonyu.exe;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.tonyu.auth.Auth;
import jp.tonyu.auth.OAuthKeyDB;
import jp.tonyu.auth.RequestSigner;
import jp.tonyu.cartridges.BlobCartridge;
import jp.tonyu.cartridges.LoginCartridge;
import jp.tonyu.cartridges.ShellCartridge;
import jp.tonyu.cartridges.UDBCartridge;
import jp.tonyu.edit.FS;
import jp.tonyu.fs.LSEmulator;
import jp.tonyu.fs.MemCache;
import jp.tonyu.js.JSRun;
import jp.tonyu.servlet.MultiServletCartridge;
import jp.tonyu.servlet.RequestFragmentReceiver;
import jp.tonyu.servlet.ServerInfo;
import jp.tonyu.servlet.ServletCartridge;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

@SuppressWarnings("serial")
public class TonyuexeServlet extends HttpServlet {
    DatastoreService dss=DatastoreServiceFactory.getDatastoreService();
    FS fs=new FS(new LSEmulator(dss, new MemCache()) );

    public ServletCartridge getCartridge(HttpServletRequest req) {
        Auth a=new Auth(req.getSession());
        OAuthKeyDB okb = new OAuthKeyDB(dss);
        RequestSigner sgn=new RequestSigner(okb);
        MultiServletCartridge c=new MultiServletCartridge(
                new ProjectInfoCartridge(fs,sgn),
                new UploadCartridge(fs,a,sgn),
                new BlobCartridge(a, dss, sgn, true),
                new LoginCartridge(dss, a, okb, fs, ServerInfo.exeTop(req)+"/"),
                new UDBCartridge(dss,a,true)
        );
        if (a.isRoot()) {
            ServletContext servletContext = getServletContext();
            JSRun jsRun=new JSRun(fs, servletContext);
            c.insert(new ShellCartridge(jsRun));
        }
        c.insert(new RunScriptCartridge(this, dss, fs)); // this must be last!
        return new RequestFragmentReceiver(a, dss, c);
    }
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        getCartridge(req).get(req, resp);
    }
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        getCartridge(req).post(req, resp);
    }
}
