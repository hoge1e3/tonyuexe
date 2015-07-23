function addProjectInfo(r,i) {
    function pad0(n,c) {
        if (!c) c=2;
        var res="00000000"+n;
        return res.substring(res.length-c);
    }
    function fmt(d) {
        return d.getFullYear()+"/"+pad0(d.getMonth()+1)+"/"+pad0(d.getDate())+" "+
        pad0(d.getHours())+":"+pad0(d.getMinutes());
    }
    var editURL=(location.href.match(/localhost/)?
            "http://localhost:8888":"http://tonyuedit.appspot.com");
    var lbl={
            mit:"MIT License",
            gplv3:"GPL License ver3"
    };
    var url="/exe/"+r.user+"/"+r.project;
    var title=(r.title && r.title!="null" ? r.title:r.project);
    $("#prjs").append(UI("div",
            ["a",{name:i}],
            ["div",
                 ["a",{href:url,"class":"title",target:"play"},title],
                 " by ",["a",{href:"/exe/"+r.user},r.user],
                 r.lastUpdate? " at "+fmt(new Date(r.lastUpdate))  :"",
                 " / License: ",lbl[r.license]||r.license+""  , " / ",
                 (r.allowFork?["span",
                       ["a",{
                           href:url+"/view-source",target:"viewsrc"
                       },"View Source"],
                       " / ",
                       ["a",{
                           href:editURL+"/html/build/importFromJsdoit.html?user="+r.user+"&project="+r.project
                       },"Fork"]," / "
                 ]:""),
                 ["a",{
                     href:url+"/info",target:"info"
                 },"Info"]
           ],
           ["pre",
              ["a", {href:url,target:"play"},["img",{
                 style:"float:left;padding:10px;",
                 src:(r.thumbnail?r.thumbnail:"/images/nowprint.png")
               }]],
               (r.description || "")],
           ["div",{style:"clear:left;"}]
    ));
}