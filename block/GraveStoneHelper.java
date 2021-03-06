package gravestone.block;

import gravestone.GraveStoneLogger;
import gravestone.block.enums.EnumGraves;
import gravestone.config.GraveStoneConfig;
import gravestone.core.GSBlock;
import gravestone.core.GSMobSpawn;
import gravestone.core.compatibility.*;
import gravestone.item.CorpseHelper;
import gravestone.item.enums.EnumCorpse;
import gravestone.tileentity.DeathMessageInfo;
import gravestone.tileentity.TileEntityGSGraveStone;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.*;

/**
 * GraveStone mod
 *
 * @author NightKosh
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */
public class GraveStoneHelper {
    private static final List<Integer> gravestoneGround = Arrays.asList(
            Block.getIdFromBlock(Blocks.grass), Block.getIdFromBlock(Blocks.dirt),
            Block.getIdFromBlock(Blocks.sand), Block.getIdFromBlock(Blocks.gravel),
            Block.getIdFromBlock(Blocks.soul_sand), Block.getIdFromBlock(Blocks.mycelium),
            Block.getIdFromBlock(Blocks.snow));
    public static ArrayList<Item> swordsList = new ArrayList<Item>(
            Arrays.asList(
                    Items.wooden_sword,
                    Items.stone_sword,
                    Items.iron_sword,
                    Items.golden_sword,
                    Items.diamond_sword
            )
    );

    public static final Item[] GENERATED_SWORD_GRAVES = {
            Items.wooden_sword,
            Items.stone_sword
    };

    private GraveStoneHelper() {
    }

    /**
     * Check is there sword in your inventory
     */
    // TODO
    public static ItemStack oldCheckSword(List<ItemStack> items) {
        if (items != null) {
            for (ItemStack stack : items) {
                if (stack != null && swordsList.contains(stack.getItem())) {
                    ItemStack sword = stack.copy();
                    items.remove(stack);
                    return sword;
                }
            }
        }

        return null;
    }

    /**
     * Check is grave - sword grave
     *
     * @param graveType Grave type
     */
    // TODO
    public static boolean isSwordGrave(byte graveType) {
        return graveType == EnumGraves.SWORD.ordinal();
    }

    /**
     * Return random grave type
     *
     * @param random
     * @param graveType
     */
    // TODO
    public static byte getGraveType(Random random, BlockGSGraveStone.EnumGraveType graveType) {
        switch (graveType) {
            case PLAYER_GRAVES:
                if (random.nextFloat() > 0.1) {
                    return BlockGSGraveStone.GENERATED_GRAVES[random.nextInt(BlockGSGraveStone.GENERATED_GRAVES.length)];
                } else {
                    return (byte) EnumGraves.SWORD.ordinal();
                    //return BlockGSGraveStone.GENERATED_SWORD_GRAVES[random.nextInt(BlockGSGraveStone.GENERATED_SWORD_GRAVES.length)];
                }
            case PETS_GRAVES:
                return BlockGSGraveStone.PETS_GRAVES[random.nextInt(BlockGSGraveStone.PETS_GRAVES.length)];
            case DOGS_GRAVES:
                return BlockGSGraveStone.DOG_GRAVES[random.nextInt(BlockGSGraveStone.DOG_GRAVES.length)];
            case CATS_GRAVES:
                return BlockGSGraveStone.CAT_GRAVES[random.nextInt(BlockGSGraveStone.CAT_GRAVES.length)];
            case ALL_GRAVES:
            default:
                if (random.nextFloat() > 0.2) {
                    if (random.nextFloat() > 0.1) {
                        return BlockGSGraveStone.GENERATED_GRAVES[random.nextInt(BlockGSGraveStone.GENERATED_GRAVES.length)];
                    } else {
                        return (byte) EnumGraves.SWORD.ordinal();
                        //return BlockGSGraveStone.GENERATED_SWORD_GRAVES[random.nextInt(BlockGSGraveStone.GENERATED_SWORD_GRAVES.length)];
                    }
                } else {
                    return BlockGSGraveStone.PETS_GRAVES[random.nextInt(BlockGSGraveStone.PETS_GRAVES.length)];
                }
        }
    }

