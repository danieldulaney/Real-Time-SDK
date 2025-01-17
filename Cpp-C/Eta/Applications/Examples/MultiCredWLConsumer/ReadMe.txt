rsslMultiCredWLConsumer Application Description

--------
Summary:
--------

The purpose of this application is to demonstrate connecting and consuming 
data from an ADS device, OMM Provider application or LSEG Real-Time - 
Optimized using ValueAdd components with multiple credentials.  It is a single-threaded 
client application.

This application leverages the consumer watchlist feature provided by the
RsslReactor to provide recovery and aggregation of items. Using the 
consumer watchlist feature also enables this application to consume
from either an OMM Provider or ADS over a socket-based connection, or from
an ADS over a multicast network.  It requests items within the 
channelOpenCallback function, which are automatically requested by the
RsslReactor on its behalf when the channel connects to the providing component.

If the dictionary is found in the directory of execution, then it is loaded
directly from the file.  However, the default configuration for this application
is to request the dictionary from the provider.  Hence, no link to the dictionary
is made in the execution directory by the build script.  The user can change this
behavior by manually creating a link to the dictionary in the execution directory.

This application supports consuming Level I Market Price.

This application is intended as a basic usage example.  Some of the design 
choices were made to favor simplicity and readability over performance.  
This application is not intended to be used for measuring performance.

 
-----------------
Application Name:
-----------------

MultiCredWLConsumer

------------------
Setup Environment:
------------------

The RDMFieldDictionary and enumtype.def files located in the etc directory
can be located in the directory of execution.  If the dictionary files
cannot be found, they are requested from the provider.

Both the shared version of libcurl and the openssl libraries are needed to run this example for 
connecting and consuming data from Real-Time - Optimized. 

-------------------
Json configuration file format:
-------------------  
The Json format is 4 named arrays:
LoginMsgs : Array of login message credentials.
oAuthCredentials : Array of oauth credentials.
WSBGroups : Array of warm standby groups consisting of an active server and standby servers.
ConnectionList : Ordered list of connections. 

LoginMsgs values:
	Name : Name of the login msg, which will be used by any connection configuration.
	UserName : String user name for a login nametype of USER_NAME.  This can be used OR an AuthToken type..
	AuthToken : String authentication token for a login type of USER_AUTHN_TOKEN.
	AuthTokenExtended : String for an authentication extended buffer.
	
oAuthCredentials values:
	Name : Name of the oAuthCredential, which will be used by any connection configuration.
	UserName : String V1 Machine Id .
	Password : String V1 password associated with the machine Id.
	clientId : String V1 clientId retrieved from Eikon.  For V2, this is the service account.
	clientSecret : String client secreted associated with a V2 service account.
	clientJWK : File location of the client public JWK associated with a V2 service account.
	takeExclusiveSignOn : boolean value for the take Exclusive Sign On functionality of V1 logins.
	scope : String scope for this token.
		
WSBGroups values:
	WSBMode : string either "login" or "service", indicating that this warm standby group is using a Login or Service based configuration.
	WSBActive : object containing a channel configuration for the active warm standby group.  Please see below for the channel formatting.
	WSBStandby : Array of channel objects. Please see below for the channel formatting.
	
ConnectionList values:
	This is an ordered list of channel objects. 

Channel object format:
	Name : String channel name that is used in the application.
	Host : String host name.
	Port : String port.
	Interface : String interface.
	connType : numeric connection protocol type.  See RsslConnectionTypes in rsslTransport.h for acceptable values.
	encryptedConnType : numeric encrypted connection protocol type.  See RsslConnectionTypes in rsslTransport.h for acceptable values.
	SessionMgnt : boolean value.  If true, session management will be turned on for this channel, and the watchlist will automatically get an access token and 
				  optionally perform service discovery if host and port are not specified.
	ProxyHost : String proxy host.
	ProxyPort : String proxy port.
	ProxyUserName : String user name for proxy authentication.
	ProxyPassword : String password for proxy authentication.
	ProxyDomain : String domain for proxy authentication.
	CAStore : String certificate store for encryped connections.
	Location : String location for session management.
	oAuthCredentialName : String name of associated oAuth credential for this connection.
	LoginMsgName : String name of associated login message.
	
