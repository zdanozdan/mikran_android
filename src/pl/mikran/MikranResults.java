package pl.mikran;

import android.graphics.drawable.Drawable;

public class MikranResults
{
	public String productName; 			// nazwa produktu
	public String availableText; 		// informacja o dostêpnoœci
	public String companyName; 			// nazwa producenta
	public String nettoText; 			// cena netto
	public String bruttoText;			// cena brutto
	public String photo_pathText; 		// link do fotografii
	public String numStore;				// liczba w magazynie
	public Drawable image = null;		// Obiekt w którym wyœwietlany jest obrazek
	public String productID;			// obecnie wpisany id
	public SearchThread thread = null;		// w¹tek do œci¹gania danych
	public static Response appState = Response.DEFAULT;	// aktualny stan ekranu (odpowiedŸ servera lub ekran powitalny)
	
}
