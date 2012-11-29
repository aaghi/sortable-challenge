package aaghi.sortable.challenge;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONObject;

	public class Product{
		HashMap<String,Integer> keywords;
		String[] modelTokens;
		ArrayList<JSONObject> listings;
		String name, model;
		
		private int totalWeight = 0;
		Product(JSONObject productJson){
			keywords = new HashMap<String,Integer>();
			listings = new ArrayList<JSONObject>();
			name = (String) productJson.get("product_name");
			model = (String)productJson.get("model");
			model = model.toUpperCase();
		    modelTokens = model.split(" |_|-");
		    modelTokens[modelTokens.length-1] += " ";
			keywordAdderHelper((String) productJson.get("product_name"),1);
			keywordAdderHelper((String) productJson.get("model"),5);
			keywordAdderHelper((String) productJson.get("manufacturer"),2);
			if( productJson.containsKey("family"))
				keywordAdderHelper((String) productJson.get("family"),0);
		}
		void keywordAdderHelper(String keyword, int score){
			keyword = keyword.toUpperCase();
			String[] valueStringTokens = keyword.split(Application.tokenRegex);
				for (String valueStringToken:valueStringTokens){
				if(!keywords.containsKey(valueStringToken)){
					keywords.put(valueStringToken, score);
					totalWeight+=score;
				}
			}

		}
		public  int tokenScore(String titleToken) {
			if(keywords.containsKey(titleToken)){
				return keywords.get(titleToken);
			}
			else{
				return 0;

			}
		}
		public boolean isListingContainModel(String listingTitle){
			for(String modelToken:modelTokens){
				if(!listingTitle.contains(modelToken)){
					return false;
				}
			}
			return true;
		}
		public int getTotalWeight() {
			return totalWeight ;
		}
		int scoreProduct(String[] titleTokens) {
			int score = 0;
			for (String titleToken : titleTokens) {
				score += tokenScore(titleToken);
			}
			return (score * 100) / getTotalWeight();
		}
		public JSONObject toResult(){
			JSONObject ret = new JSONObject();
			ret.put("product_name", name);
			ret.put("listings", this.listings);
			return ret;
		}
	}