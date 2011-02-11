package pl.mikran;

import android.graphics.drawable.Drawable;

public class MikranResults
{
	public String productName; 			// nazwa produktu
	public String availableText; 		// informacja o dost�pno�ci
	public String companyName; 			// nazwa producenta
	public String nettoText; 			// cena netto
	public String bruttoText;			// cena brutto
	public String photo_pathText; 		// link do fotografii
	public String numStore;				// liczba w magazynie
	public Drawable image = null;		// Obiekt w kt�rym wy�wietlany jest obrazek
	public String productID;			// obecnie wpisany id
	public SearchThread thread = null;		// w�tek do �ci�gania danych
	public static Response appState = Response.DEFAULT;	// aktualny stan ekranu (odpowied� servera lub ekran powitalny)
	
}
