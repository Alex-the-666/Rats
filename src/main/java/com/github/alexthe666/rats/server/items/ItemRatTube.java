package com.github.alexthe666.rats.server.items;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.blocks.ICustomRendered;
import com.github.alexthe666.rats.server.blocks.RatsBlockRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRatTube extends Item implements ICustomRendered {
    public DyeColor color;

    public ItemRatTube(DyeColor color) {
        super(new Item.Properties().group(RatsMod.TAB));
        this.setRegistryName(RatsMod.MODID, "rat_tube_" + color.getTranslationKey());
        this.color = color;
    }

    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent("block.rats.rat_tube.desc0").func_240699_a_(TextFormatting.GRAY));
        tooltip.add(new TranslationTextComponent("block.rats.rat_tube.desc1").func_240699_a_(TextFormatting.GRAY));
        tooltip.add(new TranslationTextComponent("block.rats.rat_tube.desc2").func_240699_a_(TextFormatting.GRAY));
    }

    public ActionResultType onItemUse(ItemUseContext context) {
        ActionResultType actionresulttype = this.tryPlace(new BlockItemUseContext(context));
        return actionresulttype != ActionResultType.SUCCESS && this.isFood() ? this.onItemRightClick(context.getWorld(), context.getPlayer(), context.getHand()).getType() : actionresulttype;
    }

    public ActionResultType tryPlace(BlockItemUseContext context) {
        if (!context.canPlace()) {
            return ActionResultType.FAIL;
        } else {
            BlockItemUseContext blockitemusecontext = this.getBlockItemUseContext(context);
            if (blockitemusecontext == null) {
                return ActionResultType.FAIL;
            } else {
                BlockState blockstate = this.getStateForPlacement(blockitemusecontext);
                if (blockstate == null) {
                    return ActionResultType.FAIL;
                } else if (!this.placeBlock(blockitemusecontext, blockstate)) {
                    return ActionResultType.FAIL;
                } else {
                    BlockPos blockpos = blockitemusecontext.getPos();
                    World world = blockitemusecontext.getWorld();
                    PlayerEntity playerentity = blockitemusecontext.getPlayer();
                    ItemStack itemstack = blockitemusecontext.getItem();
                    BlockState blockstate1 = world.getBlockState(blockpos);
                    Block block = blockstate1.getBlock();
                    if (block == blockstate.getBlock()) {
                        blockstate1 = this.func_219985_a(blockpos, world, itemstack, blockstate1);
                        this.onBlockPlaced(blockpos, world, playerentity, itemstack, blockstate1);
                        block.onBlockPlacedBy(world, blockpos, blockstate1, playerentity, itemstack);
                        if (playerentity instanceof ServerPlayerEntity) {
                            CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity)playerentity, blockpos, itemstack);
                        }
                    }

                    SoundType soundtype = blockstate1.getSoundType(world, blockpos, context.getPlayer());
                    world.playSound(playerentity, blockpos, this.getPlaceSound(blockstate1), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    itemstack.shrink(1);
                    return ActionResultType.SUCCESS;
                }
            }
        }
    }

    protected SoundEvent getPlaceSound(BlockState state) {
        return state.getSoundType().getPlaceSound();
    }

    @Nullable
    public BlockItemUseContext getBlockItemUseContext(BlockItemUseContext context) {
        return context;
    }

    protected boolean onBlockPlaced(BlockPos pos, World worldIn, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        return setTileEntityNBT(worldIn, player, pos, stack);
    }

    @Nullable
    protected BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockstate = RatsBlockRegistry.RAT_TUBE_COLOR.getStateForPlacement(context);
        return blockstate != null && this.canPlace(context, blockstate) ? blockstate : null;
    }

    private BlockState func_219985_a(BlockPos p_219985_1_, World p_219985_2_, ItemStack p_219985_3_, BlockState p_219985_4_) {
        BlockState blockstate = p_219985_4_;
        CompoundNBT compoundnbt = p_219985_3_.getTag();
        if (compoundnbt != null) {
            CompoundNBT compoundnbt1 = compoundnbt.getCompound("BlockStateTag");
            StateContainer<Block, BlockState> statecontainer = p_219985_4_.getBlock().getStateContainer();

            for(String s : compoundnbt1.keySet()) {
                Property<?> iproperty = statecontainer.getProperty(s);
                if (iproperty != null) {
                    String s1 = compoundnbt1.get(s).getString();
                    blockstate = func_219988_a(blockstate, iproperty, s1);
                }
            }
        }

        if (blockstate != p_219985_4_) {
            p_219985_2_.setBlockState(p_219985_1_, blockstate, 2);
        }

        return blockstate;
    }

    private static <T extends Comparable<T>> BlockState func_219988_a(BlockState p_219988_0_, Property<T> p_219988_1_, String p_219988_2_) {
        return p_219988_1_.parseValue(p_219988_2_).map((p_219986_2_) -> {
            return p_219988_0_.with(p_219988_1_, p_219986_2_);
        }).orElse(p_219988_0_);
    }

    protected boolean canPlace(BlockItemUseContext p_195944_1_, BlockState p_195944_2_) {
        PlayerEntity playerentity = p_195944_1_.getPlayer();
        ISelectionContext iselectioncontext = playerentity == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(playerentity);
        return (!this.func_219987_d() || p_195944_2_.isValidPosition(p_195944_1_.getWorld(), p_195944_1_.getPos()));
    }

    protected boolean func_219987_d() {
        return true;
    }

    protected boolean placeBlock(BlockItemUseContext context, BlockState state) {
        return context.getWorld().setBlockState(context.getPos(), state, 11);
    }

    public static boolean setTileEntityNBT(World worldIn, @Nullable PlayerEntity player, BlockPos pos, ItemStack stackIn) {
        MinecraftServer minecraftserver = worldIn.getServer();
        if (minecraftserver == null) {
            return false;
        } else {
            CompoundNBT compoundnbt = stackIn.getChildTag("BlockEntityTag");
            if (compoundnbt != null) {
                TileEntity tileentity = worldIn.getTileEntity(pos);
                if (tileentity != null) {
                    if (!worldIn.isRemote && tileentity.onlyOpsCanSetNbt() && (player == null || !player.canUseCommandBlock())) {
                        return false;
                    }

                    CompoundNBT compoundnbt1 = tileentity.write(new CompoundNBT());
                    CompoundNBT compoundnbt2 = compoundnbt1.copy();
                    compoundnbt1.merge(compoundnbt);
                    compoundnbt1.putInt("x", pos.getX());
                    compoundnbt1.putInt("y", pos.getY());
                    compoundnbt1.putInt("z", pos.getZ());
                    if (!compoundnbt1.equals(compoundnbt2)) {
                        tileentity.func_230337_a_(worldIn.getBlockState(pos), compoundnbt1);
                        tileentity.markDirty();
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
