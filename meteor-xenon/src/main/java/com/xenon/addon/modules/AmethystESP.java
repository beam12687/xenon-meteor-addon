package com.xenon.addon.modules;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class AmethystESP extends Module {
    private final SettingGroup sg=settings.getDefaultGroup();
    private final SettingGroup sgR=settings.createGroup("Render");
    private final Setting<Integer> simDist=sg.add(new IntSetting.Builder().name("sim-distance").defaultValue(8).min(1).sliderMax(32).build());
    private final Setting<Integer> minCluster=sg.add(new IntSetting.Builder().name("min-cluster").defaultValue(3).min(1).sliderMax(20).build());
    private final Setting<Double> chunkY=sg.add(new DoubleSetting.Builder().name("chunk-y").defaultValue(55).min(-64).max(320).build());
    private final Setting<Boolean> esp=sgR.add(new BoolSetting.Builder().name("block-esp").defaultValue(true).build());
    private final Setting<Boolean> chunkMark=sgR.add(new BoolSetting.Builder().name("chunk-mark").defaultValue(true).build());
    private final Setting<Boolean> tracers=sgR.add(new BoolSetting.Builder().name("tracers").defaultValue(true).build());
    private final Setting<Boolean> chat=sg.add(new BoolSetting.Builder().name("chat-alert").defaultValue(true).build());
    private final Setting<SettingColor> espCol=sgR.add(new ColorSetting.Builder().name("esp-color").defaultValue(new SettingColor(180,100,255,180)).build());
    private final Setting<SettingColor> chunkCol=sgR.add(new ColorSetting.Builder().name("chunk-color").defaultValue(new SettingColor(180,100,255,80)).build());
    private final Map<ChunkPos,Set<BlockPos>> clusters=new ConcurrentHashMap<>();
    private final Set<ChunkPos> notified=ConcurrentHashMap.newKeySet();
    private int tick=0;
    public AmethystESP(){super(com.xenon.addon.XenonAddon.CATEGORY,"amethyst-esp","Highlights amethyst geodes.");}
    @Override public void onActivate(){clusters.clear();notified.clear();fullScan();}
    @Override public void onDeactivate(){clusters.clear();notified.clear();}
    @EventHandler private void onTick(TickEvent.Post e){if(mc.world==null||mc.player==null)return;if(++tick%40!=0)return;ChunkPos c=mc.player.getChunkPos();int r=simDist.get();for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){WorldChunk ch=mc.world.getChunkManager().getWorldChunk(c.x+dx,c.z+dz,false);if(ch!=null)scan(ch);}}
    @EventHandler private void onChunk(ChunkDataEvent e){scan(e.chunk());}
    @EventHandler private void onRender(Render3DEvent e){if(mc.world==null||clusters.isEmpty())return;double fy=chunkY.get();for(Map.Entry<ChunkPos,Set<BlockPos>> en:clusters.entrySet()){ChunkPos cp=en.getKey();Set<BlockPos> ps=en.getValue();if(ps.isEmpty())continue;if(chunkMark.get())e.renderer.box(cp.getStartX(),fy,cp.getStartZ(),cp.getEndX()+1,fy+0.05,cp.getEndZ()+1,chunkCol.get(),chunkCol.get(),ShapeMode.Both,0);if(esp.get())for(BlockPos p:ps)e.renderer.box(p.getX(),p.getY(),p.getZ(),p.getX()+1,p.getY()+1,p.getZ()+1,espCol.get(),espCol.get(),ShapeMode.Both,0);if(tracers.get()){BlockPos near=null;double nd=Double.MAX_VALUE;for(BlockPos p:ps){double d=p.getSquaredDistance(mc.player.getBlockPos());if(d<nd){nd=d;near=p;}}if(near!=null){Vec3d s=mc.player.getEyePos();Vec3d t=Vec3d.ofCenter(near);e.renderer.line(s.x,s.y,s.z,t.x,t.y,t.z,espCol.get());}}}}
    private void fullScan(){if(mc.world==null||mc.player==null)return;ChunkPos c=mc.player.getChunkPos();int r=simDist.get();int n=0;for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){WorldChunk ch=mc.world.getChunkManager().getWorldChunk(c.x+dx,c.z+dz,false);if(ch!=null){scan(ch);n++;}}info("Scanned "+n+" chunks | "+clusters.size()+" geodes");}
    private void scan(WorldChunk chunk){if(mc.world==null)return;ChunkPos cp=chunk.getPos();Set<BlockPos> hits=new HashSet<>();int bx=cp.x<<4,bz=cp.z<<4;for(int y=-64;y<=70;y++)for(int lx=0;lx<16;lx++)for(int lz=0;lz<16;lz++){BlockPos p=new BlockPos(bx+lx,y,bz+lz);if(nearby(p))hits.add(p.toImmutable());}if(hits.size()>=minCluster.get()){clusters.put(cp,hits);if(chat.get()&&notified.add(cp))info("Amethyst at "+cp.x+","+cp.z+" ("+hits.size()+"hits)");}else{clusters.remove(cp);notified.remove(cp);}}
    private boolean nearby(BlockPos p){for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)for(int dz=-1;dz<=1;dz++){BlockState s=mc.world.getBlockState(p.add(dx,dy,dz));if(s.isOf(Blocks.AMETHYST_CLUSTER)||s.isOf(Blocks.LARGE_AMETHYST_BUD)||s.isOf(Blocks.MEDIUM_AMETHYST_BUD)||s.isOf(Blocks.SMALL_AMETHYST_BUD)||s.isOf(Blocks.BUDDING_AMETHYST)||s.isOf(Blocks.AMETHYST_BLOCK))return true;}return false;}
}
