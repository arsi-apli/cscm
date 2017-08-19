# Code snippets community manager - BETA-SNAPSHOT 

## Netbeans plugin  

### Icons


![Community server](https://sourceforge.net/p/cscm/wiki/Netbeans%20plugin/attachment/help_item.png)  Community server  
![Team server](https://sourceforge.net/p/cscm/wiki/Netbeans%20plugin/attachment/help_item_team.png)  Team server  
![Local database](https://sourceforge.net/p/cscm/wiki/Netbeans%20plugin/attachment/help_item_local.png)  Local database  
  
![Full text search](https://sourceforge.net/p/cscm/wiki/Netbeans%20plugin/attachment/letter_F.png)  Full text search  
![Keyword search](https://sourceforge.net/p/cscm/wiki/Netbeans%20plugin/attachment/letter_k.png)  Keyword search  
  
### Options
![img1](https://a.fsdn.com/con/app/proj/cscm/screenshots/example4.png/1)  
To post code snippets to community server or team server with enabled authentication, you must sign up..  
After registering, you will receive a password by mail..    
  
###  Post code snippet
Highlight the code and click the right mouse button and select **Create community help**  
  
![img2](https://a.fsdn.com/con/app/proj/cscm/screenshots/example2.png/1)
  
### Editor toolbar ![img5](https://sourceforge.net/p/cscm/wiki/Netbeans%20plugin/attachment/toolbar.png)
![img3](https://sourceforge.net/p/cscm/wiki/Netbeans%20plugin/attachment/server_selector.png) Select source for code snippets search. (community server, team server, local database)

![img4](https://sourceforge.net/p/cscm/wiki/Netbeans%20plugin/attachment/search_selector.png) Select search type. (by key, full text)


### Full text code completion  
Default ist search for decription and the whole line used.  After highlighting some text and ctrl-space the highlighted text ist searched in code section of snippet..  
  
## Team server  
### Server start:
java -jar CodeSnippetsServer.jar /path/to/config.ini

### config.ini

[SERVER]  
PORT = 3232 ;server port
TURN_OFF_AUTHENTICATION = false ; enable/disable user autentication for post code snippets  

[SMTP]  
ENABLE = false ; enable/disable smtp for user autentication must enabled  
SERVER =   
USER =   
PASSWORD =  
MAIL_ADDRESS =   
  
[ADMIN]  
ENABLE = false ;enable/disable admin email info    
MAIL = user@user.com  
MAIL_ON_USER_ADD = true  
MAIL_ON_HELP_ADD = true  
SEND_DAILY_STATISTICS = true  

[DATABASE]  
DIRECTORY = /ssd/codesnippets ;H2 database directory  