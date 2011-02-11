package pl.mikran;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.*;
import android.net.*;
import android.content.*;
import android.app.Dialog;
import android.view.inputmethod.*;

import java.net.*;

public class MikranSearch extends Activity
{
	TextView nameLabel;
	TextView companyNameLabel;
	TextView nettoPriceValueLabel;
	TextView bruttoPriceValueLabel;
	TextView numStoreValueLabel;
	TextView notFoundResultLabel;
	TextView welcomeMessageLabel;
	ImageView imageView;
	Button searchButton;
	RelativeLayout resultLayout;
	RelativeLayout descLayout;
	LinearLayout notFoundResultLayout;
	LinearLayout welcomeMessageLayout;
	EditText idEditText;
	
	private static final int DIALOG_PROGRESS = 1;
	private MikranResults results = new MikranResults();
	
	private boolean isChangedOrientation = false; 	// informuje o zmianie orientacji
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        nameLabel = (TextView) findViewById(R.id.NameLabel);
        companyNameLabel = (TextView) findViewById(R.id.CompanyNameLabel);
    	nettoPriceValueLabel = (TextView) findViewById(R.id.NettoPriceValueLabel);
    	bruttoPriceValueLabel = (TextView) findViewById(R.id.BruttoPriceValueLabel);
        numStoreValueLabel = (TextView) findViewById(R.id.NumStoreValueLabel);
        notFoundResultLabel = (TextView) findViewById(R.id.NotFoundResultLabel);
        imageView = (ImageView) findViewById(R.id.ProductImageView);
        searchButton = (Button) findViewById(R.id.SearchButton);
        resultLayout = (RelativeLayout) findViewById(R.id.ResultLayout);
        descLayout = (RelativeLayout) findViewById(R.id.DescriptionLayout);
        idEditText = (EditText) findViewById(R.id.SearchText);
        notFoundResultLayout = (LinearLayout) findViewById(R.id.NotFoundResultLayout);
        welcomeMessageLayout = (LinearLayout) findViewById(R.id.WelcomeMessageLayout);
        welcomeMessageLabel = (TextView) findViewById(R.id.WelcomeMessageLabel);
        
        welcomeMessageLabel.setText("Aplikacja Mikran s³u¿y do sprawdzania cen i dostêpnoœci towarów" +
			                        " w sklepie mikran.pl na podstawie numeru katalogowego produktu. " +
			                        "Jeœli chcia³byœ otrzymaæ nasz katalog wysy³kowy wyœlij maila na sklep@mikran.pl");
        
