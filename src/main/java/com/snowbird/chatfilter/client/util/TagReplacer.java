package com.snowbird.chatfilter.client.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TagReplacer processes a Minecraft chat Component that may have its tag (e.g. [ROCK] or [ELECTRIC])
 * split across several child (leaf) components. It will flatten the component, search for any tag
 * (using a regex pattern built from the defined replacement keys) in the entire concatenated text, then
 * reconstruct a new component that replaces each found tag with a custom replacement component.
 * The replacement component displays (for example) a short form such as “[R]” or “[E]” and is configured
 * with a hover event that shows the original tag (with its full styling).
 *
 * Any text outside a tag is reassembled with its original style.
 */
public class TagReplacer {

	/**
	 * Container for tag replacement data.
	 */
	public static class RankReplacement {
		// The list of components that represent the replacement (for example, a short version like “[R]”)
		private final List<MutableComponent> rankTag;

		public RankReplacement(List<MutableComponent> rankTag) {
			this.rankTag = rankTag;
		}

		public List<MutableComponent> getRankTag() {
			return rankTag;
		}
	}

	// Mapping from tag key (including the brackets) to its replacement.
	// Existing tags are kept and new tags are added.
	private static final Map<String, RankReplacement> RANK_REPLACEMENTS = new HashMap<String, RankReplacement>() {{
		// Existing definitions:
		put("[ROCK]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("R")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("B6A136", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[TRAINER]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("T")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("1CFF1F", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[WATER]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("W")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("415E9D", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[ADMIN]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("A")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("E22C21", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[BETA]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("B")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("DC8AD5", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[ARCHITECT]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("A")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FB01A6", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[ELECTRIC]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("E")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFA300", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[MOD]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("M")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("0E66FB", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[FIRE]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("F")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("9C0000", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[HEAD-ADMIN]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("H")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("585757", 16)))),
			MutableComponent.create(new LiteralContents("A")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("585757", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[OWNER]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("O")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FB0800", 16)))),
			MutableComponent.create(new LiteralContents("W")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FB0800", 16)))),
			MutableComponent.create(new LiteralContents("N")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FB0800", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[HEAD-STAFF]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("H")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FB0EF9", 16)))),
			MutableComponent.create(new LiteralContents("S")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FB0EF9", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[HEAD-LEADER]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("H")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("927046", 16)))),
			MutableComponent.create(new LiteralContents("L")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("927046", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[DEVELOPER]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("D")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("8D138C", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[ICE]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("I")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("11D0DC", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[GRASS]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("G")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("7AC74C", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[TWITCHTV]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("T")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FF55FF", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		// New tags with one-letter replacements.
		put("[FAY]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("F")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("D685AD", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[GRD]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("G")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("E2BF65", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[FTG]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("F")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("C22E28", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[DRK]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("D")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("705746", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[GHT]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("G")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("735797", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[ELC]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("E")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("F7D02C", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[WTR]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("W")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("6390F0", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		put("[STL]", new RankReplacement(Arrays.asList(
			MutableComponent.create(new LiteralContents("[")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
			MutableComponent.create(new LiteralContents("S")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("B7B7CE", 16)))),
			MutableComponent.create(new LiteralContents("]")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
		)));
		// Definition for "[G] " with no replacement: the filter will leave it unchanged.
		put("[G] ", new RankReplacement(Collections.emptyList()));
	}};

	/**
	 * Builds a regex pattern that only matches tags defined in RANK_REPLACEMENTS.
	 * For example, if the keys are "[ROCK]", "[TRAINER]", "[ELECTRIC]", etc.,
	 * the pattern will be a case-insensitive alternation of the quoted keys.
	 */
	private static Pattern buildTagPattern() {
		if (RANK_REPLACEMENTS.isEmpty()) {
			return Pattern.compile("(\\w+)");
		}
		Set<String> keys = new HashSet<>();
		for (String key : RANK_REPLACEMENTS.keySet()) {
			keys.add(key.toUpperCase());
		}
		String joined = String.join("|", keys.stream().map(Pattern::quote).toArray(String[]::new));
		String regex = "(?i:" + joined + ")";
		return Pattern.compile(regex);
	}

	// Pattern to match tags defined in the mapping.
	private static final Pattern TAG_PATTERN = buildTagPattern();

	/**
	 * A helper class representing a leaf node in the flattened component tree.
	 */
	private static class Leaf {
		final String text;
		final Style style;

		Leaf(String text, Style style) {
			this.text = text;
			this.style = style;
		}
	}

	/**
	 * A helper class representing a segment of text in the full concatenated message.
	 * Each segment corresponds to one leaf and holds its start and end offsets.
	 */
	private static class Segment {
		final Leaf leaf;
		final int start; // inclusive offset in the full text
		final int end;   // exclusive offset

		Segment(Leaf leaf, int start, int end) {
			this.leaf = leaf;
			this.start = start;
			this.end = end;
		}
	}

	/**
	 * Recursively flattens a component into a list of Leaf objects containing literal text and style.
	 * Note: We access the raw text via literal.text() to avoid debug formatting.
	 *
	 * @param comp The component to flatten.
	 * @return A list of Leaf objects.
	 */
	private static List<Leaf> flatten(Component comp) {
		List<Leaf> leaves = new ArrayList<>();
		if (comp.getContents() instanceof LiteralContents) {
			LiteralContents literal = (LiteralContents) comp.getContents();
			String text = literal.text();
			if (!text.isEmpty()) {
				leaves.add(new Leaf(text, comp.getStyle()));
			}
		}
		for (Component sibling : comp.getSiblings()) {
			leaves.addAll(flatten(sibling));
		}
		return leaves;
	}

	/**
	 * Given a list of segments (each mapping a leaf to an offset in the full concatenated text),
	 * extracts the components corresponding to the text in the range [rangeStart, rangeEnd).
	 *
	 * @param rangeStart The start offset.
	 * @param rangeEnd   The end offset.
	 * @param segments   The list of segments.
	 * @return A list of MutableComponent representing the extracted text.
	 */
	private static List<MutableComponent> extractComponents(int rangeStart, int rangeEnd, List<Segment> segments) {
		List<MutableComponent> result = new ArrayList<>();
		for (Segment seg : segments) {
			if (seg.end <= rangeStart || seg.start >= rangeEnd) continue;
			int localStart = Math.max(rangeStart, seg.start) - seg.start;
			int localEnd = Math.min(rangeEnd, seg.end) - seg.start;
			String partText = seg.leaf.text.substring(localStart, localEnd);
			result.add(MutableComponent.create(new LiteralContents(partText)).withStyle(seg.leaf.style));
		}
		return result;
	}

	/**
	 * Processes the given component by flattening it and then searching for tag matches that may span
	 * multiple leaves. It replaces each found tag with a replacement component that has a hover event
	 * showing the original tag’s components (preserving their styles).
	 * The final message is reassembled as a flat component.
	 *
	 * @param comp The original component.
	 * @return A new Component with all defined tags replaced.
	 */
	public static Component processComponent(Component comp) {
		// Flatten the component tree.
		List<Leaf> leaves = flatten(comp);
		StringBuilder fullTextBuilder = new StringBuilder();
		List<Segment> segments = new ArrayList<>();
		int offset = 0;
		for (Leaf leaf : leaves) {
			int len = leaf.text.length();
			segments.add(new Segment(leaf, offset, offset + len));
			fullTextBuilder.append(leaf.text);
			offset += len;
		}
		String fullText = fullTextBuilder.toString();

		List<MutableComponent> outputComponents = new ArrayList<>();
		int currentPos = 0;
		Matcher matcher = TAG_PATTERN.matcher(fullText);
		while (matcher.find()) {
			int matchStart = matcher.start();
			int matchEnd = matcher.end();
			// Append text from currentPos to matchStart.
			if (matchStart > currentPos) {
				List<MutableComponent> beforeParts = extractComponents(currentPos, matchStart, segments);
				outputComponents.addAll(beforeParts);
			}
			// Extract the components corresponding to the matched tag.
			List<MutableComponent> tagComponents = extractComponents(matchStart, matchEnd, segments);
			StringBuilder tagKeyBuilder = new StringBuilder();
			for (MutableComponent mc : tagComponents) {
				tagKeyBuilder.append(mc.getString());
			}
			String fullTagText = tagKeyBuilder.toString(); // e.g. "[Rock]" or "[FAY]"
			String tagKey = fullTagText.toUpperCase();
			if (RANK_REPLACEMENTS.containsKey(tagKey)) {
				RankReplacement replacement = RANK_REPLACEMENTS.get(tagKey);
				// Build the replacement component.
				MutableComponent replacementComponent = MutableComponent.create(new LiteralContents(""));
				for (MutableComponent part : replacement.getRankTag()) {
					replacementComponent.append(part.copy());
				}
				// Build the tooltip from the original tag components.
				MutableComponent tooltip = MutableComponent.create(new LiteralContents(""));
				for (MutableComponent compPart : tagComponents) {
					tooltip.append(compPart);
				}
				replacementComponent.setStyle(
					replacementComponent.getStyle().withHoverEvent(
						new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)
					)
				);
				outputComponents.add(replacementComponent);
			} else {
				// If no replacement is defined, append the original tag components.
				outputComponents.addAll(tagComponents);
			}
			currentPos = matchEnd;
		}
		// Append any remaining text after the last match.
		if (currentPos < fullText.length()) {
			List<MutableComponent> remainingParts = extractComponents(currentPos, fullText.length(), segments);
			outputComponents.addAll(remainingParts);
		}
		// Combine all output components into one final mutable component.
		MutableComponent finalComponent = MutableComponent.create(new LiteralContents(""));
		for (MutableComponent part : outputComponents) {
			finalComponent.append(part);
		}
		return finalComponent;
	}
}
