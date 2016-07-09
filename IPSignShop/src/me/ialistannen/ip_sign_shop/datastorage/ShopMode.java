package me.ialistannen.ip_sign_shop.datastorage;

import static me.ialistannen.ip_sign_shop.util.Language.tr;

/**
 * The mode of the shop
 */
public enum ShopMode {
	/**
	 * The shop sells items inside his chest 
	 */
	SELL,
	/**
	 * The shop buys items with the money of the owner
	 */
	BUY,
	/**
	 * The plugin sells items, but has an unlimited supply. Any profit just disappears.
	 */
	SELL_UNLIMITED,
	/**
	 * The shop buys an unlimited amount of items. The items will just be destroyed.
	 */
	BUY_UNLIMITED;
	
	/**
	 * @return The Opposite shop mode.
	 */
	public ShopMode getOpposite() {
		switch(this) {
		case BUY: {
			return SELL;
		}
		case BUY_UNLIMITED: {
			return SELL_UNLIMITED;
		}
		case SELL: {
			return BUY;
		}
		case SELL_UNLIMITED: {
			return ShopMode.BUY_UNLIMITED;
		}
		}
		
		return SELL;
	}
	
	/**
	 * @return The {@link ShopMode} as a verb
	 */
	public String getShopModeName() {
		switch(this) {
		case BUY: {
			return tr("shop mode buy");
		}
		case BUY_UNLIMITED: {
			return tr("shop mode buy unlimited");
		}
		case SELL: {
			return tr("shop mode sell");
		}
		case SELL_UNLIMITED: {
			return tr("shop mode sell unlimited");
		}
		}
		return "UNDEFINED";
	}
	
	/**
	 * @return The Unlimited version of the current ShopMode
	 */
	public ShopMode getUnlimtedVersion() {
		switch(this) {
		case BUY:
		case BUY_UNLIMITED: {
			return ShopMode.BUY_UNLIMITED;
		}
		case SELL:
		case SELL_UNLIMITED: {
			return SELL_UNLIMITED;
		}
		}
		
		return ShopMode.SELL_UNLIMITED;
	}

	/**
	 * @return The limited version of the current ShopMode
	 */
	public ShopMode getLimitedVersion() {
		switch(this) {
		case BUY:
		case BUY_UNLIMITED: {
			return ShopMode.BUY;
		}
		case SELL:
		case SELL_UNLIMITED: {
			return SELL;
		}
		}
		
		return ShopMode.SELL;
	}
}
