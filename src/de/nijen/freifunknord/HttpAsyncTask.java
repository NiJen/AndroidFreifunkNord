package de.nijen.freifunknord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class HttpAsyncTask extends AsyncTask<String, Integer, String> {

private String url;
private ArrayList<HashMap<String, String>> result;
private FreifunkNord ajt;
private ProgressDialog dialog;

	public HttpAsyncTask(String url, ArrayList<HashMap<String, String>> result, FreifunkNord ajt) {
		this.url = url;
		this.result = result;
		this.ajt = ajt;
	}

	@Override
	protected String doInBackground(String... arg0) {

		String displayString = null;
		HttpParams httpParameters = new BasicHttpParams();
		httpParameters.setIntParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 5000);
		HttpClient client = new DefaultHttpClient(httpParameters);
		HttpGet httpget = new HttpGet(url);
		try {
			HttpResponse response = client.execute(httpget);
			StatusLine statusline = response.getStatusLine();
			int statusCode = statusline.getStatusCode();
			if (statusCode == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

				StringBuffer sb = new StringBuffer("");
				String line = "";
				String NL = System.getProperty("line.separator");
				while ((line = in.readLine()) != null) {
					sb.append(line + NL);
				}
				in.close();
				String page = sb.toString();
				Log.i("page in synce -->", page);
				 Nodes bd = new Nodes();
				result = bd.getDisplayString(page);
				

			} else {
				Log.e(FreifunkNord.class.toString(), "Failed to download file");
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(FreifunkNord.class.toString(), "Http Async task Io exception");
			publishProgress(1);
			e.printStackTrace();
		}
	return displayString;
	}

	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(ajt, ProgressDialog.STYLE_SPINNER);
		dialog.setTitle("Getting data");
		dialog.setMessage("Please, wait...");
		dialog.show();
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		Toast.makeText(ajt, "Can not conntection to Internet, try later", Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPostExecute(String result) {
		dialog.dismiss();
		ajt.displayResults(this.result);
	}
}