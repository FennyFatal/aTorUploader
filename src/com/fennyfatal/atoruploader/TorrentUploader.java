package com.fennyfatal.atoruploader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import android.content.Context;
import android.content.SharedPreferences;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.Toast;

@SuppressLint("SdCardPath")
public class TorrentUploader extends AsyncTask<TorrentUploadARGS, Void, Boolean> {
	private Context parentContext;
	UIHandler uIHandler;
	HandlerThread uiThread = new HandlerThread("UIHandler");
	
	public TorrentUploader(Context theContext)
	{
		HandlerThread uiThread = new HandlerThread("UIHandler");
	    uiThread.start();
	    uIHandler = new UIHandler(uiThread.getLooper());
		parentContext = theContext;
	}
	protected HttpClient constructClient() {
	    HttpParams params = new BasicHttpParams();
	    params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

	    // use the debug proxy to view internet traffic on the host computer

	    // ignore ssl certification (due to signed authority not appearing on android list of permitted authorities)
	    // see: http://blog.antoine.li/2010/10/22/android-trusting-ssl-certificates/
	    SchemeRegistry registry = new SchemeRegistry();
	    registry.register(new Scheme("http", new PlainSocketFactory(), 80));
	    registry.register(new Scheme("https", new PlainSocketFactory(), 443)); 
	    ClientConnectionManager cm = new SingleClientConnManager(params, registry);

	    return new DefaultHttpClient(cm, params);
	}
	@Override
	protected Boolean doInBackground(TorrentUploadARGS... arg0) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(parentContext);
		String rawServer = prefs.getString("server_url", "fail");
		HttpClient httpclient = new DefaultHttpClient();
		String upload_path = arg0[0].fileName;
		URI uri;
		try {
			uri = new URI(arg0[0].uploadURI);
			byte[] torrentFileByteArray = null;
			String filename = "";
			if ( uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https"))
			{
				HttpUriRequest torGetter = new HttpGet(uri); 
				HttpResponse myTorrent = httpclient.execute(torGetter);
				HttpEntity ent = myTorrent.getEntity();
				torrentFileByteArray = EntityUtils.toByteArray(ent);
				try {
				HeaderElement[] helems = myTorrent.getFirstHeader("content-disposition").getElements();
				for (HeaderElement element: helems)
				{
					if (element.getName().equalsIgnoreCase("attachment"))
						filename = element.getParameterByName("filename").getValue();
				}
				}
				finally {}
				if (filename.equalsIgnoreCase(""))
					filename = "temp.torrent";
			}
			else if ( uri.getScheme().equalsIgnoreCase("file"))
			{
				File data = new File(uri.getPath());
			    BufferedInputStream is = new BufferedInputStream(new FileInputStream(data));
			    torrentFileByteArray =  new byte[(int)data.length()];
			    is.read(torrentFileByteArray);
				filename = uri.getPath();
				filename = filename.substring(filename.lastIndexOf('/')+1);
			}
			HttpPost post = new HttpPost("http://" + TorrentUploader.getServerDomain(rawServer)+"/rutorrent/php/addtorrent.php?");
			MultipartEntity entity = new MultipartEntity();
			entity.addPart(new FormBodyPart("dir_edit", new StringBody(upload_path)));
			//entity.addPart(new FormBodyPart("torrent_file", new StringBody(torrentFileContentsString, "application/x-bittorrent", Charset.defaultCharset())));
			entity.addPart("torrent_file", new MyContentBody(torrentFileByteArray, "application/x-bittorrent", filename));
			post.setEntity(entity);
			post.addHeader("Connection", "close");
			httpclient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
			HttpResponse respo = httpclient.execute(post);
			String response = respo.getFirstHeader("Location").getValue();
			if (response.contains("Success"))
			{
				handleUIRequest("Torrent Successfully Uploaded");
				return true;
			}
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		handleUIRequest("Torrent Upload Failure");
		return false;
	}
	
	@SuppressLint("DefaultLocale")
	public static String getServerDomain(String rawServer) {
		String[] RemoveAtTheStart = {"http://","ftp://","https://"};
		for (String s: RemoveAtTheStart)
			if (rawServer.toLowerCase().startsWith(s))
				rawServer = rawServer.substring(s.length());
		if (rawServer.contains("/"))
			rawServer = rawServer.substring(0, rawServer.indexOf('/'));
		return rawServer;
	}

	
	private final class UIHandler extends Handler
	{
	    public static final int DISPLAY_UI_TOAST = 0;
	    public static final int DISPLAY_UI_DIALOG = 1;

	    @SuppressLint("HandlerLeak")
		public UIHandler(Looper looper)
	    {
	        super(looper);
	    }

	    @Override
	    public void handleMessage(Message msg)
	    {
	        switch(msg.what)
	        {
	        case UIHandler.DISPLAY_UI_TOAST:
	        {
	            Context context = parentContext;
	            Toast t = Toast.makeText(context, (String)msg.obj, Toast.LENGTH_SHORT);
	            t.show();
	        }
	        case UIHandler.DISPLAY_UI_DIALOG:
	            //TBD
	        default:
	            break;
	        }
	    }
	}

	protected void handleUIRequest(String message)
	{
	    Message msg = uIHandler.obtainMessage(UIHandler.DISPLAY_UI_TOAST);
	    msg.obj = message;
	    uIHandler.sendMessage(msg);
	}
}
