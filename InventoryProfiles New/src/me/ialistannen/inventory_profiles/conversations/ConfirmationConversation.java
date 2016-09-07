package me.ialistannen.inventory_profiles.conversations;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import java.util.regex.Pattern;

import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Wants the user to confirm something. The resulting boolean will be saved in the SessionData with the key "result".
 * <br>Already sends the "confirmation conversation picked no option" message.
 */
class ConfirmationConversation extends StringPrompt {

	private final String promptText;
	private final Pattern yesPattern = Pattern.compile(tr("confirmation conversation yes"), Pattern.CASE_INSENSITIVE);
	private final Pattern noPattern = Pattern.compile(tr("confirmation conversation no"), Pattern.CASE_INSENSITIVE);
	
	/**
	 * Creates a new Instance.
	 * 
	 * @param promptText The PromptText
	 */
	public ConfirmationConversation(String promptText) {
		this.promptText = promptText;
	}
	
	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		if(yesPattern.matcher(input).matches()) {
			context.setSessionData("result", true);
			return null;
		}
		else if(noPattern.matcher(input).matches()) {
			context.setSessionData("result", false);
			context.getForWhom().sendRawMessage(tr("confirmation conversation picked no option"));
			return null;
		}
		else {
			return this;
		}
	}

	@Override
	public String getPromptText(ConversationContext context) {
		return promptText;
	}

}
