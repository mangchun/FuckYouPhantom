package org.example;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;

import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NATURAL;

public class fuckyouphantom extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getLogger().info("fuck you phantom");
    }
    @Override
    public void onDisable() {
        getLogger().info("motnahp uoy kcuf");
    }
}
/**
 * 监听幻翼自然生成事件，并为每一位亲爱的幻翼生成独特的跟踪导弹（幻翼我草泥马）
 * 致敬幻翼
 *
 *
 */


class EventListener implements Listener {
    private Plugin plugin = JavaPlugin.getPlugin(fuckyouphantom.class);
    // 幻翼对象池
    private ArrayList phantoms = new ArrayList<Entity>();
    // 导弹对象池
    private ArrayList snowballs = new ArrayList<Entity>();
    // 一开始打算用雪球做导弹的模型，后来替换成其他的，所以变量名无需在意
    // 导弹幻翼一对一对象池，即1个幻翼对象对于一个导弹对象[幻翼entity，导弹entity]
    private ArrayList task_queue = new ArrayList();
    private boolean suo = false;
    @EventHandler
    public void onPlayerJoin(CreatureSpawnEvent event) {
        // 将新生成的幻翼/导弹添加进列表并触发任务调度器。注意：导弹的实体在生成时会消除重力
        // 将生成时间加入列表供超时销毁使用
        // & event.getSpawnReason().equals(NATURAL)
        if (event.getEntity().getName().equals("Phantom")){
            //event.getEntity().teleport(new Location(event.getEntity().getWorld(),-6,135,3));
            Entity ph = event.getEntity();
            Entity sn = event.getEntity().getWorld().spawnEntity(getSnowballSpawnPosition(event.getEntity()), EntityType.ARROW);
            sn.setGravity(false);
            phantoms.add(ph);
            snowballs.add(sn);
            ArrayList task = new ArrayList<>();
            task.add(ph);
            task.add(sn);
            task.add(Instant.now().getEpochSecond());
            task_queue.add(task);
            attack_Phantom();
        }
    }


    private void attack_Phantom(){
        // 将检测任务加入主线程执行
        BukkitRunnable br = new BukkitRunnable() {
            // 如果导弹实体未被销毁则持续检测直到销毁
            @Override
            public void run() {
                suo = true;
                if (snowballs.isEmpty()){
                    this.cancel();
                    suo = false;
                }
                check_snowball_to_phantom();
                }
            };
        if (!suo){br.runTaskTimer(plugin,20,1);}
        }



    // 更新实体对象池，删除死亡的实体，引导导弹飞向目标
    private void update(){
        if (!phantoms.isEmpty()){
            Iterator iterator = phantoms.iterator();
            while (iterator.hasNext()){
                Entity entity = (Entity) iterator.next();
                if(entity.isDead()){
                    iterator.remove();
                }
            }
        }
        if (!snowballs.isEmpty()){
            Iterator iterator = snowballs.iterator();
            while (iterator.hasNext()){
                Entity entity = (Entity) iterator.next();
                if(entity.isDead()){
                    iterator.remove();
                }
            }
        }
        if (!task_queue.isEmpty()){
            Iterator iterator = task_queue.iterator();
            while (iterator.hasNext()){
                ArrayList<Entity> list = (ArrayList) iterator.next();
                // 超时销毁
                if(list.get(0).isDead() | Integer.parseInt(String.valueOf(list.get(2)))+8 <= Instant.now().getEpochSecond()){
                    list.get(0).remove();
                    list.get(1).remove();
                    iterator.remove();
                }else {
                    // 导弹算法
                    System.out.println(String.valueOf(list.get(2)));
                    Entity target = list.get(0);   // 目标
                    Entity mover  = list.get(1);   // 要移动的实体
                    org.bukkit.util.Vector vector = target.getLocation().toVector()
                            .subtract(mover.getLocation().toVector());
                    // 设置向量生成粒子和声音
                    mover.setVelocity(vector.normalize().multiply(0.4));
                    // 烟火
                    mover.getWorld().spawnParticle(Particle.LAVA,mover.getLocation(),2);
                    // 樱花粒子
                    mover.getWorld().spawnParticle(Particle.CHERRY_LEAVES,mover.getLocation(),2);
                    // 细雪粒子
                    mover.getWorld().spawnParticle(Particle.SNOWFLAKE,mover.getLocation(),1);
                    // 烟花声音
                    mover.getWorld().playSound(mover.getLocation(),Sound.ENTITY_FIREWORK_ROCKET_TWINKLE,2,2);
                }
            }
        }
    }


    // 监测导弹周边实体并爆炸
    private void check_snowball_to_phantom(){
        update();
        if (!snowballs.isEmpty()){
            Iterator snowball_iterator = snowballs.iterator();
            while (snowball_iterator.hasNext()){
                Entity snowball = (Entity) snowball_iterator.next();
                snowball.setGravity(true);
                // 范围内的实体列表
                ArrayList<Entity> arrayList = (ArrayList) snowball.getNearbyEntities(2, 2, 2.);
                if (!arrayList.isEmpty()){
                    // 生成爆炸并删除导弹
                    if (arrayList.get(0).getName().equals("Phantom")){
                        snowball.getWorld().createExplosion(snowball.getLocation(),5,false,false);
                        snowball.getWorld().spawnParticle(Particle.FLASH,snowball.getLocation(),2);
                        snowball.remove();
                    }

                }
            }
        }

    }
    // 获取导弹生成位置
    private Location getSnowballSpawnPosition(Entity entity){
        Location location = entity.getLocation();
        while (location.getBlock().getType() == Material.AIR | location.getY() < 63){
            location.setY(location.getBlockY()-1);
        }
        // 离地高度
        location.setY(location.getY()+3);
        return location;
    }
}