Example config:

{
	"LoginMsgs" : [
		{
			"Name": "Login1",
			"UserName": "User1"
		},
		{
			"Name": "Login2",
			"UserName": "User2"
		},
	],
	"OAuthCredentials" : [
		{
			"Name": "V1Login1",
			"UserName": "<Machine Id>",
			"Password": "<Password>",
			"ClientId": "<Client Id from Eikon>",
			"takeExclusiveSignOn" : true
		},
		{
			"Name": "V2Login2",
			"ClientId": "<Service Account>",
			"ClientSecret": "<Service Account Secret>",
			"Scope": "trapi"
		}
	],
	"WSBGroups" : [
		{
			"WSBMode" : "login",
			"WSBActive" : {
				"Host" : "localhost",
				"Port" : "14002",
				"LoginMsgName" : "Login1"
			},
			"WSBStandby" : [
				{
					"Host" : "localhost",
					"Port" : "14000",
					"LoginMsgName" : "Login2"
				}
			]
		}
	],
	"ConnectionList" : [
	{
	"Host" : "us-east-1-aws-3-sm.optimized-pricing-api.refinitiv.net",
	"Port" : "14002",
	"SessionMgnt" : true,
	"oAuthCredentialName" : "V2Login1",
	"ConnType": 1,
	"EncryptedConnType" : 0
	},
	{
	"Host" : "us-east-2-aws-2-med.optimized-pricing-api.refinitiv.net",
	"Port" : "14002",
	"SessionMgnt" : true,
	"oAuthCredentialName" : "V1Login1",
	"ConnType": 1,
	"EncryptedConnType" : 0
	}
	]
}

-------------------
Command line usage:
-------------------  

MultiCredWLConsumer

(runs with a default set of parameters (-configJson WSBConfig.json))

or

WatchlistConsumer or WatchlistConsumer [ -configJson <Config file name> ]
  [ -s <ServiceName>] [ -mp <MarketPrice ItemName> ] [ -mbo <MarketByOrder ItemName> ]
  [ -mbp <MarketByPrice ItemName> ] [ -x ] [ -runTime <runtime> ]

Specifying the -x option will set the ETA Transport XML tracing output to true.

The user can specify multiple instances of -mp, -mbo, -mbp, -yc, -sl and -sld,
where each occurrence is associated with a single item. For example, 
specifying "-mp TRI -mp GOOG -mbo AAPL" will issue requests for two 
MarketPrice items and one MarketByOrder item.  The -sld option is like the -sl 
option, but will request that data streams be opened for items present in the 
symbol list response.

Specifying the -runTime option controls the time the application will run
before exiting, in seconds.

- WatchlistConsumer -? displays command line options.  


-----------------
Compiling Source:
-----------------

The included makefile is set up to run from the file
locations as presented through the distribution package.
It is set up for building on the Transport API supported 
Linux platforms using the supported compilers.

The VA_CUSTOM_BUILT_LIBS value in the makefile is used to 
link in the Transport API ValueAdd components compiled from the provided
source code.  If a custom library is used, VA_CUSTOM_BUILT_LIBS 
should be set to Yes.  In addition, the user should point the 
VA_INCLUDE locations to the location where the directory of 
the current platform's library is built.

The LINKTYPE value in the makefile is used to control
whether the application is built using Transport API static or
shared libraries. The default build uses Transport API static
libraries. To use Transport API shared libraries,
set LINKTYPE=Shared.

To compile, run the gmake command.

Gmake can be obtained at http://www.gnu.org/software/make/
For windows platform, using Visual Studio, open one of the included vcxproj project
files and build.

----------------
Example Content:
----------------

Included for this application are:

- Source files.

- This document.

--------------------
Detailed Description
--------------------

rsslMultiCredWLConsumer.c - The main file for the rsslMultiCredWLConsumer application.

itemDecoder.c - Contains decoding logic for the different item domains.

rsslMultiCredWLConsumerConfig.c - Handles configuration options for the consumer application.

CLConfig.json - Example of json config for connection list.

WSBConfig.json - Example of json config for warm standby.
