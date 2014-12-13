function pad0(n,c) {
    if (!c) c=2;
    var res="00000000"+n;
    return res.substring(res.length-c);
}
function fmt(d) {
    return d.getFullYear()+"/"+pad0(d.getMonth()+1)+"/"+pad0(d.getDate())+" "+
    pad0(d.getHours())+":"+pad0(d.getMinutes());
}
function showPrjInfo(info, editURL, exeURL) {
    var res=JSON.parse(info);
    var lbl={
            mit:"MIT License",
            gplv3:"GPL License ver3"
    };
    console.log(res);
    res.sort(function (a,b) {
        var la=a.lastUpdate || 0;
        var lb=b.lastUpdate || 0;
        return la>lb?-1:la<lb?1:0;
    });
    var div=$("<div>");
    res.forEach(function (r){
        var url=exeURL+"/exe/"+r.user+"/"+r.project;
        var title=(r.title && r.title!="null" ? r.title:r.project);
        div.append(UI("div",
                ["div",
                 ["a",{href:url,"class":"title"},title],
                 " by ",r.user,
                 //["td",r.allowFork],
                 r.lastUpdate? " at "+fmt(new Date(r.lastUpdate))  :"",
                         " / License: ",lbl[r.license]||r.license+""  , " / ",
                         (r.allowFork?
                                 ["a",{
                                     href:editURL+"/html/build/importFromJsdoit.html?user="+r.user+"&project="+r.project
                                 },"Fork"]
                         :"")
                         ],
                         ["pre",["a", {href:url},["img",{
                             style:"float:left;padding:10px;",
                             src:(r.thumbnail?r.thumbnail:"images/nowprint.png")
                         }]],
                         (r.description || "")],
                         ["div",{style:"clear:left;"}]

        ));
    });
    return div.html();
}
return showPrjInfo;
