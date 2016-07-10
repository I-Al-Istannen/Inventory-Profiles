package me.ialistannen.inventory_profiles.conversations;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.ialistannen.inventory_profiles.util.Util;

/**
 * Manages conversations
 */
public class ConversationManager {

	private Map<String, Conversation> activeConversations = new HashMap<>();
	private ConversationFactory factory;
	
	/**
	 * @param plugin The plugin this factory is for
	 */
	public ConversationManager(Plugin plugin) {
		factory = new ConversationFactory(plugin).withEscapeSequence("exit").withLocalEcho(false).withModality(false);
	}
	
	/**
	 * Cancels the current conversation
	 * 
	 * @param player The Player that is currently conversing
	 */
	public void cancelConversation(Player player) {
		if(activeConversations.containsKey(player.getName())) {
			activeConversations.get(player.getName()).abandon();
		}
	}
	
	/**
	 * Starts the conversation. Sends the "conversation cancelled" message upon an ungracefuly exit.
	 * 
	 * @param player The Player to converse with
	 * @param type The type of the conversation
	 * @param abandonListener The AbandonListener 
	 * @param promptText The PromptText. Only needed for the {@link ConversationType#CONFIRMATION}
	 * @return True if the conversation was started
	 */
	public boolean startConversation(Player player, ConversationType type, ConversationAbandonedListener abandonListener, String promptText) {
		if(player.isConversing()) {
			return false;
		}
		
		if(type == ConversationType.LOGIN) {
			Conversation conversation = factory.withTimeout(type.getTimeoutSeconds()).withFirstPrompt(new LoginConversation()).buildConversation(player);
			
			conversation.addConversationAbandonedListener((event) -> {
				abandonListener.conversationAbandoned(event);
				activeConversations.remove(((Player) event.getContext().getForWhom()).getName());
			});
			
			activeConversations.put(player.getName(), conversation);
			conversation.begin();
		}
		else if(type == ConversationType.CONFIRMATION) {
			Conversation conversation = factory.withTimeout(type.getTimeoutSeconds()).withFirstPrompt(new ConfirmationConversation(promptText)).buildConversation(player);
			
			conversation.addConversationAbandonedListener((event) -> {
				abandonListener.conversationAbandoned(event);
				activeConversations.remove(((Player) event.getContext().getForWhom()).getName());
				if(!event.gracefulExit()) {
					event.getContext().getForWhom().sendRawMessage(Util.tr("conversation cancelled"));
				}
			});
			
			activeConversations.put(player.getName(), conversation);
			conversation.begin();
		}
		
		return true;
	}
	
	/**
	 * The same as the more detailed method, just <b>passes "" as prompt text.</b>
	 * 
	 * @param player The Player to converse with
	 * @param type The type of the conversation
	 * @param abandonListener The AbandonListener 
	 * @return True if the conversation was started
	 * 
	 * @see #startConversation(Player, ConversationType, ConversationAbandonedListener, String)
	 */
	public boolean startConversation(Player player, ConversationType type, ConversationAbandonedListener abandonListener) {
		return startConversation(player, type, abandonListener, "");
	}
	
	/**
	 * @return The Conversation factory
	 */
	public ConversationFactory getFactory() {
		return factory;
	}
	
	/**
	 * Cancels all the conversations
	 */
	public void cancelAllConversations() {
		for (Conversation conversation : activeConversations.values()) {
			conversation.abandon();
		}
	}
	
	/**
	 * The different types of conversations
	 */
	public enum ConversationType {
		/**
		 * The login confirmation
		 */
		LOGIN(600),
		/**
		 * A conversation to confirm something
		 */
		CONFIRMATION(10);
		
		
		private int timeoutSeconds;
		private ConversationType(int timeoutSeconds) {
			this.timeoutSeconds = timeoutSeconds;
		}
		
		/**
		 * @return The Seconds until an timeout should occur
		 */
		public int getTimeoutSeconds() {
			return timeoutSeconds;
		}
	}
}
