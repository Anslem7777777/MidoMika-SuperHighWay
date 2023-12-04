package me.mika.midomikasuperhighway.Listeners;

import me.mika.midomikasuperhighway.MidoMika_SuperHighWay;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

public class WalkOnHighWay implements Listener {

    private Map<UUID, Double> hashSpeed = new HashMap<>();
    private Map<UUID, Double> hashGear = new HashMap<>();
    private static int count = 0;
    private final List<EntityType> pullAbleEntityTypes = new ArrayList<>(Arrays.asList(EntityType.PIG, EntityType.COW, EntityType.SHEEP, EntityType.CAT, EntityType.CHICKEN));
    private List<Entity> passengers;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();

        if (hashSpeed.get(p.getUniqueId()) != null) {
            if (e.getAction().name().contains("LEFT_CLICK") && e.getMaterial() == Material.DIAMOND) {
                if (hashSpeed.get(p.getUniqueId()) != 1.7) {
                    Double speed = hashSpeed.get(p.getUniqueId());
                    Double gear = hashGear.get(p.getUniqueId());
                    speed += 0.5;
                    gear += 1;
                    hashSpeed.put(p.getUniqueId(), speed);
                    hashGear.put(p.getUniqueId(), gear);
                    p.sendMessage(ChatColor.GREEN + "Gear: " + ChatColor.YELLOW + hashGear.get(p.getUniqueId()).intValue());

                }
            } else if (e.getAction().name().contains("RIGHT_CLICK") && e.getMaterial() == Material.DIAMOND) {
                if (hashSpeed.get(p.getUniqueId()) >= 0.2) {
                    Double speed = hashSpeed.get(p.getUniqueId());
                    double gear = hashGear.get(p.getUniqueId());
                    speed -= 0.5;
                    gear -= 1;
                    hashSpeed.put(p.getUniqueId(), speed);
                    hashGear.put(p.getUniqueId(), gear);
                    p.sendMessage(ChatColor.GREEN + "Gear: " + ChatColor.YELLOW + hashGear.get(p.getUniqueId()).intValue());
                }
            }else if (e.getAction().name().contains("LEFT_CLICK") && e.getMaterial() != Material.DIAMOND){
                p.sendMessage(ChatColor.GOLD + "Use Diamond: ");
                p.sendMessage(ChatColor.GREEN + "Left Click: " + ChatColor.YELLOW + "Accelerate");
                p.sendMessage(ChatColor.RED + "Right Click: " + ChatColor.YELLOW + "Decelerate");

            }
        }
    }

    @EventHandler
    public void onRightClickEntity(PlayerInteractEntityEvent e){
        //right click 不知道为什么运行两次，以下count代码用来限制
        if (count <= 0) {
            count++;
            Player p = e.getPlayer();
            Entity rightClickedEntity = e.getRightClicked();
            Material itemOnMainhand = p.getInventory().getItemInMainHand().getType();
            PersistentDataContainer data = rightClickedEntity.getPersistentDataContainer();

            if (pullAbleEntityTypes.contains(rightClickedEntity.getType()) && itemOnMainhand == Material.DIAMOND && p.isSneaking() == false) {
                if (!data.has(new NamespacedKey(MidoMika_SuperHighWay.getPlugin(), "Owner"), PersistentDataType.STRING)) {
                    data.set(new NamespacedKey(MidoMika_SuperHighWay.getPlugin(), "Owner"), PersistentDataType.STRING, p.getName());
                    p.sendMessage(ChatColor.GREEN + "Marked this " + ChatColor.YELLOW + rightClickedEntity.getName() + ChatColor.GREEN + " as your leashing creature!");

                } else {
                    String entityOwner = data.get(new NamespacedKey(MidoMika_SuperHighWay.getPlugin(), "Owner"), PersistentDataType.STRING);
                    if (entityOwner.equals(p.getName())) {
                        p.sendMessage(ChatColor.GREEN + "This " + ChatColor.YELLOW + rightClickedEntity.getName() + ChatColor.GREEN + " already mark as your leashing creature!");
                        Location targetLocation = rightClickedEntity.getLocation();
                        double radius = 5.0;
                        Collection<Entity> nearbyEntities = targetLocation.getWorld().getNearbyEntities(targetLocation, radius, radius, radius);

                        for (Entity entity : nearbyEntities) {
                            if (entity.getType() == EntityType.MINECART) {
                                Minecart minecart = (Minecart) entity;
                                if (minecart.getPassengers().toString() == "[]") {
                                    moveEntityToLocation(minecart, targetLocation);
                                    break;
                                }
                            }
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "This " + ChatColor.YELLOW + rightClickedEntity.getName() + ChatColor.RED + " already mark by someone!");

                    }
                }
            } else if (pullAbleEntityTypes.contains(rightClickedEntity.getType()) && itemOnMainhand == Material.DIAMOND && p.isSneaking() == true) {
                if (data.has(new NamespacedKey(MidoMika_SuperHighWay.getPlugin(), "Owner"), PersistentDataType.STRING)) {
                    String entityOwner = data.get(new NamespacedKey(MidoMika_SuperHighWay.getPlugin(), "Owner"), PersistentDataType.STRING);

                    if (entityOwner.equals(p.getName())) {
                        p.sendMessage(net.md_5.bungee.api.ChatColor.of(new java.awt.Color(255,165,0, 10)) + "Unmarked this " + ChatColor.YELLOW + rightClickedEntity.getName() + net.md_5.bungee.api.ChatColor.of(new java.awt.Color(255,165,0, 10)) + " as your leashing creature!");
                        data.remove(new NamespacedKey(MidoMika_SuperHighWay.getPlugin(), "Owner"));

                    } else {
                        p.sendMessage(ChatColor.RED + "This " + ChatColor.YELLOW + rightClickedEntity.getName() + ChatColor.RED + " is not marked by you!");

                    }
                }
            }
        } else if (count > 0) {
            count = 0;

        }
    }

    private void moveEntityToLocation(Minecart minecart, Location targetLocation) {
        // 计算从实体当前位置到目标位置的向量
        Vector vector = targetLocation.toVector().subtract(minecart.getLocation().toVector());
        minecart.setMaxSpeed(7);
        minecart.setVelocity(vector.multiply(1));

    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent e) {
        Entity vehicle = e.getVehicle();

        if (e.getVehicle() instanceof Minecart) {
            Minecart minecart = (Minecart) e.getVehicle();
            double radius = 20.0;
            List<Material> activateMinecartBlock = new ArrayList<>(Arrays.asList(Material.WHITE_WOOL, Material.BLACK_WOOL));
            List<Material> destoryMinecartBlock = new ArrayList<>(Arrays.asList(Material.RED_WOOL));

            // 检查载具是否是矿车
            if (!vehicle.getPassengers().isEmpty() && vehicle.getPassengers().get(0) instanceof Player) {
                Player p = (Player) vehicle.getPassengers().get(0);
                Location playerLocation = p.getLocation();
                Collection<Entity> nearbyEntities = p.getWorld().getNearbyEntities(playerLocation, radius, radius, radius);

                for (Entity entity : nearbyEntities) {
                    if (entity.getType() == EntityType.MINECART) {
                        Minecart minecartRideWithEntity = (Minecart) entity;
                        passengers = minecartRideWithEntity.getPassengers();
                        for (Entity passenger : passengers) {
                            if (passenger.getType() != EntityType.PLAYER) {
                                PersistentDataContainer data = passenger.getPersistentDataContainer();
                                boolean hasData = data.has(new NamespacedKey(MidoMika_SuperHighWay.getPlugin(), "Owner"), PersistentDataType.STRING);
                                if (hasData) {
                                    String entityOwner = data.get(new NamespacedKey(MidoMika_SuperHighWay.getPlugin(), "Owner"), PersistentDataType.STRING);
                                    if (entityOwner.equals(p.getName())) {
                                        //you are owner
                                        Vector vectorToPlayer = p.getLocation().toVector().subtract(minecartRideWithEntity.getLocation().toVector());
                                        if (vectorToPlayer.lengthSquared() > 10.0) {
                                            // 设置矿车速度向量
                                            if (hashSpeed.get(p.getUniqueId()) != null) {
                                                if (hashSpeed.get(p.getUniqueId()) <= 0.2) {
                                                    minecartRideWithEntity.setMaxSpeed(7);
                                                    minecartRideWithEntity.setVelocity(vectorToPlayer.normalize().multiply(0.2).setY(0));

                                                } else {
                                                    minecartRideWithEntity.setMaxSpeed(7);
                                                    minecartRideWithEntity.setVelocity(vectorToPlayer.normalize().multiply(0.7));

                                                }
                                            }
                                        } else {
                                            //too near
                                            vectorToPlayer = p.getLocation().toVector().subtract(minecartRideWithEntity.getLocation().toVector()).multiply(-2);
                                            minecartRideWithEntity.setMaxSpeed(7);
                                            minecartRideWithEntity.setVelocity(vectorToPlayer.normalize().multiply(0.5));

                                        }
                                    } else {
                                        //own by other player

                                    }
                                }
                            } else {

                            }
                        }
                    }
                }

                passengers.clear();

                if (activateMinecartBlock.contains(minecart.getLocation().subtract(0, 1, 0).getBlock().getType())) {
                    // 检查矿车是否有乘客且是玩家
                    Vector direction = p.getLocation().getDirection();
                    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(102, 102, 153), 2f);
                    Vector particleDirection = p.getLocation().getDirection().normalize();
                    Vector offset = particleDirection.clone().multiply(-2); // 向后偏移
                    Location particleLocation = p.getLocation().add(offset);

                    // 在位置生成带颜色的粒子效果
                    p.spawnParticle(Particle.REDSTONE, particleLocation, 10, 0.125, .125, .125, 0.01, dustOptions);

                    minecart.setMaxSpeed(7);
                    if (hashSpeed.get(p.getUniqueId()) != null) {
                        minecart.setVelocity(new Vector(direction.normalize().getX(), 0, direction.normalize().getZ()).multiply(hashSpeed.get(p.getUniqueId())).setY(0.25));
                    }

                //因为car飞太高了，所以设置为car以下的1或2格
                } else if (destoryMinecartBlock.contains(minecart.getLocation().subtract(0, 2, 0).getBlock().getType()) || destoryMinecartBlock.contains(minecart.getLocation().subtract(0, 1, 0).getBlock().getType())) {
                    if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                        p.sendMessage(ChatColor.RED + "Car Break!");
                        Bukkit.getServer().getWorld(p.getWorld().getName()).dropItemNaturally(minecart.getLocation(), new ItemStack(Material.MINECART));
                        minecart.remove();
                    }else {
                        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                        p.sendMessage(ChatColor.RED + "Car Break!");
                        minecart.remove();

                    }
                }
            }
        }
    }


    @EventHandler
    public void onRideVehicle(VehicleEnterEvent e){
        if (e.getVehicle() instanceof Minecart) {
            Minecart minecart = (Minecart) e.getVehicle();
            PersistentDataContainer data = e.getEntered().getPersistentDataContainer();

            if (e.getEntered() instanceof Player) {
                Player p = (Player) e.getEntered();
                List<Material> activateMinecartBlock = new ArrayList<>(Arrays.asList(Material.WHITE_WOOL, Material.BLACK_WOOL));

                if (activateMinecartBlock.contains(minecart.getLocation().subtract(0, 1, 0).getBlock().getType())) {
                    // 检查矿车是否有乘客且是玩家
                    if (e.getEntered() instanceof Player) {
                        hashSpeed.put(p.getUniqueId(), 0.7);
                        hashGear.put(p.getUniqueId(), 1.0);
                        p.sendMessage(ChatColor.GREEN + "Car activate!");
                        Vector direction = p.getLocation().getDirection();
                        minecart.setMaxSpeed(1);
                        minecart.setVelocity(new Vector(direction.normalize().getX(), 0, direction.normalize().getZ()).setY(0.05));

                    }
                }
            }
        }
    }

    @EventHandler
    public void onLeaveVehicle(VehicleExitEvent e){
        if (e.getVehicle() instanceof Minecart) {
            if (e.getExited() instanceof Player) {
                Player p = (Player) e.getExited();
                hashSpeed.remove(p.getUniqueId());
                hashGear.remove(p.getUniqueId());

            }
        }
    }

    @EventHandler
    public void onPlaceMinecart(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block spawnMinecartBlock = p.getTargetBlockExact(5);

        if (spawnMinecartBlock != null && p.getInventory().getItemInMainHand().getType().equals(Material.MINECART)) {
            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Location location = spawnMinecartBlock.getLocation().add(0, 1, 0);
                Minecart minecart = (Minecart) p.getWorld().spawnEntity(location, EntityType.MINECART);
                if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                    p.getInventory().removeItem(new ItemStack(Material.MINECART, 1));
                }

            }
        }else {

        }
    }
}
