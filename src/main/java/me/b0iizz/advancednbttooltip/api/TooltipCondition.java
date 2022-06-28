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
package me.b0iizz.advancednbttooltip.api;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;

import java.util.function.BooleanSupplier;

/**
 * An interface used to restrict the visibility of a tooltip. A lambda function
 * is recommended.
 *
 * @author B0IIZZ
 */
@FunctionalInterface
public interface TooltipCondition {

	/**
	 * Condition which is always true
	 */
	TooltipCondition TRUE = TooltipCondition.of(() -> true);

	/**
	 * Condition which is always false
	 */
	TooltipCondition FALSE = TooltipCondition.of(() -> false);

	/**
	 * Decides if the condition is enabled.
	 *
	 * @param item    The {@link Item} the tooltip will be added to.
	 * @param tag     The Item's {@link NbtCompound NBT-tag}.
	 * @param context The current {@link TooltipContext}.
	 * @return Whether the tooltip should be displayed.
	 */
	boolean isEnabled(Item item, NbtCompound tag, TooltipContext context);

	/**
	 * Creates a condition based on the {@link BooleanSupplier}. This is useful for
	 * conditions not relying on the supplied parameters.
	 *
	 * @param condition The condition used for {@link TooltipCondition#isEnabled}
	 * @return a {@link TooltipCondition}
	 */
	static TooltipCondition of(BooleanSupplier condition) {
		return (i, t, c) -> condition.getAsBoolean();
	}

}
