package jp.tonyu.exe;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.arnx.jsonic.JSON;

import jp.tonyu.edit.EQ;
import jp.tonyu.servlet.ServletCartridge;

import java.util.Vector;

import com.google.appengine.api.datastore.Entity;

public class ProjectInfoCartridge implements ServletCartridge {

    @Override
    public boolean get(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String u=req.getPathInfo();
        if (u.startsWith("/listPublished")) {
            Vector res=new Vector();
            for (Entity ee:ProjectInfo.listPublished()) {
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
                        elem.get(ProjectInfo.KEY_LICENSE)!=null) res.add(elem);
            }
            resp.setContentType("text/json; charset=utf8");
            resp.getWriter().print(JSON.encode(res));
            return true;
        }
        return false;
    }

    @Override
    public boolean post(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

}
