package romelo333.notenoughwands.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import romelo333.notenoughwands.Items.ProtectionWand;
import romelo333.notenoughwands.ProtectedBlocks;

import java.util.HashSet;
import java.util.Set;

public class PacketGetProtectedBlocks implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketGetProtectedBlocks() {
    }

    public static class Handler implements IMessageHandler<PacketGetProtectedBlocks, IMessage> {
        @Override
        public IMessage onMessage(PacketGetProtectedBlocks message, MessageContext ctx) {
            MinecraftServer.getServer().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetProtectedBlocks message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            ItemStack heldItem = player.getHeldItem();
            if (heldItem == null || !(heldItem.getItem() instanceof ProtectionWand)) {
                // Cannot happen normally
                return;
            }
            ProtectionWand protectionWand = (ProtectionWand) heldItem.getItem();
            int id = protectionWand.getId(heldItem);

            ProtectedBlocks protectedBlocks = ProtectedBlocks.getProtectedBlocks(world);
            Set<BlockPos> blocks = new HashSet<>();
            protectedBlocks.fetchProtectedBlocks(blocks, world, (int)player.posX, (int)player.posY, (int)player.posZ, protectionWand.blockShowRadius, id);
            Set<BlockPos> childBlocks = new HashSet<>();
            if (id == -1) {
                // Master wand:
                protectedBlocks.fetchProtectedBlocks(childBlocks, world, (int)player.posX, (int)player.posY, (int)player.posZ, protectionWand.blockShowRadius, -2);
            }
            PacketReturnProtectedBlocks msg = new PacketReturnProtectedBlocks(blocks, childBlocks);
            PacketHandler.INSTANCE.sendTo(msg, player);
        }
    }
}
