package me.ialistannen.inventory_profiles.conversations;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * The login conversation. Sets the profile as Session data with the key "profile".
 */
class LoginConversation extends StringPrompt {

	private final String promptText;
	
	/**
	 * @param promptText The prompt text
	 */
	private LoginConversation(String promptText) {
		this.promptText = promptText;
	}
	
	/**
	 * Uses the key defined in the language file
	 */
	public LoginConversation() {
		this(tr("login usage"));
	}
	

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		if(!input.contains(" ")) {
			return this;
		}
		
		String[] splitted = input.split("\\s");
		
		// too many parts
		if(splitted.length != 2) {
			return this;
		}
		
		if(!InventoryProfiles.getProfileManager().hasProfile(splitted[0])) {
			context.getForWhom().sendRawMessage(tr("username unknown"));
			return this;
		}
		
		Profile profile = InventoryProfiles.getProfileManager().getProfile(splitted[0]).get();
		
		if(!profile.getPassword().equals(splitted[1])) {
			context.getForWhom().sendRawMessage(tr("password incorrect"));
			return this;
		}
		
		context.setSessionData("profile", profile);
		return null;
	}

	@Override
	public String getPromptText(ConversationContext context) {
		return promptText;
	}

}
