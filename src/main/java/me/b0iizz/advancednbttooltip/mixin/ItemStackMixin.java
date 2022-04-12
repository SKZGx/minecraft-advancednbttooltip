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
package me.b0iizz.advancednbttooltip.mixin;

import java.util.List;
import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.b0iizz.advancednbttooltip.config.ConfigManager;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.text.Style;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	private static final int TAG_INT = 3;
	private static final int GRAY_COLOR = 0xA0A0A0; // I don't think this is precisely the right shade?
	private static final int[] AXOLOTL_COLORS = new int[] { 0xFFC0CB, 0x835C3B, 0xFFFF00, 0xCCFFFF, 0x728FCE };

	@Shadow
	public abstract Item getItem();

	@Shadow
	@Nullable
	public abstract NbtCompound getNbt();

	@ModifyVariable(at = @At(value = "INVOKE_ASSIGN", target = "net.minecraft.item.ItemStack.getHideFlags()I"), method = "getTooltip")
	private int advancednbttooltip$rewriteHideFlags(int i) {
		if (ConfigManager.overrideHideFlags()) {
			return i & ConfigManager.getHideflagOverrideBitmask();
		}
		return i;
	}

	// TODO: If a reasonable JSON-based solution is implemented, this stuff (and relevant declarations and imports) should be removed.
	@Inject(method = "getTooltip", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void onGetTooltip(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> ci, List<Text> list) {

		if (ConfigManager.isShowAxolotlVariant() && this.getItem() == Items.AXOLOTL_BUCKET) {

			NbtCompound tag = this.getNbt();

			if (tag != null && tag.contains(AxolotlEntity.VARIANT_KEY, TAG_INT)) {

				int id = tag.getInt(AxolotlEntity.VARIANT_KEY);

				AxolotlEntity.Variant variant = AxolotlEntity.Variant.VARIANTS[id];
				TranslatableText label = new TranslatableText("text.advancednbttooltip.tooltip.axolotl.label");
				TranslatableText value = new TranslatableText("text.advancednbttooltip.tooltip.axolotl.value", variant.getName());

				label.setStyle(Style.EMPTY.withColor(GRAY_COLOR));
				
				// Make sure we don't go out of bounds if mods add more axolotl types.
				if (id < AXOLOTL_COLORS.length) {
					value.setStyle(Style.EMPTY.withColor(AXOLOTL_COLORS[id]));
				}

				list.add(Math.min(1, list.size()), label.append(value));
				list.add(1, new TranslatableText(""));
			}
		}
	}
}
