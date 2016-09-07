package me.ialistannen.ip_sign_shop.conversations;

import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.ShopMode;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import java.text.NumberFormat;
import java.text.ParseException;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;

/**
 * The Create shop conversation
 */
public class GetTradingPriceConversation extends StringPrompt {

	private final String itemToTrade;

	private ConversationStage stage = ConversationStage.SHOP_MODE;
	
	/**
	 * @param itemToTrade The name of the item to sell
	 */
	public GetTradingPriceConversation(String itemToTrade) {
		this.itemToTrade = itemToTrade;
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		if(stage == ConversationStage.SHOP_MODE) {
			String sell = tr("buy amount conversation sell");
			String buy = tr("buy amount conversation buy");
			
			if(input.trim().equalsIgnoreCase(sell.trim())) {
				context.setSessionData("mode", ShopMode.SELL);
			}
			else if(input.trim().equalsIgnoreCase(buy.trim())) {
				context.setSessionData("mode", ShopMode.BUY);				
			}
			else {
				return this;
			}
			
			// all went well user is not a goat, now continue with the other stage
			stage = ConversationStage.PRICE;
			return this;
		}
		else {
			Double inputNumber = getDouble(input);
			if(inputNumber == null) {
				return this;
			}
			
			// round to two decimal digits (is a currency value)
			inputNumber = (Math.round(inputNumber * 100.0) / 100.0);
			context.setSessionData("amount", inputNumber);

			return null;	// done with it
		}
	}
	
	/**
	 * @param input The input
	 * @return The number or null if not valid
	 */
	private Double getDouble(String input) {
		try {
			return NumberFormat.getNumberInstance(IPSignShop.getInstance().getLanguage().getLanguage()).parse(input).doubleValue();
		} catch (ParseException e) {
			return null;
		}
	}
	
	@Override
	public String getPromptText(ConversationContext context) {
		if(stage == ConversationStage.PRICE) {
			return tr("trading price conversation prompt text", itemToTrade);
		}
		else {
			return tr("trading price conversation prompt text shop mode", itemToTrade, tr("buy amount conversation sell"), tr("buy amount conversation buy"));
		}
	}
	
	private enum ConversationStage {
		/**
		 * Getting the Shop mode from the user
		 */
		SHOP_MODE,
		/**
		 * Getting the price from the user
		 */
		PRICE
	}
	
}
