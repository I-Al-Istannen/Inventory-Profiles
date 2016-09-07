package me.ialistannen.ip_sign_shop.conversations;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;

/**
 * Asks the user if he really wants to do something.
 */
public class ConfirmationConversation extends StringPrompt {

	private final String promptText;
	
	/**
	 * @param promptText The - already colored - prompt text.
	 */
	public ConfirmationConversation(String promptText) {
		this.promptText = promptText;
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		if(input.equalsIgnoreCase(tr("confirmation conversation yes"))) {
			context.setSessionData("result", true);
			return null;
		}
		else if(input.equalsIgnoreCase(tr("confirmation conversation no"))) {
			context.setSessionData("result", false);
			return null;
		}
		
		// user did something wrong
		return this;
	}

	@Override
	public String getPromptText(ConversationContext context) {
		return promptText;
	}

}