    /**
     * Return grave metadata by direction
     */
    public static int getMetaDirection(int direction) {
        switch (direction) {
            case 0: // S
                return 1;
            case 1: // W
                return 2;
            case 2: // N
                return 0;
            case 3: // E
                return 3;
            default:
                return 0;
        }
    }

    /**
     * Check is grave - pet grave
     *
     * @param graveType Grave type
     */
    public static boolean isPetGrave(byte graveType) {
        return Arrays.binarySearch(BlockGSGraveStone.PETS_GRAVES, graveType) >= 0;
    }

    // TODO
    public static byte oldGraveTypeToSwordType(byte graveType) {
        return (byte) (graveType - 4);
    }

    /**
     * Check ground type and replace it on dirt if it grass or mycelium
     */
    public static void replaceGround(World world, int x, int y, int z) {
        Block botBlock = world.getBlock(x, y, z);

        if (botBlock.equals(Blocks.grass) || botBlock.equals(Blocks.mycelium)) {
            world.setBlock(x, y, z, Blocks.dirt);
        }
    }

    /**
     * Spawn mob
     */
    public static void spawnMob(World world, int x, int y, int z) {
        if (world.rand.nextInt(10) == 0) {
            TileEntityGSGraveStone tileEntity = (TileEntityGSGraveStone) world.getTileEntity(x, y, z);

            if (tileEntity != null) {
                Entity mob = GSMobSpawn.getMobEntity(world, tileEntity.getGraveType(), x, y, z);

                if (mob != null) {
                    GSMobSpawn.spawnMob(world, mob, x, y, z, false);
                }
            }
        }
    }

    /**
     * Check can be grave placed on this type of surface
     */
    public static boolean canPlaceBlockAt(Block block) {
        return GraveStoneConfig.canPlaceGravesEveryWhere || gravestoneGround.contains(Block.getIdFromBlock(block));
    }

    public static int getMetadataBasedOnRotation(int rotation) {
        if (rotation >= 315 || rotation < 45) {
            return 1;
        } else if (rotation >= 45 && rotation < 135) {
            return 2;
        } else if (rotation >= 135 && rotation < 225) {
            return 0;
        } else {
            return 3;
        }
    }

    public static void createPlayerGrave(EntityPlayer player, LivingDeathEvent event) {
        if (player.worldObj != null && !player.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory") && GraveStoneConfig.graveItemsCount > 0) {
            List<ItemStack> items = new LinkedList<ItemStack>();

            items.addAll(Arrays.asList(player.inventory.mainInventory));
            items.addAll(Arrays.asList(player.inventory.armorInventory));

            GSCompatibilityBattlegear.addItems(items, player);
            GSCompatibilityTheCampingMod.addItems(items, player);
            GSCompatibilityBaubles.addItems(items, player);
            GSCompatibilityMariculture.addItems(items, player);
            GSCompatibilityTinkerConstruct.addItems(items, player);
            GSCompatibilityRpgInventory.addItems(items, player);
            //GSCompatibilityBackpacksMod.addItems(items, player);
            player.inventory.clearInventory(null, -1);

            GSCompatibilityisArsMagica.getSoulboundItemsBack(items, player);

            createGrave(player, event, items, player.getAge(), BlockGSGraveStone.EnumGraveType.PLAYER_GRAVES, false);
        } else {
            createGrave(player, event, null, player.getAge(), BlockGSGraveStone.EnumGraveType.PLAYER_GRAVES, false);
        }
    }

    public static void createGrave(Entity entity, LivingDeathEvent event, List<ItemStack> items, int age, BlockGSGraveStone.EnumGraveType entityType, boolean isVillager) {
        boolean isMagic = isMagicDamage(event.source);
        GSBlock.graveStone.createOnDeath(entity.worldObj, (int) entity.posX, (int) entity.posY, (int) entity.posZ - 1,
                getDeathMessage((EntityLivingBase) entity, event.source.damageType, isVillager),
                MathHelper.floor_float(entity.rotationYaw), items, age, entityType, isMagic);
    }

