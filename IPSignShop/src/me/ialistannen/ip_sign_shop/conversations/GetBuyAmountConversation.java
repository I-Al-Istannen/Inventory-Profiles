package me.ialistannen.ip_sign_shop.conversations;

import static me.ialistannen.ip_sign_shop.util.Language.tr;

import java.text.NumberFormat;
import java.text.ParseException;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import me.ialistannen.ip_sign_shop.datastorage.ShopMode;
import me.ialistannen.ip_sign_shop.util.Language;

/**
 * The conversation prompting you for the amount of things you want to buy
 * <br>
 * The amount will be saved in the session data under "amount" and is an integer
 */
public class GetBuyAmountConversation extends StringPrompt {

	private String suffix;
	private ShopMode shopMode;

	
	/**
	 * @param shopMode The mode the shop is in with the appropiate chat color. If none specified the deafult is AQUA
	 * @param suffix The suffix to append at the end
	 */
	public GetBuyAmountConversation(ShopMode shopMode, String suffix) {
		this.shopMode = shopMode;
		this.suffix = suffix;
	}


	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		Integer amount = getInt(input);
		if(amount == null) {
			context.getForWhom().sendRawMessage(tr("not a number", input));
			return this;
		}
		
		context.setSessionData("amount", amount);
		return null;
	}
	
	
	/**
	 * Parses with base 10 and 16
	 * 
	 * @param input The String to parse
	 * @return The parsed int or null if not a number.
	 */
	private Integer getInt(String input) {
		NumberFormat format = NumberFormat.getNumberInstance(Language.getLocale());
		format.setParseIntegerOnly(true);
		
		try {
			return format.parse(input).intValue();
		} catch(ParseException e) {
		}
		
		try {
			return Integer.parseInt(input, 16);
		} catch(NumberFormatException e) {			
		}
		
		return null;
	}

	@Override
	public String getPromptText(ConversationContext context) {
		String shopMode = this.shopMode.name().toLowerCase().contains("sell") ? tr("buy amount conversation sell") : tr("buy amount conversation buy");
		
		return tr("buy amount conversation prompt text", shopMode, suffix);
	}

}