        searchButton.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View view)
			{
				// TODO Auto-generated method stub
				onClickSearchButton(view);
			}
		});
        
        restoreData(savedInstanceState);     
    }
    
    @Override
    public void onPostCreate(Bundle savedInstanceState)
    {
    	super.onPostCreate(savedInstanceState);
    	if(isChangedOrientation)
        {
    		isChangedOrientation = false;
        }
        else
        {
            MikranResults.appState = Response.WELCOME;
        }
    	displayData();
    }
    
    @Override
    public Object onRetainNonConfigurationInstance()
    {
    	if(null != results.thread)
    	{
    		results.thread.setHandler(null);
    	}
    	else
    	{
    		results.thread = null;
    	}
    	return results;
    }
    
    private void restoreData(Bundle inState)
    {
    	if (null != inState && null != getLastNonConfigurationInstance())
		{
    		isChangedOrientation = true;
    		results = (MikranResults)getLastNonConfigurationInstance();
    		if(null != results.thread)
    		{
    			results.thread.setHandler(handler);
    		}
		}
    }
    
    @Override
    protected Dialog onCreateDialog(int id)
    {
    	switch(id)
    	{
    		case DIALOG_PROGRESS:
    			ProgressDialog progressDialog = new ProgressDialog(MikranSearch.this);
    			progressDialog.setMessage("£¹czenie. Proszê czekaæ...");
    			return progressDialog;
    		default:
    			return null;
    	}
    }
    
    public void onClickSearchButton(View view)
    {
    	// hide soft keyboard
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(idEditText.getWindowToken(), 0);

    	boolean isProductID = !idEditText.getText().toString().equals("");//isEmpty();
    	if(!isProductID)
    	{
    		notFoundResultLabel.setText("Wyszukiwanie nie zwróci³o ¿adnych wyników.");
    		resultLayout.setVisibility(View.INVISIBLE);
            descLayout.setVisibility(View.INVISIBLE);
            notFoundResultLayout.setVisibility(View.VISIBLE);
            displayData();
    	}
    	else
    	{
    		ConnectivityManager cM = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE));
    		NetworkInfo netInfo[] = cM.getAllNetworkInfo();
    		boolean connected = false;
    		for(int i=0; i < netInfo.length; i++)
    		{
    			if((netInfo[i] != null) && (netInfo[i].isConnected()))
        		{
    				connected = true;
    				results.thread = new SearchThread(idEditText.getText().toString(), handler);
        			results.thread.start();
    				
        			showDialog(DIALOG_PROGRESS);
        			break;
        		}
    		}
    		if(!connected)
    		{
    			MikranResults.appState = Response.NO_INTERNET_CONNECTION;
    			displayData();
    		}
    		/*if((netInfo != null) && (netInfo.isConnected()))
    		{    			
    			myThread = new SearchThread(idEditText.getText().toString(), handler);
    			myThread.start();
    			showDialog(DIALOG_PROGRESS);
    		}
    		else
    		{
    			MikranResults.httpResponse = Response.NO_INTERNET_CONNECTION;
    		}*/	
    	}
    }
    
    final private Handler handler = new Handler()
    {
		@Override
		public void handleMessage(Message msg)
		{
			welcomeMessageLayout.setVisibility(View.INVISIBLE);
			results = (MikranResults)msg.obj;
			displayData();
			try
			{
				dismissDialog(DIALOG_PROGRESS);
			}
			catch(IllegalArgumentException e)
			{			
			}
		}
	};
    
    private void displayData()
    {
    	if(Response.CODE_200 == MikranResults.appState) // dostêpny
    	{
    		notFoundResultLayout.setVisibility(View.INVISIBLE);
    		welcomeMessageLayout.setVisibility(View.INVISIBLE);
    		numStoreValueLabel.setText("dostêpny z magazynu");
    		resultLayout.setVisibility(View.VISIBLE);
    		descLayout.setVisibility(View.VISIBLE);
    		
    		nameLabel.setText(results.productName);
    		companyNameLabel.setText(results.companyName);
    		nettoPriceValueLabel.setText(results.nettoText);
    		bruttoPriceValueLabel.setText(results.bruttoText);
    		numStoreValueLabel.setText(results.numStore + " szt. w magazynie");
    		
    		imageView.setImageDrawable(results.image);		
    	}
    	else if(Response.CODE_404 == MikranResults.appState)
    	{
    		resultLayout.setVisibility(View.INVISIBLE);
            descLayout.setVisibility(View.INVISIBLE);
            welcomeMessageLayout.setVisibility(View.INVISIBLE);
            notFoundResultLayout.setVisibility(View.VISIBLE);
            notFoundResultLabel.setText("Wyszukiwanie nie zwróci³o ¿adnych wyników dla kodu " + results.productID + ".");
    	}
    	else if(Response.CODE_500 == MikranResults.appState)
    	{
    		resultLayout.setVisibility(View.INVISIBLE);
            descLayout.setVisibility(View.INVISIBLE);
            welcomeMessageLayout.setVisibility(View.INVISIBLE);
            notFoundResultLayout.setVisibility(View.VISIBLE);
            notFoundResultLabel.setText("ID produktu powinno byæ liczb¹.");
    	}
    	else if(Response.DATA_ERROR == MikranResults.appState)
    	{
    		resultLayout.setVisibility(View.INVISIBLE);
            descLayout.setVisibility(View.INVISIBLE);
            welcomeMessageLayout.setVisibility(View.INVISIBLE);
            notFoundResultLayout.setVisibility(View.VISIBLE);
            notFoundResultLabel.setText("B³¹d danych.");
    	}
    	else if(Response.NO_INTERNET_CONNECTION == MikranResults.appState)
    	{
    		resultLayout.setVisibility(View.INVISIBLE);
            descLayout.setVisibility(View.INVISIBLE);
            welcomeMessageLayout.setVisibility(View.INVISIBLE);
            notFoundResultLayout.setVisibility(View.VISIBLE);
            notFoundResultLabel.setText("Brak po³¹czenia internetowego.");
    	}
    	else if(Response.EXCEPTION_NullPointerException == MikranResults.appState)
    	{
    		resultLayout.setVisibility(View.INVISIBLE);
            descLayout.setVisibility(View.INVISIBLE);
            welcomeMessageLayout.setVisibility(View.INVISIBLE);
            notFoundResultLayout.setVisibility(View.VISIBLE);
            notFoundResultLabel.setText("B³¹d danych.");
    	}
    	else if(Response.EXCEPTION_IOException == MikranResults.appState)
    	{
    		resultLayout.setVisibility(View.INVISIBLE);
            descLayout.setVisibility(View.INVISIBLE);
            welcomeMessageLayout.setVisibility(View.INVISIBLE);
            notFoundResultLayout.setVisibility(View.VISIBLE);
            notFoundResultLabel.setText("Brak danych.");
    	}
    	else if(Response.EXCEPTION_MalformedURLException == MikranResults.appState)
    	{
    		resultLayout.setVisibility(View.INVISIBLE);
            descLayout.setVisibility(View.INVISIBLE);
            welcomeMessageLayout.setVisibility(View.INVISIBLE);
            notFoundResultLayout.setVisibility(View.VISIBLE);
            notFoundResultLabel.setText("B³¹d w adresie serwera.");
    	}
    	else if(Response.EXCEPTION_UnsupportedEncodingException == MikranResults.appState)
    	{
    		resultLayout.setVisibility(View.INVISIBLE);
            descLayout.setVisibility(View.INVISIBLE);
            welcomeMessageLayout.setVisibility(View.INVISIBLE);
            notFoundResultLayout.setVisibility(View.VISIBLE);
            notFoundResultLabel.setText("B³êdne kodowanie danych.");
    	}
    	else if(Response.EXCEPTION_IndexOutOfBoundsException == MikranResults.appState)
    	{
    		resultLayout.setVisibility(View.INVISIBLE);
            descLayout.setVisibility(View.INVISIBLE);
            welcomeMessageLayout.setVisibility(View.INVISIBLE);
            notFoundResultLayout.setVisibility(View.VISIBLE);
            notFoundResultLabel.setText("B³¹d danych.");
    	}
    	else if(Response.EXCEPTION_IllegalArgumentException == MikranResults.appState)
    	{
    		resultLayout.setVisibility(View.INVISIBLE);
            descLayout.setVisibility(View.INVISIBLE);
            welcomeMessageLayout.setVisibility(View.INVISIBLE);
            notFoundResultLayout.setVisibility(View.VISIBLE);
            notFoundResultLabel.setText("B³¹d danych.");
    	}
    	else if(Response.WELCOME == MikranResults.appState)
    	{
    		welcomeMessageLayout.setVisibility(View.VISIBLE);
    		resultLayout.setVisibility(View.INVISIBLE);
            descLayout.setVisibility(View.INVISIBLE);
            notFoundResultLayout.setVisibility(View.INVISIBLE);
            idEditText.setText("");
    	}
    	else
    	{
    		resultLayout.setVisibility(View.INVISIBLE);
            descLayout.setVisibility(View.INVISIBLE);
            notFoundResultLayout.setVisibility(View.INVISIBLE);
            welcomeMessageLayout.setVisibility(View.INVISIBLE);
    	}
    }
}



class MyAuthenticator extends Authenticator
{
  protected PasswordAuthentication getPasswordAuthentication()
  {
    String username = "tkowalski";
    String password = "MIkran123";
    return new PasswordAuthentication(username, password.toCharArray());
  }
}