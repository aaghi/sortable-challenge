package aaghi.sortable.challenge;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Application {

	static String manIngnore = " |\\.|\\(|\\)|&|_|-|TECHNOLOGY|PHOTO|GMBH|EUROPE|DEUTSCHLAND|CPFR|ELECTRONICS|CONSUMER|IMAGING|ELECTRONIC|DIVISION|DSC|COMPANY|DSLR|ALPHA|CORPORATION|SYSTEMS|DIGITAL|INC|LTD|CANADA|CO|OPTO|USA|UK|FIRSTORDER|CAMERA|COMPUTER|ACCOUNT|INSTOCK";
	static String tokenRegex = " |\\.|\\(|\\)|&|_|-";
	HashMap<String, ArrayList<Product>> productsTable;
	HashMap<String, ArrayList<String>> sysnomTable ;
	String productsFileName, listingsFileName,resultsFileName;
	int matchCount = 0, listingCount=0, productsCount=0;
	public Application(String productsFileName, String listingsFileName,String resultsFileName){
		this.productsFileName = productsFileName;
		this.listingsFileName = listingsFileName;
		this.resultsFileName = resultsFileName;
		productsTable = new HashMap<String, ArrayList<Product>>();
		sysnomTable = new HashMap<String, ArrayList<String>>();
		sysnomTable.put("HP", new ArrayList<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				add("HEWLETTPACKARD");
			}
		});
		sysnomTable.put("HEWLETTPACKARD", new ArrayList<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 2138343536643081363L;

			{
				add("HP");
			}
		});
		sysnomTable.put("EASYPIX", new ArrayList<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				add("CANON");
			}
		});
		sysnomTable.put("FUJI", new ArrayList<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				add("FUJIFILM");
				add("FUJIFINEPIX");
			}
		});
		sysnomTable.put("FUJIFINEPIX", new ArrayList<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				add("FUJIFILM");
				add("FUJI");
			}
		});
		sysnomTable.put("FUJIFILMCA", new ArrayList<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				add("FUJIFILM");
				add("FUJI");
			}
		});
		sysnomTable.put("GE", new ArrayList<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				add("GENERALELECTRIC");
			}
		});

		sysnomTable.put("VISTAQUEST", new ArrayList<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				add("AGFA");
			}
		});
		sysnomTable.put("YASHICA", new ArrayList<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				add("KYOCERA");
			}
		});
		sysnomTable.put("EASTMANKODAK", new ArrayList<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				add("KODAK");
			}
		});
	}
	private void loadProducts() throws IOException{
		System.out.println("Products file name: "+productsFileName);
		FileInputStream productsFile = new FileInputStream(productsFileName);
		Scanner productsScanner = new Scanner(productsFile);
		while (productsScanner.hasNextLine()) {
			productsCount++;
			String productLine = productsScanner.nextLine();
			Object obj = JSONValue.parse(productLine);
			JSONObject productObj = (JSONObject) obj;
			String manufacturerString = (String) productObj.get("manufacturer");
			manufacturerString = manufacturerString.toUpperCase();
			String manufacturer = manufacturerString.replaceAll(manIngnore, "");
			if (!productsTable.containsKey(manufacturer)) {
				ArrayList<Product> productsList = new ArrayList<Product>();
				productsList.add(new Product(productObj));
				productsTable.put(manufacturer, productsList);
			} else {
				ArrayList<Product> productsList = productsTable
						.get(manufacturer);
				productsList.add(new Product(productObj));
				productsTable.put(manufacturer, productsList);
			}

		}
		productsScanner.close();
		productsFile.close();
		System.out.println("Loaded "+productsCount+" products successfully");
	}
	private void processListings() throws IOException{
		System.out.println("Listings file name: "+listingsFileName);
		FileInputStream listingsFile = new FileInputStream(listingsFileName);
		Scanner listingsScanner = new Scanner(listingsFile);
		while (listingsScanner.hasNextLine()) {
			listingCount++;
			String listingLine = listingsScanner.nextLine();
			Object obj = JSONValue.parse(listingLine);
			JSONObject listingsObj = (JSONObject) obj;
			String listingTitle = (String) listingsObj
					.get("title");
			listingTitle = listingTitle.toUpperCase();
			String[] titleTokens = listingTitle.split(tokenRegex);

			String manufacturerString = (String) listingsObj
					.get("manufacturer");
			manufacturerString = manufacturerString.toUpperCase();
			manufacturerString = manufacturerString.replaceAll(manIngnore, "");
			ArrayList<Product> productsList = new ArrayList<Product>();
			ArrayList<String> sysnomTokens = new ArrayList<String>();
			if (productsTable.containsKey(manufacturerString)) {
				productsList = productsTable.get(manufacturerString);
			}
				if (sysnomTable.containsKey(manufacturerString)) {
					sysnomTokens.addAll(sysnomTable.get(manufacturerString));
					for (String token : sysnomTokens) {
						if (productsTable.containsKey(token)) {
							productsList.addAll(productsTable.get(token));
						}
						
					}
				}
			if (!productsList.isEmpty()) {

				if(matchListing(listingsObj,titleTokens, productsList)){
					matchCount++;
				}
			}

		}
		listingsScanner.close();
		listingsFile.close();
		System.out.println("Matched "+matchCount+" of "+listingCount+" lisitngs successfully");
	}

	private void printResults() throws FileNotFoundException{
		System.out.println("Printing Results to: "+resultsFileName);
		PrintStream out = new PrintStream(new FileOutputStream(resultsFileName));
		for (ArrayList<Product> products : productsTable.values()) {
			for (Product p : products) {
				out.println(p.toResult().toJSONString());
			}
		}
		out.close();
	}


	private boolean matchListing(JSONObject listing,String[] titleTokens, ArrayList<Product> productsList) {
		int maxScore = 0;
		Product maxScoreProduct = null;
		String title = (String) listing.get("title");
		title = title.toUpperCase();
		for (Product product : productsList) {
			if (!product.isListingContainModel(title)) {
				continue;
			}
			 int score = product.scoreProduct(titleTokens);
			 if(score>70 && maxScore < score){
				 maxScore = score;
				 maxScoreProduct = product;
			 }
		}
		if (maxScoreProduct != null) {
			maxScoreProduct.listings.add(listing);
			return true;
		}
		return false;
	}
	
	public void run() throws IOException{
		loadProducts();
		processListings();
		printResults();
		System.out.println("done!");
	}
	
	public static void main(String[]  args) throws IOException{
		String productsFileName="products.txt", listingsFileName="listings.txt", resultsFileName="results.txt";
		if(args.length==3){
			productsFileName = args[0];
			listingsFileName = args[1];
			resultsFileName = args[2];
		}
		new Application(productsFileName, listingsFileName,resultsFileName).run();
	}

}
