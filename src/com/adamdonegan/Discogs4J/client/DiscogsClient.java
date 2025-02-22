package com.adamdonegan.Discogs4J.client;

import com.adamdonegan.Discogs4J.models.AuthenticationType;
import com.adamdonegan.Discogs4J.util.HttpRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class DiscogsClient {
	
	//Authorization endpoints for OAuth
	public static final String URL_REQUEST_TOKEN = "https://api.discogs.com/oauth/request_token";
	public static final String URL_AUTHORIZE = "https://discogs.com/oauth/authorize";
	public static final String URL_ACCESS_TOKEN = "https://api.discogs.com/oauth/access_token";
	
	//Database
	public static final String URL_RELEASE = "https://api.discogs.com/releases/{release_id}";
	public static final String URL_MASTER_RELEASE = "https://api.discogs.com/masters/{master_id}";
	public static final String URL_MASTER_RELEASE_VERSIONS = "https://api.discogs.com/masters/{master_id}/versions";
	public static final String URL_ARTIST = "https://api.discogs.com/artists/{artist_id}";
	public static final String URL_ARTIST_RELEASES = "https://api.discogs.com/artists/{artist_id}/releases";
	public static final String URL_LABEL = "https://api.discogs.com/labels/{label_id}";
	public static final String URL_LABEL_RELEASES = "https://api.discogs.com/labels/{label_id}/releases";
	public static final String URL_SEARCH = "https://api.discogs.com/database/search?q={query}";
	
	//User Identity
	public static final String URL_USER_IDENTITY = "https://api.discogs.com/oauth/identity";
	public static final String URL_USER_PROFILE = "https://api.discogs.com/users/{username}";
	
	//User Collection
	public static final String URL_COLLECTION = "https://api.discogs.com/users/{username}/collection/folders";
	public static final String URL_COLLECTION_FOLDER = "https://api.discogs.com/users/{username}/collection/folders/{folder_id}";
	public static final String URL_COLLECTION_RELEASES = "https://api.discogs.com/users/{username}/collection/folders/{folder_id}/releases";
	public static final String URL_ADD_RELEASE_TO_FOLDER = "https://api.discogs.com/users/{username}/collection/folders/{folder_id}/releases/{release_id}";
	public static final String URL_MODIFY_INSTANCE_IN_FOLDER = "https://api.discogs.com/users/{username}/collection/folders/{folder_id}/releases/{release_id}/instances/{instance_id}";
	
	//User Wantlist
	public static final String URL_WANTLIST = "https://api.discogs.com/users/{username}/wants";
	public static final String URL_MODIFY_WANTLIST_WITH_RELEASE = "https://api.discogs.com/users/{username}/wants/{release_id}";
	
	//Marketplace
	public static final String URL_INVENTORY ="https://api.discogs.com/users/{username}/inventory";
	public static final String URL_LISTING = "https://api.discogs.com/marketplace/listings/{listing_id}";

    //OAuth
	private static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
	private static final String OAUTH_NONCE = "oauth_nonce";
	private static final String OAUTH_SIGNATURE = "oauth_signature";
	private static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
	private static final String OAUTH_SIGNATURE_METHOD_VALUE = "PLAINTEXT";
	private static final String OAUTH_TIMESTAMP = "oauth_timestamp";
	private static final String OAUTH_ACCESS_TOKEN = "oauth_token";
	private static final String OAUTH_CALLBACK = "oauth_callback";
	private static final String OAUTH_VERIFIER = "oauth_verifier";

    //Discogs Authorization
    private static final String DISCOGS_CONSUMER_KEY = "key";
    private static final String DISCOGS_CONSUMER_SECRET = "secret";
    private static final String DISCOGS_PERSONAL_TOKEN = "token";

    private AuthenticationType authenticationType;

	private boolean debugEnabled = false;
    private int connectTimeout = 0;
    private int readTimeout = 0;
	
	private String consumerKey = "";
	private String consumerSecret = "";
	private String userAgent = "";
	private String callbackUrl = "";
	
	private String requestToken = "";
	private String requestTokenSecret = "";
	private String accessVerifier = "";
	
	private String oauthToken = "";
	private String oauthTokenSecret = "";

    private String personalAccessToken = "";

    /**
     * Create a new client with OAuth authorization
     * @param consumer_key application consumer key
     * @param consumer_secret application consumer secret
     * @param user_agent application user agent
     * @param callback_url oauth callback url
     */
	public DiscogsClient (String consumer_key, String consumer_secret, String user_agent, String callback_url){
		consumerKey = consumer_key;
		consumerSecret = consumer_secret;
		userAgent = user_agent;
		callbackUrl = callback_url;
        authenticationType = AuthenticationType.OAUTH;
	}

    /**
     * Create a new client with OAuth authorization
     * @param consumer_key application consumer key
     * @param consumer_secret application consumer secret
     * @param user_agent application user agent
     * @param oauth_token oauth flow token
     * @param oauth_token_secret oauth flow token secret
     */
	public DiscogsClient (String consumer_key, String consumer_secret, String user_agent, String oauth_token, String oauth_token_secret){
		consumerKey = consumer_key;
		consumerSecret = consumer_secret;
		userAgent = user_agent;
		oauthToken = oauth_token;
		oauthTokenSecret = oauth_token_secret;
        authenticationType = AuthenticationType.OAUTH;
	}

    /**
     * Create a new client with Discogs authorization
     * @param user_agent application user agent
     * @param consumer_key application consumer key
     * @param consumer_secret application consumer secret
     */
    public DiscogsClient (String user_agent, String consumer_key, String consumer_secret) {
        userAgent = user_agent;
        consumerKey = consumer_key;
        consumerSecret = consumer_secret;
        authenticationType = AuthenticationType.DISCOGS_CONSUMER_AUTH;
    }

    /**
     * Create a new client with Discogs authorization
     * @param user_agent application user agent
     * @param personal_access_token user personal access token
     */
    public DiscogsClient (String user_agent, String personal_access_token) {
        userAgent = user_agent;
        personalAccessToken = personal_access_token;
        authenticationType = AuthenticationType.DISCOGS_PERSONAL_AUTH;
    }

    /**
     * Create a new client without authorization
     * @param user_agent application user agent
     */
	public DiscogsClient (String user_agent) {
		userAgent = user_agent;
        authenticationType = AuthenticationType.NONE;
	}
	
	public String genericGet(String URL) {
		HttpRequest request = createGetRequest(URL).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());

		return request.body();
	}

    public String genericPost(String URL, Map<String, String> params) {
		HttpRequest request = createPostRequest(URL, true).authorization(authenticatedHeader()).userAgent(userAgent).contentType(HttpRequest.CONTENT_TYPE_JSON).send(mapToJson(params));
		debugLog(request.toString());

		return request.body();
	}

    public String genericDelete(String URL) {
		HttpRequest request = createDeleteRequest(URL, true).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());

		if(request.noContent()){
			debugLog(Integer.toString(request.code()));
			return Integer.toString(request.code()) + " No Content";
		}
		
		return request.body();
	}

    /**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/oauth/identity
	 * params: none
	 */
	public String identity() {
		HttpRequest request = createGetRequest(URL_USER_IDENTITY).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());
        
		return request.body();
	}
	
	/**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/users/{username}
	 * params: username
	 */
	public String profile(String username)
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		HttpRequest request = createGetRequest(replaceURLParams(URL_USER_PROFILE, params)).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());
        
		return request.body();
	}
	
	public String updateProfile(String username, Map<String, String> extraParams)
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		HttpRequest request = createPostRequest(replaceURLParams(URL_USER_PROFILE, params), true).authorization(authenticatedHeader()).userAgent(userAgent).contentType(HttpRequest.CONTENT_TYPE_JSON).send(mapToJson(extraParams));
		debugLog(request.toString());

		return request.body();
	}
	
	/**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/database/search?q={query}
	 * params: query
	 */
	public String search(String query) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("query", query);
		HttpRequest request = createGetRequest(replaceURLParams(URL_SEARCH, params), true).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());

		return request.body();
	}

	public String advancedSearch(String query, Map<String, String> extraParams)
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("query", query);
		HttpRequest request = createGetRequest(replaceURLParams(URL_SEARCH, params), extraParams, true).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());

		return request.body();
	}
	
	/**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/releases/{release_id}
	 * params: release_id
	 */
	public String release(String release_id) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("release_id", release_id);
		HttpRequest request = createGetRequest(replaceURLParams(URL_RELEASE, params)).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());
        
		return request.body();
	}
	
	/**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/masters/{master_id}
	 * params: master_id
	 */
	public String masterRelease(String master_id) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("master_id", master_id);
		HttpRequest request = createGetRequest(replaceURLParams(URL_MASTER_RELEASE, params)).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());
        
		return request.body();
	}
	
	/**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/masters/{master_id}/versions
	 * params: master_id
	 */
	public String masterReleaseVersions(String master_id, Map<String, String> extraParams) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("master_id", master_id);
		HttpRequest request = createGetRequest(replaceURLParams(URL_MASTER_RELEASE_VERSIONS, params), extraParams, true).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());

		return request.body();
	}
	
	public String masterReleaseVersions(String master_id)
	{
		return masterReleaseVersions(master_id, null);
	}
	
	/**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/artists/{artist_id}
	 * params: artist_id
	 */
	public String artist(String artist_id) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("artist_id", artist_id);
		HttpRequest request = createGetRequest(replaceURLParams(URL_ARTIST, params)).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());
        
		return request.body();
	}
	
	/**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/artists/{artist_id}/releases
	 * params: artist_id
	 */
	public String artistReleases(String artist_id, Map<String, String> extraParams) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("artist_id", artist_id);
		HttpRequest request = createGetRequest(replaceURLParams(URL_ARTIST_RELEASES, params), extraParams, true).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());

		return request.body();
	}
	
	public String artistReleases(String artist_id)
	{
		return artistReleases(artist_id, null);
	}
	
	/**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/labels/{label_id}
	 * params: label_id
	 */
	public String label(String label_id) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("label_id", label_id);
		HttpRequest request = createGetRequest(replaceURLParams(URL_LABEL, params)).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());
        
		return request.body();
	}
	
	/**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/labels/{label_id}/releases
	 * params: label_id
	 */
	public String labelReleases(String label_id, Map<String, String> extraParams) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("label_id", label_id);
		HttpRequest request = createGetRequest(replaceURLParams(URL_LABEL_RELEASES, params), extraParams, true).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());
        
		return request.body();
	}
	
	public String labelReleases(String label_id)
	{
		return labelReleases(label_id, null);
	}
	
	/**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/users/{username}/collection/folders
	 * params: username
	 */
	public String collection(String username) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		HttpRequest request = createGetRequest(replaceURLParams(URL_COLLECTION, params)).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());
        
		return request.body();
	}
    
	public String addCollectionFolder(String username, String folderName)
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		Map<String, String> extraParams = new HashMap<String, String>();
		extraParams.put("name", folderName);
		HttpRequest request = createPostRequest(replaceURLParams(URL_COLLECTION, params)).authorization(authenticatedHeader()).userAgent(userAgent).contentType(HttpRequest.CONTENT_TYPE_JSON).send(mapToJson(extraParams));
		debugLog(request.toString());

		return request.body();
	}
	
	/**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/users/{username}/collection/folders/{folder_id}
	 * params: username, folder_id
	 */
	public String collectionFolder(String username, String folder_id) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("folder_id", folder_id);
		HttpRequest request = createGetRequest(replaceURLParams(URL_COLLECTION_FOLDER, params)).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());
        
		return request.body();
	}
	
	public String updateCollectionFolder(String username, String folder_id, Map<String, String> extraParams)
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("folder_id", folder_id);
		HttpRequest request = createPostRequest(replaceURLParams(URL_COLLECTION_FOLDER, params)).authorization(authenticatedHeader()).userAgent(userAgent).contentType(HttpRequest.CONTENT_TYPE_JSON).send(mapToJson(extraParams));
	
		return request.body();
	}
	
	/**----------------------------------------------
	 * method: DELETE
	 * URL   : https://api.discogs.com/users/{username}/collection/folders/{folder_id}
	 * params: username, folder_id
	 */
	public String deleteCollectionFolder(String username, String folder_id) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("folder_id", folder_id);
		HttpRequest request = createDeleteRequest(replaceURLParams(URL_COLLECTION_FOLDER, params)).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());

		if(request.noContent()){
			debugLog(Integer.toString(request.code()));
			return Integer.toString(request.code()) + " No Content";
		}
        
		return request.body();
	}
	
	/**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/users/{username}/collection/folders/{folder_id}/releases
	 * params: username, folder_id
	 */
	public String collectionReleases(String username, String folder_id, Map<String, String> extraParams) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("folder_id", folder_id);
		HttpRequest request = createGetRequest(replaceURLParams(URL_COLLECTION_RELEASES, params), extraParams, true).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());
        
		return request.body();
	}
	
	public String collectionReleases(String username, String folder_id)
	{
		return collectionReleases(username, folder_id, null);
	}
	
	/**----------------------------------------------
	 * method: POST
	 * URL   : https://api.discogs.com/users/{username}/collection/folders/{folder_id}/releases/{release_id}
	 * params: username, folder_id, release_id
	 * NOTE  : use folder_id = 1 for uncategorized
	 */
	public String addReleaseToFolder(String username, String folder_id, String release_id) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("folder_id", folder_id);
		params.put("release_id", release_id);
		HttpRequest request = createPostRequest(replaceURLParams(URL_ADD_RELEASE_TO_FOLDER, params)).authorization(authenticatedHeader()).userAgent(userAgent).send("");
		debugLog(request.toString());
		debugLog(request.code());
		
		return request.body();
	}
	
	/**----------------------------------------------
	 * method: POST
	 * URL   : https://api.discogs.com/users/{username}/collection/folders/{folder_id}/releases/{release_id}/instances/{instance_id}
	 * params: username, folder_id, release_id, instance_id
	 */
	public String updateInstanceInFolder(String username, String folder_id, String release_id, String instance_id, Map<String, String> extraParams) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("folder_id", folder_id);
		params.put("release_id", release_id);
		params.put("instance_id", instance_id);
		HttpRequest request = createPostRequest(replaceURLParams(URL_MODIFY_INSTANCE_IN_FOLDER, params)).authorization(authenticatedHeader()).userAgent(userAgent).contentType(HttpRequest.CONTENT_TYPE_JSON).send(mapToJson(extraParams));
		debugLog(request.toString());

		if(request.noContent()){
			debugLog(Integer.toString(request.code()));
			return Integer.toString(request.code()) + " No Content";
		}
		
		return request.body();
	}
    
	/**----------------------------------------------
	 * method: DELETE
	 * URL   : https://api.discogs.com/users/{username}/collection/folders/{folder_id}/releases/{release_id}/instances/{instance_id}
	 * params: username, folder_id, release_id, instance_id
	 */
	public String deleteInstanceFromFolder(String username, String folder_id, String release_id, String instance_id) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("folder_id", folder_id);
		params.put("release_id", release_id);
		params.put("instance_id", instance_id);
		HttpRequest request = createDeleteRequest(replaceURLParams(URL_MODIFY_INSTANCE_IN_FOLDER, params)).authorization(authenticatedHeader()).userAgent(userAgent);
		
		if(request.noContent()){
			debugLog(Integer.toString(request.code()));
			return Integer.toString(request.code()) + " No Content";
		}
        
		return request.body();
	}
	
	/**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/users/{username}/wants
	 * params: username
	 */
	public String wantlist(String username, Map<String, String> extraParams) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		HttpRequest request = createGetRequest(replaceURLParams(URL_WANTLIST, params), extraParams, true).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());
        
		return request.body();
	}
	
	public String wantlist(String username)
	{
		return wantlist(username, null);
	}
	
	/**----------------------------------------------
	 * method: PUT
	 * URL   : https://api.discogs.com/users/{username}/wants/{release_id}
	 * params: username, release_id
	 */
	public String addToWantlist(String username, String release_id) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("release_id", release_id);
		HttpRequest request = createPutRequest(replaceURLParams(URL_MODIFY_WANTLIST_WITH_RELEASE, params)).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());
		debugLog(request.code());
		
		return request.body();
	}

    /**----------------------------------------------
	 * method: DELETE
	 * URL   : https://api.discogs.com/users/{username}/wants/{release_id}
	 * params: username, release_id
	 */
	public String deleteFromWantlist(String username, String release_id) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("release_id", release_id);
		HttpRequest request = createDeleteRequest(replaceURLParams(URL_MODIFY_WANTLIST_WITH_RELEASE, params)).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());
		if(request.noContent()){
			debugLog(Integer.toString(request.code()));
			return Integer.toString(request.code()) + " No Content";
		}
		
		return request.body();
	}
	
	public String updateInWantlist(String username, String release_id, Map<String, String> extraParams)
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("release_id", release_id);
		HttpRequest request = createPostRequest(replaceURLParams(URL_MODIFY_WANTLIST_WITH_RELEASE, params)).authorization(authenticatedHeader()).userAgent(userAgent).contentType(HttpRequest.CONTENT_TYPE_JSON).send(mapToJson(extraParams));
		debugLog(request.toString());

		if(request.noContent()){
			debugLog(Integer.toString(request.code()));
			return Integer.toString(request.code()) + " No Content";
		}
		
		return request.body();
	}
	
	/**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/users/{username}/inventory
	 * params: username
	 */
	public String inventory(String username) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		HttpRequest request = createGetRequest(replaceURLParams(URL_INVENTORY, params)).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());
        
		return request.body();
	}
	
	/**----------------------------------------------
	 * method: GET
	 * URL   : https://api.discogs.com/marketplace/listings/{listing_id}
	 * params: listing_id
	 */
	public String listing(String listing_id) 
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("listing_id", listing_id);
		HttpRequest request = createGetRequest(replaceURLParams(URL_LISTING, params)).authorization(authenticatedHeader()).userAgent(userAgent);
		debugLog(request.toString());
        
		return request.body();
	}
	
	public String replaceURLParams(String start, Map<String, String> keysAndValues){
		String endString = start;
		
		for(String key : keysAndValues.keySet()){
			endString = endString.replace("{"+key+"}", keysAndValues.get(key));
		}
		
		return endString;
	}
	
	public String mapToJson(Map<String, String> map) {
		String mapAsJson = "";
		int index = 0;
		
		if(map == null || map.isEmpty())
			return "";
		
		mapAsJson += "{";
		for(String key : map.keySet()) {
			mapAsJson += "\""+key+"\"" + ":" + "\""+map.get(key)+"\"";
			index++;
			if(index < map.size())
				mapAsJson += ",";
			
		}
		mapAsJson += "}";
		
		return mapAsJson;
	}
	
	public Map<String, String> parseParams (String responseString) {
		Map<String, String> responseMap = new HashMap<String, String>();
		debugLog(responseString);
		String[] keysAndValues = responseString.split("&");
		
		for(int i=0; i<keysAndValues.length; i++){
			String[] keyAndValue = keysAndValues[i].split("=");
			
			responseMap.put(keyAndValue[0], keyAndValue[1]);
		}
		return responseMap;
	}
	
	public Map<String, String> optionalParamsToMap(String... params) {
		Map<String, String> map = new HashMap<String, String>();
		String key = "";
		for(int i=0; i<params.length; i++){
			if(i%2 == 0)
				key = params[i];
			else
				map.put(key, params[i]);
		}
		return map;
	}
	
	public void getRequestToken() {
		HttpRequest request = createGetRequest(HttpRequest.append(URL_REQUEST_TOKEN)).userAgent(userAgent).authorization(requestAuthorizationHeader());

		debugLog(request.toString());
		debugLog(request.code());
		Map<String, String> r = parseParams(request.body());
		String token = r.get("oauth_token");
		String token_secret = r.get("oauth_token_secret");
		debugLog(token);
		debugLog(token_secret);

		requestToken = token;
		requestTokenSecret = token_secret;
	}

    /**
     * @return OAuth Authorization URL
     */
	public String getAuthorizationURL() {
		return HttpRequest.append(URL_AUTHORIZE, "oauth_token", requestToken);
	}
	
	public void getAccessToken(String verifier) {
		accessVerifier = verifier;
		HttpRequest request = createPostRequest(URL_ACCESS_TOKEN).userAgent(userAgent).authorization(accessAuthorizationHeader()).send("");
		debugLog(request.toString());
		debugLog(request.code());
		Map<String, String> r = parseParams(request.body());
		String token = r.get("oauth_token");
		String token_secret = r.get("oauth_token_secret");
		debugLog(token);
		debugLog(token_secret);

		oauthToken = token;
		oauthTokenSecret = token_secret;
	}
    
    private HttpRequest createGetRequest(String URL) {
        HttpRequest httpRequest = HttpRequest.get(URL);
        return requestWithTimeout(httpRequest);
    }

    private HttpRequest createGetRequest(String URL, boolean encode) {
        HttpRequest httpRequest = HttpRequest.get(URL, encode);
        return requestWithTimeout(httpRequest);
    }

    private HttpRequest createGetRequest(String URL, Map<?,?> parameters, boolean encode) {
        HttpRequest httpRequest = HttpRequest.get(URL, parameters, encode);
        return requestWithTimeout(httpRequest);
    }

    private HttpRequest createPostRequest(String URL) {
        HttpRequest httpRequest = HttpRequest.post(URL);
        return requestWithTimeout(httpRequest);
    }

    private HttpRequest createPostRequest(String URL, boolean encode) {
        HttpRequest httpRequest = HttpRequest.post(URL, encode);
        return requestWithTimeout(httpRequest);
    }

    private HttpRequest createPutRequest(String URL) {
        HttpRequest httpRequest = HttpRequest.put(URL);
        return requestWithTimeout(httpRequest);
    }

    private HttpRequest createDeleteRequest(String URL) {
        HttpRequest httpRequest = HttpRequest.delete(URL);
        return requestWithTimeout(httpRequest);
    }

    private HttpRequest createDeleteRequest(String URL, boolean encode) {
        HttpRequest httpRequest = HttpRequest.delete(URL, encode);
        return requestWithTimeout(httpRequest);
    }

    private HttpRequest requestWithTimeout(HttpRequest httpRequest) {
        if (connectTimeout > 0) {
            httpRequest.connectTimeout(connectTimeout);
        }
        if (readTimeout > 0) {
            httpRequest.readTimeout(readTimeout);
        }
        return httpRequest;
    }

    private void debugLog(String logText) {
		if(isDebugEnabled()) {
			System.out.println(logText);
		}
	}

	private void debugLog(int logNumber) {
		if(isDebugEnabled()) {
			System.out.println(logNumber);
		}
	}

    /**
     * @return the value to use in the Authorization header on Discogs resource requests
     */
	public String authenticatedHeader(){
        switch (getAuthenticationType()) {
            case DISCOGS_CONSUMER_AUTH:
                return getDiscogsConsumerAuthHeader();
            case DISCOGS_PERSONAL_AUTH:
                return getDiscogsPersonalAuthHeader();
            case OAUTH:
                return getOAuthHeader();
            case NONE:
                // intentional fall-through to default
            default:
                return "";
        }
	}

    private String getDiscogsConsumerAuthHeader() {
        return "Discogs " +
                DISCOGS_CONSUMER_KEY + "=" + consumerKey + ", " +
                DISCOGS_CONSUMER_SECRET + "=" + consumerSecret;
    }

    private String getDiscogsPersonalAuthHeader() {
        return "Discogs " +
                DISCOGS_PERSONAL_TOKEN + "=" + personalAccessToken;
    }

    private String getOAuthHeader() {
        java.util.Date date= new java.util.Date();

        return "OAuth "
                + OAUTH_CONSUMER_KEY + "=\"" + consumerKey + "\", "
                + OAUTH_NONCE + "=\"" + String.valueOf(date.getTime()) + "\", "
                + OAUTH_SIGNATURE + "=\"" + consumerSecret + "&" + oauthTokenSecret + "\", "
                + OAUTH_SIGNATURE_METHOD + "=\"" + OAUTH_SIGNATURE_METHOD_VALUE + "\", "
                + OAUTH_TIMESTAMP + "=\"" + String.valueOf(date.getTime()) + "\", "
                + OAUTH_ACCESS_TOKEN + "=\"" + oauthToken + "\"";
    }

    /**
     * @return the value to use in the Authorization header on Discogs OAuth flow requests (acces_token)
     */
	public String accessAuthorizationHeader(){
		java.util.Date date= new java.util.Date();
		
		String authorization = "OAuth "
				+ OAUTH_CONSUMER_KEY + "=\"" + consumerKey + "\", "
		        + OAUTH_NONCE + "=\"" + String.valueOf(date.getTime()) + "\", "
		        + OAUTH_SIGNATURE + "=\"" + consumerSecret + "&" + requestTokenSecret + "\", "
		        + OAUTH_SIGNATURE_METHOD + "=\"" + OAUTH_SIGNATURE_METHOD_VALUE + "\", "
		        + OAUTH_TIMESTAMP + "=\"" + String.valueOf(date.getTime()) + "\", "
		        + OAUTH_VERIFIER + "=\"" + accessVerifier + "\", "
		        + OAUTH_ACCESS_TOKEN + "=\"" + requestToken + "\"";
		
		return authorization;
	}

    /**
     * @return the value to use in the Authorization header on Discogs OAuth flow requests (request_token)
     */
	public String requestAuthorizationHeader(){
		java.util.Date date= new java.util.Date();
		
		String authorization = "OAuth "
				+ OAUTH_CONSUMER_KEY + "=\"" + consumerKey + "\", "
		        + OAUTH_NONCE + "=\"" + String.valueOf(date.getTime()) + "\", "
		        + OAUTH_SIGNATURE + "=\"" + consumerSecret + "&" + "\", "
		        + OAUTH_SIGNATURE_METHOD + "=\"" + OAUTH_SIGNATURE_METHOD_VALUE + "\", "
		        + OAUTH_TIMESTAMP + "=\"" + String.valueOf(date.getTime()) + "\", "
		        + OAUTH_CALLBACK + "=\"" + callbackUrl + "\"";
								
		return authorization;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
	
	public void setRequestToken(String requestToken) {
		this.requestToken = requestToken;
	}

	public String getRequestTokenSecret() {
		return requestTokenSecret;
	}

	public void setRequestTokenSecret(String requestTokenSecret) {
		this.requestTokenSecret = requestTokenSecret;
	}

	public String getAccessVerifier() {
		return accessVerifier;
	}

	public void setAccessVerifier(String accessVerifier) {
		this.accessVerifier = accessVerifier;
	}

	public String getOauthToken() {
		return oauthToken;
	}

	public void setOauthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}

	public String getOauthTokenSecret() {
		return oauthTokenSecret;
	}

	public void setOauthTokenSecret(String oauthTokenSecret) {
		this.oauthTokenSecret = oauthTokenSecret;
	}

	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	public void setDebugEnabled(boolean debugEnabled) {
		this.debugEnabled = debugEnabled;
	}

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = (int)connectTimeout.toMillis();
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = (int)readTimeout.toMillis();
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

    public void setPersonalAccessToken(String personalAccessToken) {
        this.personalAccessToken = personalAccessToken;
    }
}
