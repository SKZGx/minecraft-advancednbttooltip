/*	MIT License
	
	Copyright (c) 2020-present b0iizz
	
	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
*/
package me.b0iizz.advancednbttooltip;

import com.google.common.collect.ImmutableSet;
import me.b0iizz.advancednbttooltip.api.CustomTooltip;
import me.b0iizz.advancednbttooltip.api.JsonTooltips;
import me.b0iizz.advancednbttooltip.api.impl.builtin.*;
import me.b0iizz.advancednbttooltip.config.ConfigManager;
import me.b0iizz.advancednbttooltip.gui.HudTooltipPicker;
import me.b0iizz.advancednbttooltip.gui.HudTooltipRenderer;
import me.b0iizz.advancednbttooltip.misc.JsonTooltipResourceManager;
import me.b0iizz.advancednbttooltip.misc.ModKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * The Fabric Entrypoint of this mod. <br>
 * <br>
 * <b>Implements:</b> <br>
 * {@link ClientModInitializer}
 *
 * @author B0IIZZ
 */
public final class AdvancedNBTTooltips implements ClientModInitializer {

	/**
	 * The mod's modid
	 */
	public static final String modid = "advancednbttooltip";

	/**
	 * The list of all loaded tooltips
	 */
	protected static final Map<Identifier, CustomTooltip> TOOLTIPS = new HashMap<>();

	/**
	 * Constructs a new {@link Identifier} consisting of this mod's modid and the
	 * given name.
	 *
	 * @param name a name
	 * @return the {@link Identifier} of this mod corresponding to the given name.
	 */
	public static Identifier id(String name) {
		return new Identifier(modid, name);
	}

	/**
	 * @return A Set containing all registered Tooltips
	 */
	public static Set<Map.Entry<Identifier, CustomTooltip>> getRegisteredTooltips() {
		return ImmutableSet.copyOf(TOOLTIPS.entrySet());
	}

	/**
	 * Called on initialization. Registers and loads this mod's config.
	 */
	@Override
	public void onInitializeClient() {
		ConfigManager.registerConfig();
		ConfigManager.loadConfig();

		ModKeybinds.initKeyBindings();
		ClientTickEvents.END_CLIENT_TICK.register(ModKeybinds::updateKeyBindings);

		JsonTooltips.getInstance().registerFactory(LiteralFactory.class);
		JsonTooltips.getInstance().registerFactory(FormattedFactory.class);
		JsonTooltips.getInstance().registerFactory(TranslatedFactory.class);
		JsonTooltips.getInstance().registerFactory(NbtValueFactory.class);
		JsonTooltips.getInstance().registerFactory(NbtRetargetFactory.class);
		JsonTooltips.getInstance().registerFactory(NbtSizeFactory.class);
		JsonTooltips.getInstance().registerFactory(NbtTextFactory.class);
		JsonTooltips.getInstance().registerFactory(ConditionalFactory.class);
		JsonTooltips.getInstance().registerFactory(MultipleFactory.class);
		JsonTooltips.getInstance().registerFactory(MixFactory.class);
		JsonTooltips.getInstance().registerFactory(EffectFactory.class);
		JsonTooltips.getInstance().registerFactory(LimitFactory.class);
		JsonTooltips.getInstance().registerFactory(LimitLinesFactory.class);
		JsonTooltips.getInstance().registerFactory(BuiltInHideflagsFactory.class);
		JsonTooltips.getInstance().registerFactory(ItemRendererFactory.class);

		JsonTooltips.getInstance().registerCondition(AndCondition.class);
		JsonTooltips.getInstance().registerCondition(OrCondition.class);
		JsonTooltips.getInstance().registerCondition(NotCondition.class);
		JsonTooltips.getInstance().registerCondition(IsItemCondition.class);
		JsonTooltips.getInstance().registerCondition(HasTagCondition.class);
		JsonTooltips.getInstance().registerCondition(TagMatchesCondition.class);
		JsonTooltips.getInstance().registerCondition(AdvancedContextCondition.class);
		JsonTooltips.getInstance().registerCondition(HudContextCondition.class);
		JsonTooltips.getInstance().registerCondition(SectionVisibleCondition.class);

		HudTooltipRenderer.setup();
		HudTooltipPicker.setup();

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
				.registerReloadListener(new JsonTooltipResourceManager(TOOLTIPS));
	}

	/**
	 * The tooltip handler for the ItemTooltipCallback
	 *
	 * @param stack The item stack
	 * @param ctx   The context of the tooltip
	 * @param lines The lines in the tooltip
	 */
	public static void getTooltip(ItemStack stack, TooltipContext ctx, List<TooltipComponent> lines) {
		if (ConfigManager.getTooltipToggle()) {
			ArrayList<TooltipComponent> text = new ArrayList<>();
			appendCustomTooltip(stack.copy(), text, ctx);

			if (!lines.isEmpty() && !text.isEmpty())
				text.add(0, TooltipComponent.of(Text.of("").asOrderedText()));

			if (ConfigManager.getTooltipPosition() == TooltipPosition.TOP && !text.isEmpty() && lines.size() > 1)
				text.add(TooltipComponent.of(Text.of(" ").asOrderedText()));

			lines.addAll(ConfigManager.getTooltipPosition().position(lines), text);
		}
	}

	/**
	 * Used by the ItemTooltipCallback function to interact with the tooltip
	 * pipeline.
	 *
	 * @param stack   The {@link ItemStack} of which a tooltip should be generated.
	 * @param tooltip The List of text to add Tooltips to.
	 * @param context The {@link TooltipContext} where the tooltip is being
	 *                generated.
	 */
	protected static void appendCustomTooltip(ItemStack stack, List<TooltipComponent> tooltip, TooltipContext context) {
		Item item = stack.getItem();
		NbtCompound tag = stack.hasNbt() ? stack.getNbt() : new NbtCompound();
		TOOLTIPS.entrySet().stream().sorted(Comparator.comparing(a -> a.getKey().toString()))
				.forEachOrdered(t -> tooltip.addAll(t.getValue().getTooltip(item, tag, context)));
	}

	/**
	 * An enum representing the position of custom tooltips in the tooltip list
	 *
	 * @author B0IIZZ
	 */
	public enum TooltipPosition {
		TOP(1), BOTTOM(-1);

		private final int offset;

		TooltipPosition(int offset) {
			this.offset = offset;
		}

		public int position(List<?> list) {
			return offset < 0 ? list.size() + offset + 1 : offset;
		}

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

}
