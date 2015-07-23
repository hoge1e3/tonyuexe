package jp.tonyu.exe;

import java.util.Date;

import jp.tonyu.edit.CountingEntityIterable;
import jp.tonyu.edit.EQ;
import jp.tonyu.cartridges.UploadClient;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;

public class ProjectInfo {
    public static final String KIND_PRJINFO = UploadClient.KIND_PRJINFO;
    public static final String KEY_USER = UploadClient.KEY_USER;
    public static final String KEY_PRJ_TITLE = UploadClient.KEY_PRJ_TITLE;
    public static final String KEY_PRJ_DESC = UploadClient.KEY_PRJ_DESC;
    public static final String KEY_PRJ_NAME = UploadClient.KEY_PRJ_NAME;
    public static final String KEY_PUBLIST = UploadClient.KEY_PUBLIST;
    public static final String KEY_ALLOW_FORK = UploadClient.KEY_ALLOW_FORK;
    public static final String KEY_LICENSE = UploadClient.KEY_LICENSE;
    public static final String KEY_THUMB = UploadClient.PARAM_THUMB;
    public static final String KEY_LASTUPDATE = "lastUpdate";


    static DatastoreService dss=DatastoreServiceFactory.getDatastoreService();
    public static EQ get(String user,String prjName,boolean cine) {
        Query vQuery = new Query(KIND_PRJINFO);
        System.out.println("Getting prj info u="+user+" p="+prjName);
        Filter filter= CompositeFilterOperator.and(
            new Query.FilterPredicate(KEY_USER, FilterOperator.EQUAL, user),
            new Query.FilterPredicate(KEY_PRJ_NAME, FilterOperator.EQUAL, prjName)
        );
        vQuery.setFilter(filter);
        Iterable<Entity> it = new CountingEntityIterable( dss.prepare(vQuery).asIterable() );
        Entity res=null;
        for (Entity e:it) {
            res=e;
            break;
        }
        System.out.println("Getting prj info res "+res);
        if (res==null && cine) {
            res=new Entity(KIND_PRJINFO);
            return EQ.$(res).attr(KEY_USER , user).attr(KEY_PRJ_NAME, prjName);
        }
        return EQ.$(res);
    }
    public static void put(String user,String prjName, String prjTitle, String desc, String thumb,
            boolean  publishToList ,boolean allowFork, String license) {
        EQ e=get(user, prjName, true).
                attr(KEY_PRJ_TITLE,prjTitle).
                attr(KEY_PRJ_DESC,desc).
                attr(KEY_THUMB,thumb).
                attr(KEY_PUBLIST, publishToList).
                attr(KEY_ALLOW_FORK,allowFork).
                attr(KEY_LICENSE,license).attr(KEY_LASTUPDATE, new Date());
        dss.put(e.get());
    }
    public static Iterable<Entity>  listPublished(String user) {
        EQ e = EQ.$(KIND_PRJINFO).attr(KEY_PUBLIST, true);
        if (user!=null && user.length()>0) {
            e.attr(KEY_USER,user);
        }
        e.order(KEY_LASTUPDATE, true);
        return e.asIterable(dss);
        /*Query vQuery = new Query(KIND_PRJINFO);
        vQuery.setFilter(
                new Query.FilterPredicate(KEY_PUBLIST, FilterOperator.EQUAL, true)
        );
        Query vQuery=e.query();
        vQuery.addSort(KEY_LASTUPDATE, SortDirection.DESCENDING);
        Iterable<Entity> it = new CountingEntityIterable( dss.prepare(vQuery).asIterable() );
        return it;*/
    }
    public static boolean addDate() {
        Query vQuery = new Query(KIND_PRJINFO);
        Iterable<Entity> it =  dss.prepare(vQuery).asIterable();
        int n=0;
        for (Entity e:it) {
            EQ eq=EQ.$(e);
            System.out.println(eq.attr(KEY_PRJ_NAME));
            if (eq.attr(KEY_LASTUPDATE)==null) {
                eq.attr(KEY_LASTUPDATE,new Date(new Date().getTime()-(60+n)*86400L*1000L   ));
                eq.putTo(dss);
                n++;
            }
        }

        return true;
    }
}