    private static boolean isMagicDamage(DamageSource damage) {
        return damage.equals(DamageSource.magic) || damage.damageType.toLowerCase().contains("magic");
    }

    public static void createPetGrave(Entity entity, LivingDeathEvent event) {
        EntityTameable pet = (EntityTameable) entity;

        if (pet.isTamed()) {
            if (pet instanceof EntityWolf) {
                createGrave(entity, event, CorpseHelper.getCorpse(entity, EnumCorpse.DOG), pet.getAge(), BlockGSGraveStone.EnumGraveType.DOGS_GRAVES, false);
            } else if (pet instanceof EntityOcelot) {
                createGrave(entity, event, CorpseHelper.getCorpse(entity, EnumCorpse.CAT), pet.getAge(), BlockGSGraveStone.EnumGraveType.CATS_GRAVES, false);
            }
        }
    }

    public static void createHorseGrave(EntityHorse horse, LivingDeathEvent event) {
        if (horse.isTame()) {
            List<ItemStack> items = new ArrayList<ItemStack>();
            items.addAll(CorpseHelper.getCorpse(horse, EnumCorpse.HORSE));

            createGrave(horse, event, items, horse.getAge(), BlockGSGraveStone.EnumGraveType.HORSE_GRAVES, false);
        }
    }

    private static DeathMessageInfo getDeathMessage(EntityLivingBase entity, String damageType, boolean isVillager) {
        EntityLivingBase killer = entity.func_94060_bK();
        String shortString = "death.attack." + damageType;
        String fullString = shortString + ".player";

        if (killer != null) {
            String killerName;
            if (killer instanceof EntityPlayer) {
                killerName = ((EntityPlayer) killer).getDisplayName();
                if (isVillager) {
                    GraveStoneLogger.logInfo("Villager was killed by " + killerName);
                }
            } else {
                killerName = EntityList.getEntityString(killer);
                if (killerName == null) {
                    killerName = "entity.generic.name";
                } else {
                    killerName = "entity." + killerName + ".name";
                }
            }
            if (StatCollector.canTranslate(fullString)) {
                return new DeathMessageInfo(entity.getCommandSenderName(), fullString, killerName);
            } else {
                return new DeathMessageInfo(entity.getCommandSenderName(), shortString, killerName);
            }
        } else {
            return new DeathMessageInfo(entity.getCommandSenderName(), shortString, null);
        }
    }

    // TODO
    public static void fillSwordsList() {
//        Iterator iter = Item.itemRegistry.iterator();
//        while (iter.hasNext()) {
//            if (((Item) iter.next()) instanceof ItemSword) {
//                swordsList.add(((Item) iter.next()));
//            }
//        }
    }

    public static void addSwordInfo(NBTTagCompound nbt, ItemStack sword) {
        NBTTagCompound swordNBT = new NBTTagCompound();
        sword.writeToNBT(swordNBT);
        nbt.setTag("Sword", swordNBT);
    }

    public static ItemStack getSwordAsGrave(Item grave, ItemStack sword) {
        ItemStack graveStoneStack = new ItemStack(grave, 1, 0);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("GraveType", (byte) EnumGraves.SWORD.ordinal());
        GraveStoneHelper.addSwordInfo(nbt, sword);

        graveStoneStack.setTagCompound(nbt);
        return graveStoneStack;
    }

    public static Item getRandomSwordForGeneration(byte graveType, Random random) {
        if (graveType == EnumGraves.SWORD.ordinal()) {
            return GENERATED_SWORD_GRAVES[random.nextInt(GENERATED_SWORD_GRAVES.length)];
        } else {
            return null;
        }
    }

    public static void addSwordToSwordsList(Item sword) {
        swordsList.add(sword);
    }
}
