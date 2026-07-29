package me.THEREALWWEFAN231.funnelmc.mixins;

import net.minecraft.core.NonNullList;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.javaconnection.packet.ClickSlotC2SPacketTranslator;
import me.THEREALWWEFAN231.funnelmc.mixins.interfaces.IMixinSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

@Mixin(AbstractContainerMenu.class)
public class MixinScreenHandler {

	//I know I shouldn't use @Overwrite but for now/testing purposes I will use it!!!!! It's not really a big deal though also I used engima to get this code rather then the source fabric attaches(because of the wack while loops)
	@Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
	private void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
		if (!Client.instance.isConnectionOpen()) {//if the connection isn't open, do the normal click stuff
			return;
		}
		ci.cancel();

		ClickSlotC2SPacketTranslator translator = Client.instance.javaConnection.packetTranslatorManager.clickSlotTranslator;

		Inventory playerInventory = player.getInventory();
		if (containerInput == ContainerInput.QUICK_CRAFT) {
			int expectedStatus = this.quickcraftStatus;
			this.quickcraftStatus = AbstractContainerMenu.getQuickcraftHeader(buttonNum);
			if ((expectedStatus == 1 && this.quickcraftStatus == 2) || expectedStatus == this.quickcraftStatus) {
				if (this.getCarried().isEmpty()) {
					this.resetQuickCraft();
				} else if (this.quickcraftStatus == 0) {
					this.quickcraftType = AbstractContainerMenu.getQuickcraftType(buttonNum);
					if (AbstractContainerMenu.isValidQuickcraftType(this.quickcraftType, player)) {
						this.quickcraftStatus = 1;
						this.quickcraftSlots.clear();
					} else {
						this.resetQuickCraft();
					}
				} else if (this.quickcraftStatus == 1) {
					Slot slot9 = this.slots.get(slotIndex);
					ItemStack carried9 = this.getCarried();
					if (slot9 != null && AbstractContainerMenu.canItemQuickReplace(slot9, carried9, true) && slot9.mayPlace(carried9) && (this.quickcraftType == 2 || carried9.getCount() > this.quickcraftSlots.size()) && this.canDragTo(slot9)) {
						this.quickcraftSlots.add(slot9);
					}
				} else if (this.quickcraftStatus == 2) {
					if (!this.quickcraftSlots.isEmpty()) {
						ItemStack source = this.getCarried().copy();
						int remaining = this.getCarried().getCount();
						for (final Slot lv6 : this.quickcraftSlots) {
							ItemStack carried = this.getCarried();
							if (lv6 != null && AbstractContainerMenu.canItemQuickReplace(lv6, carried, true) && lv6.mayPlace(carried) && (this.quickcraftType == 2 || carried.getCount() >= this.quickcraftSlots.size()) && this.canDragTo(lv6)) {
								int carry = lv6.hasItem() ? lv6.getItem().getCount() : 0;
								int maxSize = Math.min(source.getMaxStackSize(), lv6.getMaxStackSize(source));
								int newCount = Math.min(AbstractContainerMenu.getQuickCraftPlaceCount(this.quickcraftSlots.size(), this.quickcraftType, source) + carry, maxSize);
								remaining -= newCount - carry;
								lv6.setByPlayer(source.copyWithCount(newCount));
							}
						}
						source.setCount(remaining);
						this.setCarried(source);
					}
					this.resetQuickCraft();
				} else {
					this.resetQuickCraft();
				}
			} else {
				this.resetQuickCraft();
			}
		} else if (this.quickcraftStatus != 0) {
			this.resetQuickCraft();
		} else if ((containerInput == ContainerInput.PICKUP || containerInput == ContainerInput.QUICK_MOVE) && (buttonNum == 0 || buttonNum == 1)) {
			if (slotIndex == -999) {
				if (!this.getCarried().isEmpty()) {
					if (buttonNum == 0) {
						player.drop(this.getCarried(), true);
						this.setCarried(ItemStack.EMPTY);
					}
					if (buttonNum == 1) {
						player.drop(this.getCarried().split(1), true);
					}
				}
			} else if (containerInput == ContainerInput.QUICK_MOVE) {
				if (slotIndex < 0) {
					return;
				}
				Slot slot8 = this.slots.get(slotIndex);
				if (slot8 == null || !slot8.mayPickup(player)) {
					return;
				}
				ItemStack before = slot8.getItem();
				for (ItemStack lv10 = this.quickMoveStack(player, slotIndex); !lv10.isEmpty() && ItemStack.isSameItem(before, lv10); lv10 = this.quickMoveStack(player, slotIndex)) {
					before = slot8.getItem();
				}

				//EDITED
				translator.onStackShiftClicked((AbstractContainerMenu) (Object) this, slotIndex);
			} else {//PICKUP
				if (slotIndex < 0) {
					return;
				}
				Slot clickedSlot = this.slots.get(slotIndex);
				if (clickedSlot != null) {
					ItemStack clickedSlotStack = clickedSlot.getItem();
					ItemStack cursorStack = this.getCarried();
					if (clickedSlotStack.isEmpty()) {
						if (!cursorStack.isEmpty() && clickedSlot.mayPlace(cursorStack)) {
							int itemCountToMoveFromCursorToClickedSlot = (buttonNum == 0) ? cursorStack.getCount() : 1;
							if (itemCountToMoveFromCursorToClickedSlot > clickedSlot.getMaxStackSize(cursorStack)) {
								itemCountToMoveFromCursorToClickedSlot = clickedSlot.getMaxStackSize(cursorStack);
							}
							clickedSlot.setByPlayer(cursorStack.split(itemCountToMoveFromCursorToClickedSlot));
							//EDITED
							translator.onCursorStackClickEmptySlot((AbstractContainerMenu) (Object) this, slotIndex, itemCountToMoveFromCursorToClickedSlot);
						}
					} else if (clickedSlot.mayPickup(player)) {
						if (cursorStack.isEmpty()) {
							int integer11 = (buttonNum == 0) ? clickedSlotStack.getCount() : ((clickedSlotStack.getCount() + 1) / 2);
							clickedSlot.tryRemove(integer11, Integer.MAX_VALUE, player).ifPresent(taken -> {
								this.setCarried(taken);
								clickedSlot.onTake(player, taken);
							});

							//EDITED
							translator.onEmptyCursorClickStack((AbstractContainerMenu) (Object) this, slotIndex);
						} else if (clickedSlot.mayPlace(cursorStack)) {
							if (ItemStack.isSameItemSameComponents(clickedSlotStack, cursorStack)) {
								int integer11 = (buttonNum == 0) ? cursorStack.getCount() : 1;
								if (integer11 > clickedSlot.getMaxStackSize(cursorStack) - clickedSlotStack.getCount()) {
									integer11 = clickedSlot.getMaxStackSize(cursorStack) - clickedSlotStack.getCount();
								}
								if (integer11 > cursorStack.getMaxStackSize() - clickedSlotStack.getCount()) {
									integer11 = cursorStack.getMaxStackSize() - clickedSlotStack.getCount();
								}
								cursorStack.shrink(integer11);
								clickedSlotStack.grow(integer11);

								//EDITED
								translator.onCursorStackAddToStack((AbstractContainerMenu) (Object) this, slotIndex);

							} else if (cursorStack.getCount() <= clickedSlot.getMaxStackSize(cursorStack)) {
								clickedSlot.setByPlayer(cursorStack);
								this.setCarried(clickedSlotStack);
							}
						} else if (cursorStack.getMaxStackSize() > 1 && ItemStack.isSameItemSameComponents(clickedSlotStack, cursorStack) && !clickedSlotStack.isEmpty()) {
							int integer11 = clickedSlotStack.getCount();
							if (integer11 + cursorStack.getCount() <= cursorStack.getMaxStackSize()) {
								clickedSlot.tryRemove(integer11, Integer.MAX_VALUE, player).ifPresent(taken -> {
									cursorStack.grow(taken.getCount());
									clickedSlot.onTake(player, taken);
								});
							}
						}
					}
					clickedSlot.setChanged();
				}
			}
		} else if (containerInput == ContainerInput.SWAP) {
			if ((buttonNum >= 0 && buttonNum < 9) || buttonNum == 40) {
				Slot slot8 = this.slots.get(slotIndex);
				ItemStack itemStack9 = playerInventory.getItem(buttonNum);
				ItemStack itemStack10 = slot8.getItem();
				if (!itemStack9.isEmpty() || !itemStack10.isEmpty()) {
					if (itemStack9.isEmpty()) {
						if (slot8.mayPickup(player)) {
							playerInventory.setItem(buttonNum, itemStack10);
							((IMixinSlot) slot8).invokeOnSwapCraft(itemStack10.getCount());
							slot8.setByPlayer(ItemStack.EMPTY);
							slot8.onTake(player, itemStack10);
						}
					} else if (itemStack10.isEmpty()) {
						if (slot8.mayPlace(itemStack9)) {
							int integer11 = slot8.getMaxStackSize(itemStack9);
							if (itemStack9.getCount() > integer11) {
								slot8.setByPlayer(itemStack9.split(integer11));
							} else {
								playerInventory.setItem(buttonNum, ItemStack.EMPTY);
								slot8.setByPlayer(itemStack9);
							}
						}
					} else if (slot8.mayPickup(player) && slot8.mayPlace(itemStack9)) {
						int integer11 = slot8.getMaxStackSize(itemStack9);
						if (itemStack9.getCount() > integer11) {
							slot8.setByPlayer(itemStack9.split(integer11));
							slot8.onTake(player, itemStack10);
						} else {
							playerInventory.setItem(buttonNum, itemStack10);
							slot8.setByPlayer(itemStack9);
							slot8.onTake(player, itemStack10);
						}
					}
				}
			}
		} else if (containerInput == ContainerInput.CLONE && player.getAbilities().instabuild && this.getCarried().isEmpty() && slotIndex >= 0) {
			Slot slot8 = this.slots.get(slotIndex);
			if (slot8 != null && slot8.hasItem()) {
				ItemStack itemStack9 = slot8.getItem().copy();
				itemStack9.setCount(itemStack9.getMaxStackSize());
				this.setCarried(itemStack9);
			}
		} else if (containerInput == ContainerInput.THROW && this.getCarried().isEmpty() && slotIndex >= 0) {
			Slot slot8 = this.slots.get(slotIndex);
			if (slot8 != null && slot8.hasItem() && slot8.mayPickup(player)) {
				int amount = (buttonNum == 0) ? 1 : slot8.getItem().getCount();
				slot8.tryRemove(amount, Integer.MAX_VALUE, player).ifPresent(taken -> {
					slot8.onTake(player, taken);
					player.drop(taken, true);

					//EDITED
					translator.onHoverOverStackDropItem((AbstractContainerMenu) (Object) this, slotIndex, buttonNum);
				});
			}
		} else if (containerInput == ContainerInput.PICKUP_ALL && slotIndex >= 0) {
			Slot slot8 = this.slots.get(slotIndex);
			ItemStack itemStack9 = this.getCarried();
			if (!itemStack9.isEmpty() && (slot8 == null || !slot8.hasItem() || !slot8.mayPickup(player))) {
				int integer10 = (buttonNum == 0) ? 0 : (this.slots.size() - 1);
				int integer11 = (buttonNum == 0) ? 1 : -1;
				for (int w = 0; w < 2; ++w) {
					for (int x = integer10; x >= 0 && x < this.slots.size() && itemStack9.getCount() < itemStack9.getMaxStackSize(); x += integer11) {
						Slot slot14 = this.slots.get(x);
						if (slot14.hasItem() && AbstractContainerMenu.canItemQuickReplace(slot14, itemStack9, true) && slot14.mayPickup(player) && this.canTakeItemForPickAll(itemStack9, slot14)) {
							ItemStack itemStack15 = slot14.getItem();
							if (w != 0 || itemStack15.getCount() != itemStack15.getMaxStackSize()) {
								int integer16 = Math.min(itemStack9.getMaxStackSize() - itemStack9.getCount(), itemStack15.getCount());
								slot14.tryRemove(integer16, Integer.MAX_VALUE, player).ifPresent(taken -> {
									itemStack9.grow(taken.getCount());
									slot14.onTake(player, taken);
								});
							}
						}
					}
				}
			}
			this.broadcastChanges();
		}
	}

	@Shadow
	private int quickcraftType;

	@Shadow
	private int quickcraftStatus;

	@Shadow
	@Final
	private Set<Slot> quickcraftSlots;

	@Shadow
	@Final
	public NonNullList<Slot> slots;

	@Shadow
	protected void resetQuickCraft() {

	}

	@Shadow
	public void broadcastChanges() {

	}

	@Shadow
	public ItemStack quickMoveStack(Player player, int index) {
		return null;
	}

	@Shadow
	public boolean canDragTo(Slot slot) {
		return true;
	}

	@Shadow
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		return true;
	}

	@Shadow
	public ItemStack getCarried() {
		return null;
	}

	@Shadow
	public void setCarried(ItemStack stack) {

	}

}
