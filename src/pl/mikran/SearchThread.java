package pl.mikran;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

public class SearchThread extends Thread
{
	private boolean testServer = false;		//informacja jaki serwer bêdzie u¿ywany; false - product_server; true - test_server;
	private String test_server = "http://www.test.mikran.pl/go/_search/json_code.php?id=";
	private String product_server = "http://www.sklep.mikran.pl/go/_search/json_code.php?id=";
	private String server = "";
	public Handler handlerSearch = null;
	public boolean isUsed = false;
	private MikranResults results = null;
	
	public SearchThread(String prodId, Handler handler)
	{
		results = new MikranResults();
		results.productID = prodId;
		handlerSearch = handler;
	}
	
	public void run()
    {
		results.thread = this;
		MikranResults.appState = searchProduct();
		while(handlerSearch == null)
		{
		}
		Message oMessage = handlerSearch.obtainMessage();
		oMessage.obj = results;
		handlerSearch.sendMessage(oMessage);
	}
	
	public void setHandler(Handler handler)
	{
		handlerSearch = handler;
	}
	
	private Response searchProduct()
    {
		clearTempStrings();
    	Response retValue = Response.DEFAULT;
    	try
    	{
    		if(testServer)
    		{
    			Authenticator.setDefault(new MyAuthenticator());
    			server = test_server;
    		}
    		else
    		{
    			server = product_server;
    		}
        	URL url = new URL(server + results.productID);
        	URLConnection urlCon = url.openConnection();
        	InputStream input = (InputStream)urlCon.getContent();
        	BufferedReader in = new BufferedReader(new InputStreamReader(input));//, "ISO8859-2"));
        		
        	String allLine = "";
        	String local;
        		
        	while((local = in.readLine()) != null)
        	{
        		allLine = allLine + local;
        	}
        		
        	int index = allLine.indexOf("{\"Success\"");
        	if(index == -1)
        	{
        		index = allLine.indexOf("{\"Error\"");
        		if(index == -1)
            	{
            		retValue = Response.DATA_ERROR;
            	}
        		else
        		{
        			retValue = Response.ERROR;
        		}
        	}
        	else
        	{
        		retValue = Response.SUCCESS;
        	}
        		
        	if(index >= 0)
        	{
        		allLine = allLine.substring(index); // get only JSON content
        		try
    			{
    				JSONObject myJSONObject = new JSONObject(allLine);
    				if(Response.SUCCESS == retValue)
    				{
    					String code = myJSONObject.getJSONObject("Success").get("code").toString();
    					if(code.compareTo("200") == 0)
    					{
    						results.productName = myJSONObject.getJSONObject("Success").get("name").toString();    							
    						results.companyName = myJSONObject.getJSONObject("Success").get("producer").toString();
    						if(results.companyName.equals("") || results.companyName.equals(null))
    						{
    							results.companyName = "brak";
    						}
    						String vat = myJSONObject.getJSONObject("Success").get("vat").toString();
    						String bruttoString = myJSONObject.getJSONObject("Success").get("brutto").toString();
    						float brutto = 0;
    						if(!bruttoString.equals(""))
    						{
    							brutto = Float.parseFloat(bruttoString);
    						}
    						float netto = 0;
    						if(!vat.equals(""))
    						{
    							netto = brutto/(1+Float.parseFloat(vat)/100);
    						}
    						DecimalFormat df = new DecimalFormat("0.00");
    						results.nettoText = df.format(netto) + " PLN + " + String.valueOf(vat) + "% VAT";
    						results.bruttoText = String.valueOf(brutto) + " PLN";
    						results.photo_pathText = myJSONObject.getJSONObject("Success").get("photo_path").toString() + 
    										myJSONObject.getJSONObject("Success").get("photo_id").toString();
    							
    						if(!results.photo_pathText.equals(""))
    				    	{
    				    		loadImageFromUrl(results.photo_pathText);
    				    	}
    							
    						results.numStore = myJSONObject.getJSONObject("Success").get("num").toString();
    							
    						retValue = Response.CODE_200;
    					}
    					else
    					{
    						retValue = Response.DATA_ERROR;
    					}
    				}
    				else if(Response.ERROR == retValue)
    				{
    					String code = myJSONObject.getJSONObject("Error").get("code").toString();
    					if(code.compareTo("404") == 0)
    					{
    						retValue = Response.CODE_404;
    					}
    					else if(code.compareTo("500") == 0)
    					{
    						retValue = Response.CODE_500;
    					}
    				}
    			}
    			catch(JSONException e)
    			{
    				retValue = Response.DATA_ERROR;
    			}
    			catch(IllegalArgumentException e)
    	        {
    	        	retValue = Response.EXCEPTION_IllegalArgumentException;
    	        }
        	}
        }
    	catch (MalformedURLException e)
    	{
    		// TODO Auto-generated catch block
    		retValue = Response.EXCEPTION_MalformedURLException;
    	}
    	catch (IOException e)
    	{
    		// TODO Auto-generated catch block
    		retValue = Response.EXCEPTION_IOException;
    	}
        catch (NullPointerException e)
        {
        	retValue = Response.EXCEPTION_NullPointerException;
        }
        catch(IndexOutOfBoundsException e)
        {
        	retValue = Response.EXCEPTION_IndexOutOfBoundsException;
        }
    	return retValue;
    }
	
	private void loadImageFromUrl(String photoAddress)
    {
		results.image = null;
    	try
		{
			URL url = new URL(photoAddress);
			URLConnection urlCon = url.openConnection();
			InputStream input = (InputStream)urlCon.getContent();
			results.image = Drawable.createFromStream(input, "src name");
		}
		catch (MalformedURLException e)
        {
        }
        catch (IOException e)
        {
        }
    }
	
	private void clearTempStrings() // kasuje tymczasowe wartoœci zczytywane z JSON
    {
		results.productName = "";
		results.availableText = "";
		results.companyName = "brak";
		results.nettoText = "";
		results.bruttoText = "";
		results.numStore = "";
		results.photo_pathText = "";
		results.image = null;
    }
}
