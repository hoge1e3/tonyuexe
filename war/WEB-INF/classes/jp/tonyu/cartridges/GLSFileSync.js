(function () {
var SFile = FS;
var fsHome=FS.get("/");
var DINFO=".dirinfo";
var exports={};

function toLSPath(/*SFile*/ base, /*SFile*/ child) {
    var p=child.relPath(base);
    p=p.replace(/\\/g, "/");
    if (startsWith(p,"./")) {
        p=p.substring(2);
    }
    if (child.isDir() && !endsWith(p,"/")) {
        p=p+"/";
    }
    /*if (p==("./") || p==".") {
        p="";
    }*/
    return p;
}
function endsWith(str,postfix) {
    return str.substring(str.length-postfix.length)===postfix;
}
function startsWith(str,prefix) {
    return str.substring(0, prefix.length)===prefix;
}

function fromLSPath(path) {
    if (startsWith(path,"/")) path=path.substring(1);
    return fsHome.rel(path);
}
exports.getDirInfo=function(req, resp){
    //console.log("getDirInfo");
    var base=req.query.base;
    var excludes=req.query.excludes;
    console.log("getdirInfo excludes raw="+ excludes);
    if (typeof excludes=="string") {
        excludes=JSON.parse(excludes);
    } else if (typeof excludes!="object") {
        excludes=[];
    }

    var basef;
    resp.setHeader("Content-Type", "text/json;charset=utf8");
    if (base==null) {
        basef=fsHome;
        base="/";
    } else {
        basef=fsHome.rel(base.substring(1));
    }
    console.log("getdirInfo basef="+basef);
    console.log("getdirInfo excludes="+ excludes.join(":"));
    var data={},dirs={};
    basef.recursive(function (f) {
    	var p=toLSPath(basef, f);
    	data[p]=f.metaInfo();
    },{includeTrashed:true, excludes:excludes});
    var res={base:base, data:data};
    resp.send( res);
};
function getMeta(f) {
	return f.metaInfo();
}
function valid(name) {
    return !name.match(/[\\\:\?\*\>\<\|\"]/);
}
function setMeta(f, meta) {
	f.metaInfo(meta);
}
exports.File2LSSync=function(req, resp){
	var paths=JSON.parse(req.body.paths);
	var base=req.body.base;
	resp.setHeader("Content-Type", "text/json;charset=utf8");
	var basef;
	if (base==null) {
		basef=fsHome;
		base="/";
	} else {
		basef=fsHome.rel(base.substring(1));
	}
	var data={};
	paths.forEach(function (path) {
		var f=basef.rel(path);
		if (f.name()==DINFO) return;
		var m=getMeta(f);
		if (f.exists()) m.text=f.text();
		data[path]=m;
	});
	var res={base:base, data:data};
	resp.send( res);
};

exports.LS2FileSync=function(req, resp){
	var base=req.body.base;
	var data=JSON.parse(req.body.data);
	console.log("LS2FileSync base = "+base);
	console.log("LS2FileSync data = "+data);
	//throw "Test error";
	var basef;
	if (base==null) {
		basef=fsHome;
		base="/";
	} else {
		basef=fsHome.rel(base.substring(1));
	}
	for (var path in data) {
		var o=data[path];
		if (endsWith(path,"/")) continue;
		if (!valid(path)) continue;
		var dst=basef.rel(path);
		console.log(path+"->"+dst);
		if (dst.isDir()) continue;
		console.log("setMeta dst="+dst+" path="+path);
		if (typeof o=="string") {
		    dst.text(o);
		} else if (o.trashed && dst.exists()) {
			dst.trash();
		} else {
			dst.textMeta(o.text, o);
		}
	}
	resp.setHeader("Content-Type", "text/plain;charset=utf8");
	resp.send("OK!!");
};
return exports;
})();
