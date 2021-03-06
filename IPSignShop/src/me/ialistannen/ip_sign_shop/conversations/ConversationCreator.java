package me.ialistannen.ip_sign_shop.conversations;

import me.ialistannen.ip_sign_shop.IPSignShop;
import org.bukkit.conversations.ConversationFactory;

/**
 * The Conversation creator
 */
public class ConversationCreator {

	private final ConversationFactory factory;
	
	/**
	 * @param plugin The Plugin
	 */
	public ConversationCreator(IPSignShop plugin) {
		factory = new ConversationFactory(plugin);
	}
	
	/**
	 * @return The Default {@link ConversationFactory}
	 */
	public ConversationFactory getConversationFactory() {
		return getConversationFactory("exit", false, false, 10);
	}
	
	/**
	 * @param escapeSeq The Escape sequence
	 * @param localEcho With local echo
	 * @param modality The Modality
	 * @param timeoutSeconds The timeout in seconds
	 * @return The resulting conversation factory
	 */
	private ConversationFactory getConversationFactory(String escapeSeq, boolean localEcho, boolean modality, int
			timeoutSeconds) {
		return factory
				.withEscapeSequence(escapeSeq)
				.withLocalEcho(localEcho)
				.withModality(modality)
				.withTimeout(timeoutSeconds);
	}
}
