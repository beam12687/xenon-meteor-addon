package com.xenon.addon.modules;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class ActivityDebug extends Module {
    private final SettingGroup sg=settings.getDefaultGroup();
    private final Setting<Double> yLevel=sg.add(new DoubleSetting.Builder().name("y-level").defaultValue(16).min(-64).max(320).build());
    private final Setting<Boolean> notif=sg.add(new BoolSetting.Builder().name("notification").defaultValue(false).build());
    private final Setting<SettingColor> color=sg.add(new ColorSetting.Builder().name("color").defaultValue(new SettingColor(255,220,0,80)).build());
    private final Set<ChunkPos> sus=Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<Long,Long> lastNotif=new ConcurrentHashMap<>();
    private final Map<Class<?>,List<Field>> bpF=new ConcurrentHashMap<>(),v3F=new ConcurrentHashMap<>(),dF=new ConcurrentHashMap<>(),nF=new ConcurrentHashMap<>();
    private final ThreadLocal<Set<Integer>> explored=ThreadLocal.withInitial(()->Collections.newSetFromMap(new ConcurrentHashMap<>()));
    public ActivityDebug(){super(com.xenon.addon.XenonAddon.CATEGORY,"activity-debug","Detects underground activity.");}
    @Override public void onDeactivate(){sus.clear();lastNotif.clear();bpF.clear();v3F.clear();dF.clear();nF.clear();}
    @EventHandler private void onPacket(PacketEvent.Receive e){Packet<?> p=e.packet;if(p instanceof ChunkDeltaUpdateS2CPacket s){s.visitUpdates((pos,st)->check(pos.getX(),pos.getY(),pos.getZ()));return;}if(p instanceof BlockUpdateS2CPacket b){BlockPos pos=b.getPos();check(pos.getX(),pos.getY(),pos.getZ());return;}explored.get().clear();analyze(p,0);}
    @EventHandler private void onRender(Render3DEvent e){if(mc.world==null)return;double y=Math.max(mc.world.getBottomY(),Math.min(57,mc.world.getTopY()));for(ChunkPos cp:sus)e.renderer.box(cp.getStartX(),y,cp.getStartZ(),cp.getEndX()+1,y+0.05,cp.getEndZ()+1,color.get(),color.get(),ShapeMode.Both,0);}
    private void check(double x,double y,double z){if(mc.player!=null&&mc.player.getY()<0)return;if(y<=yLevel.get()){ChunkPos cp=new ChunkPos((int)Math.floor(x)>>4,(int)Math.floor(z)>>4);if(sus.add(cp)&&notif.get()&&mc.player!=null){long k=cp.toLong();long now=System.currentTimeMillis();if(now-lastNotif.getOrDefault(k,0L)>=1250){lastNotif.put(k,now);info("Activity at chunk "+cp.x+","+cp.z+" Y"+(int)Math.floor(y));}}}}
    private void analyze(Object obj,int d){if(obj==null||d>3)return;int h=System.identityHashCode(obj);if(!explored.get().add(h))return;Class<?> c=obj.getClass();bpF.computeIfAbsent(c,cl->fields(cl,BlockPos.class));v3F.computeIfAbsent(c,cl->fields(cl,Vec3d.class));dF.computeIfAbsent(c,cl->fields(cl,double.class));nF.computeIfAbsent(c,cl->{List<Field> l=new ArrayList<>();while(cl!=null&&cl!=Object.class){for(Field f:cl.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())&&!f.getType().isPrimitive()&&!f.getType().getName().startsWith("java.")&&!f.getType().isEnum()){f.setAccessible(true);l.add(f);}cl=cl.getSuperclass();}return l;});for(Field f:bpF.get(c)){try{BlockPos bp=(BlockPos)f.get(obj);if(bp!=null)check(bp.getX(),bp.getY(),bp.getZ());}catch(Exception ignored){}}for(Field f:v3F.get(c)){try{Vec3d v=(Vec3d)f.get(obj);if(v!=null)check(v.x,v.y,v.z);}catch(Exception ignored){}}List<Field> dl=dF.get(c);if(dl.size()>=3){try{double x=dl.get(0).getDouble(obj),y=dl.get(1).getDouble(obj),z=dl.get(2).getDouble(obj);if(Math.abs(x)<3e7&&Math.abs(z)<3e7&&y>-2048&&y<2048)check(x,y,z);}catch(Exception ignored){}}for(Field f:nF.get(c)){try{Object n=f.get(obj);if(n!=null)analyze(n,d+1);}catch(Exception ignored){}}}
    private List<Field> fields(Class<?> c,Class<?> t){List<Field> l=new ArrayList<>();while(c!=null&&c!=Object.class){for(Field f:c.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())&&f.getType()==t){f.setAccessible(true);l.add(f);}c=c.getSuperclass();}return l;}
}
