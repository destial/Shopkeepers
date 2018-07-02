package com.nisovin.shopkeepers.shopkeeper.offers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Stores information about a type of book being sold for a certain price.
 */
public class BookOffer {

	private final String bookTitle; // not null, can be empty
	private final int price; // > 0

	public BookOffer(String bookTitle, int price) {
		// TODO what about empty book titles?
		Validate.notNull(bookTitle, "Book title cannot be null!");
		Validate.isTrue(price > 0, "Price has to be positive!");
		this.bookTitle = bookTitle;
		this.price = price;
	}

	public String getBookTitle() {
		return bookTitle;
	}

	public int getPrice() {
		return price;
	}

	// //////////
	// STATIC UTILITIES
	// //////////

	public static void saveToConfig(ConfigurationSection config, String node, Collection<BookOffer> offers) {
		ConfigurationSection offersSection = config.createSection(node);
		for (BookOffer offer : offers) {
			offersSection.set(offer.getBookTitle(), offer.getPrice());
		}
	}

	public static List<BookOffer> loadFromConfig(ConfigurationSection config, String node) {
		List<BookOffer> offers = new ArrayList<>();
		ConfigurationSection offersSection = config.getConfigurationSection(node);
		if (offersSection != null) {
			for (String bookTitle : offersSection.getKeys(false)) {
				int price = offersSection.getInt(bookTitle);
				if (bookTitle == null || price <= 0) continue; // invalid offer
				offers.add(new BookOffer(bookTitle, price));
			}
		}
		return offers;
	}
}