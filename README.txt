tonyuexe ver 0.1

http://tonyuexe.appspot.com/
Tonyu 2 application publishing server

= How to host tonyuexe as your own GAE app

== Check folder link of tonyuedit

This repository depends on tonyuedit (github/hoge1e3/tonyuedit).
You have to link to folder tonyuedit/src as java source folder.

== Configure ServerInfo

Open jp.tonyu.servlet.ServerInfo and modify urls for your app

== Set the root password

open http://your_tonyuexe.appspot.com/

Open js console and type

$.post("/exe/passwd",{user:"root","new":"the_root_password"});

Notice: Once the root password is set, you should login as root to change the password.
        To reset password, open your GAE console and delete the "User" datastore entity where userID='root'

== Set the OAuth tokens

Log in as root:
-open http://your_tonyuexe.appspot.com/edit/login/
-Click the element "ログイン(Login)" and input form appears.
-input root and your root password

Type on js console:

$.post("/exe/oauthKey",{service:"google",
key:"your_google_oauth_key",
secret:"your_google_oauth_secret"
});

$.post("/exe/oauthKey",{service:"twitter",
key:"your_twitter_oauth_key",
secret:"your_twitter_oauth_secret"
});

== Set the tonyuedit-tonyuexe communication token

communication token is needed in order to upload projects on tonyuedit to tonyuexe
or obtain project information of tonyuexe from tonyuedit.

- Decide some random string as the_token
- Log in as root and type on js console:

$.post("/exe/oauthKey",{service:"tonyu_comm",
key:"tonyu",
secret:"the_token"
});

NOTICE: you have to set the same token also in your tonyuedit app.

== License

tonyuexe is licensed under Apache License, Version 2.0 .




