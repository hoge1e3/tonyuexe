define(["Util"],function (Util) {
    var $;
    $=function (tag){
        var res={attrs:{},elems:[]};
        function escape(html) {
            return html.replace(/&/,"&amp;").replace(/"/,"&quot;");
        }
        res.attr=function (k,v) {
            res.attrs[k]=v;
        };
        res.append=function (e) {
            res.elems.push(i);
        };
        res.html=function () {
            var buf="";
            buf+=tag.replace(/>$/," ");
            for (var k in res.attrs) {
                buf+=k+'="'+escape(res.attrs[k])+'" ';
            }
            if (res.elems.length>0) {
                res.elems.forEach(function (e) {
                    res+=e.html();
                });
                buf+=tag.replace(/^</,"</");
            } else {
                buf+="/>";
            }
        };
        return res;
    };
    var UI={};
    UI=function () {
        var expr=[];
        for (var i=0 ; i<arguments.length ; i++) {
            expr[i]=arguments[i];
        }
        var listeners=[];
        var res=parse(expr);
        return res;
        function parse(expr) {
            if (expr instanceof Array) return parseArray(expr);
            else if (typeof expr=="string") return parseString(expr);
            else return expr;
        }
        function parseArray(a) {
            var tag=a[0];
            var i=1;
            var res=$("<"+tag+">");
            if (typeof a[i]=="object" && !(a[i] instanceof Array) && !(a[i] instanceof $) ) {
                parseAttr(res, a[i],tag);
                i++;
            }
            while (i<a.length) {
                res.append(parse(a[i]));
                i++;
            }
            return res;
        }
        function parseAttr(jq, o, tag) {
            for (var k in o) {
                if (k=="on") {
                    for (var e in o.on) on(e, o.on[e]);
                } else if (k=="css") {
                    jq.css(o[k]);
                } else if (!Util.startsWith(k,"$")){
                    jq.attr(k,o[k]);
                }
            }
        }
        function parseString(str) {
            return $("<span>").text(str);
        }
    };
    return UI;
});
