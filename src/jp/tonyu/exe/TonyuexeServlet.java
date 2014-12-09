package jp.tonyu.exe;

import java.io.IOException;
import javax.servlet.http.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import jp.tonyu.auth.Auth;
import jp.tonyu.auth.LoginCartridge;
import jp.tonyu.auth.OAuthKeyDB;
import jp.tonyu.auth.RequestSigner;
import jp.tonyu.blob.BlobCartridge;
import jp.tonyu.edit.FS;
import jp.tonyu.fs.LSEmulator;
import jp.tonyu.fs.MemCache;
import jp.tonyu.servlet.MultiServletCartridge;
import jp.tonyu.servlet.RequestFragmentReceiver;
import jp.tonyu.servlet.ServerInfo;
import jp.tonyu.servlet.ServletCartridge;
import jp.tonyu.udb.UDBCartridge;

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
                new LoginCartridge(dss, a, okb, fs, ServerInfo.tonyuexe_server+"/"),
                new UDBCartridge(dss,a,true),
                new RunScriptCartridge(dss, fs) // this must be last!
        );
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
